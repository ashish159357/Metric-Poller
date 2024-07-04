package ssh

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"myproject/models"
	"myproject/utils"
	"net"
	"strings"
	"time"

	"golang.org/x/crypto/ssh"
)

func GetMetric(model models.Device) {
	defer func() {
		if r := recover(); r != nil {
			metricsWrapper := models.MetricsWrapper{
				MonitorId: model.MonitorId,
				Metrics:   models.Metrics{},
				Status:    "fail",
			}

			metricsJSON, err := json.Marshal(metricsWrapper)
			if err != nil {
				log.Fatalf("Error marshalling error metrics to JSON: %v", err)
			}
			fmt.Println(string(metricsJSON))
		}
	}()

	// SSH connection configuration
	host := model.Ip
	port := "22"
	user := model.Username
	password := model.Password

	// Create the SSH client config
	sshConfig := &ssh.ClientConfig{
		User: user,
		Auth: []ssh.AuthMethod{
			ssh.Password(password),
		},
		HostKeyCallback: ssh.InsecureIgnoreHostKey(), // Use this for testing only, avoid in production
		Timeout:         4 * time.Second,             // Timeout for SSH connection
	}

	// Create a context with a timeout
	ctx, cancel := context.WithTimeout(context.Background(), 4*time.Second)
	defer cancel()

	// Dial TCP connection with timeout
	dialer := net.Dialer{}
	conn, err := dialer.DialContext(ctx, "tcp", net.JoinHostPort(host, port))
	if err != nil {
		utils.LogError(model.MonitorId, "fail", fmt.Sprintf("Error dialing TCP: %v", err))
		// panic("Error dialing TCP")
	}
	defer conn.Close()

	// Establish SSH connection
	sshConn, chans, reqs, err := ssh.NewClientConn(conn, net.JoinHostPort(host, port), sshConfig)
	if err != nil {
		utils.LogError(model.MonitorId, "fail", fmt.Sprintf("Error establishing SSH connection: %v", err))
		// panic("Error establishing SSH connection")
	}
	defer sshConn.Close()

	// Create SSH client
	client := ssh.NewClient(sshConn, chans, reqs)
	defer client.Close()

	// Execute commands to get metrics
	cpuUtilization, err := runCommand(client, "mpstat -P ALL 1 1 | awk 'NR > 3 && $3 ~ /[0-9.]+/ { sum += (100 - $12); count++ } END { if (count > 0) print sum / count }'")
	if err != nil {
		utils.LogError(model.MonitorId, "fail", fmt.Sprintf("Error running CPU command: %v", err))
		// panic("Error running CPU command")
	}

	memoryUtilization, err := runCommand(client, "free | awk '/Mem/{printf(\"%.2f\\n\", $3/$2 * 100.0)}'")
	if err != nil {
		utils.LogError(model.MonitorId, "fail", fmt.Sprintf("Error running Memory command: %v", err))
		// panic("Error running Memory command")
	}

	diskUtilization, err := runCommand(client, "df -h --output=pcent | awk 'NR>1 {gsub(/%/, \"\", $1); total+=$1; count+=1} END {if (count > 0) printf(\"%.2f\\n\", total/count)}'")
	if err != nil {
		utils.LogError(model.MonitorId, "fail", fmt.Sprintf("Error running Disk command: %v", err))
		// panic("Error running Disk command")
	}

	// Construct Metrics object
	metrics := models.Metrics{
		CPU:    strings.TrimSpace(string(cpuUtilization)),
		Memory: strings.TrimSpace(string(memoryUtilization)),
		Disk:   strings.TrimSpace(string(diskUtilization)),
	}

	// Construct MetricsWrapper object
	metricsWrapper := models.MetricsWrapper{
		MonitorId: model.MonitorId,
		Metrics:   metrics,
		Status:    "success",
	}

	// Marshal MetricsWrapper to JSON
	metricsJSON, err := json.Marshal(metricsWrapper)
	if err != nil {
		utils.LogError(model.MonitorId, "fail", fmt.Sprintf("Error marshalling metrics to JSON: %v", err))
		// panic("Error marshalling metrics to JSON")
	}

	fmt.Println(string(metricsJSON))
}

func runCommand(client *ssh.Client, command string) ([]byte, error) {
	session, err := client.NewSession()
	if err != nil {
		return nil, fmt.Errorf("failed to create session: %v", err)
	}
	defer session.Close()

	output, err := session.CombinedOutput(command)
	if err != nil {
		return nil, fmt.Errorf("command execution error: %v", err)
	}

	return output, nil
}
