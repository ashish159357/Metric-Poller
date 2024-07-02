package org.example.runnable.poller;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.example.config.ApplicationConfig;
import org.example.config.ProcessBuilderConfig;
import org.example.service.MonitorService;
import org.example.utils.DateUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class MetricPoller{

    private List<JsonObject> devices;
    private JsonObject discovery;
    private Process process;
    private long metricPollerTimeout = ApplicationConfig.METRIC_POLLER_TIMEOUT.value;
    private ProcessBuilderConfig processBuilderConfig;
    private MonitorService monitorService;


    public MetricPoller(List<JsonObject> device, JsonObject discovery, long schedulerPeriod, MonitorService monitorService){
        this.monitorService = monitorService;
        this.devices = device;
        this.discovery = discovery;
        this.processBuilderConfig = new ProcessBuilderConfig("go run /home/ashish/a4h-personal/Prototype/go-proto/myproject/main.go");
        if(schedulerPeriod < metricPollerTimeout){
            throw new RuntimeException("Poller Scheduler Period should always more than Metric Poller Timeout");
        }
    }


    public List<JsonObject> run() {
        return startMonitor(devices,discovery);
    }


    public List<JsonObject> startMonitor(List<JsonObject> devices,JsonObject discovery){
        List<JsonObject> respone = null;
        try {

            // Start the process
            process = processBuilderConfig.getAndStartProcess();

            JsonObject listOfdevices = new JsonObject();
            listOfdevices.put("devices",devices);

            // Send JSON to Go application
            sendData(process,listOfdevices,discovery);

            // Read JSON from GO
            respone = readResponse(process);

            // waiting for process to complete
            process.waitFor(60,TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("Unable to Start Discovery : " + e.getMessage());
        }
        return respone;
    }


    public List<JsonObject> readResponse(Process process){
        List<JsonObject> listOfMetrics = new ArrayList<>();

        // Read the output from Go application (if any)
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                JsonObject object = new JsonObject(line);
                object.put("timestamp", DateUtils.getCurrentTimeStamp());

//                if(object.getString("status").equals("fail")){
//                    removeFailedMonitorDevice(object.getString("monitorId"),process);
//                }
                listOfMetrics.add(object);
                log.info(String.valueOf(object));
            }
        } catch (IOException  e) {
            throw new RuntimeException(e);
        }
        log.info("-----------------------------------------------------------");
        return listOfMetrics;
    }


    public void sendData(Process process, JsonObject device, JsonObject discovery){

        // Send JSON to Go application
        OutputStream os = process.getOutputStream();
        device.put("ip",discovery.getString("ip"));
        try {
            os.write(device.getString("devices").getBytes());
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void removeFailedMonitorDevice(String monitorId, Process process){

        // update monitor status in database
        List<JsonObject> filteredDevices = new ArrayList<>();
        for(JsonObject device:devices){
            if(!device.getString("monitorId").equals(monitorId)){
                filteredDevices.add(device);
            }
            else {
                device.put("status","fail");
                monitorService.updateMonitor(device);
            }
        }
        if(filteredDevices.size() == 0){
            process.destroyForcibly();
            log.info("Metric Poller Process is killed as No devices available to monitor");
        }
        setDevices(filteredDevices);
    }

    public List<JsonObject> getDevices() {
        return devices;
    }


    public void setDevices(List<JsonObject> devices) {
        this.devices = devices;
    }
}
