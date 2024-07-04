package main

import (
	"encoding/json"
	"io"
	"log"
	"myproject/models"
	"myproject/ssh"
	"os"
	"sync"
)

func main() {

	// Read JSON input from stdin
	jsonData, err := io.ReadAll(os.Stdin)
	if err != nil {
		log.Fatalf("Error reading input: %v", err)
	}

	// Deserialize JSON to struct
	var devices []models.Device
	if err := json.Unmarshal(jsonData, &devices); err != nil {
		log.Fatalf("Error unmarshalling JSON: %v", err)
	}

	// Use a WaitGroup to wait for all goroutines to finish
	var wg sync.WaitGroup

	for _, device := range devices {
		wg.Add(1)
		go func(device models.Device) {
			defer wg.Done()
			ssh.GetMetric(device)
		}(device)
	}

	wg.Wait()
}
