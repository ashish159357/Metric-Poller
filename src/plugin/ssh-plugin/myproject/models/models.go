package models

type Device struct {
	Username  string `json:"username"`
	Password  string `json:"password"`
	Ip        string `json:"ip"`
	MonitorId string `json:"monitorId"`
}

type Metrics struct {
	CPU    string `json:"cpu"`
	Memory string `json:"memory"`
	Disk   string `json:"disk"`
}

type MetricsWrapper struct {
	MonitorId string  `json:"monitorId"`
	Metrics   Metrics `json:"metrics"`
	Status    string  `json:"status"`
}
