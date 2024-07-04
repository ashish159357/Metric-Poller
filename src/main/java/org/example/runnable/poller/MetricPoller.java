package org.example.runnable.poller;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.example.config.ProcessBuilderConfig;
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
    private final ProcessBuilderConfig processBuilderConfig = new ProcessBuilderConfig("go run /home/ashish/a4h-personal/Prototype/go-proto/myproject/main.go");

    public MetricPoller(List<JsonObject> device){
        this.devices = device;
    }

    public MetricPoller(){}

    public List<JsonObject> run() {
        return startMonitor(devices);
    }


    public List<JsonObject> startMonitor(List<JsonObject> devices){
        List<JsonObject> respone = null;
        try {

            // Start the process
            Process process = processBuilderConfig.getAndStartProcess();

            JsonObject listOfdevices = new JsonObject();
            listOfdevices.put("devices",devices);

            // Send JSON to Go application
            String timestamp = sendData(process,devices);

            // Read JSON from GO
            respone = readResponse(process,timestamp);

            // waiting for process to complete
            process.waitFor(60,TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("Unable to Start Discovery : " + e.getMessage());
        }
        return respone;
    }


    public List<JsonObject> readResponse(Process process,String timestamp){
        List<JsonObject> listOfMetrics = new ArrayList<>();

        // Read the output from Go application (if any)
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                JsonObject object = new JsonObject(line);
                object.put("startTime",timestamp);
                object.put("endTime", DateUtils.getCurrentTimeStamp());

                listOfMetrics.add(object);
                log.info(String.valueOf(object));
            }
        } catch (IOException  e) {
            throw new RuntimeException(e);
        }
        log.info("-----------------------------------------------------------");
        return listOfMetrics;
    }


    public String sendData(Process process, List<JsonObject> devices){

        // Send JSON to Go application
        OutputStream os = process.getOutputStream();
        try {
            os.write(devices.toString().getBytes());
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return DateUtils.getCurrentTimeStamp();
    }

    public List<JsonObject> getDevices() {
        return devices;
    }


    public void setDevices(List<JsonObject> devices) {
        this.devices = devices;
    }
}
