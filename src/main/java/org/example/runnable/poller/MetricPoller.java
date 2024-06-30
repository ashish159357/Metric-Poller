package org.example.runnable.poller;

import io.vertx.core.json.JsonObject;
import org.example.config.ApplicationConfig;
import org.example.config.ProcessBuilderConfig;
import org.example.utils.DateUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MetricPoller{

    private List<JsonObject> devices;
    private JsonObject discovery;
    private Process process;
    private long metricPollerTimeout = ApplicationConfig.METRIC_POLLER_TIMEOUT.value;
    private ProcessBuilderConfig processBuilderConfig;

    public MetricPoller(List<JsonObject> device, JsonObject discovery, long schedulerPeriod){
        this.devices = device;
        this.discovery = discovery;
        this.processBuilderConfig = new ProcessBuilderConfig("go run /home/ashish/a4h-personal/Prototype/go-proto/myproject/main.go");
        if(schedulerPeriod < metricPollerTimeout){
            throw new RuntimeException("Poller Scheduler Period should always more than Metric Poller Timeout");
        }
    }


    public List<JsonObject> run() {
        return startDiscovery(devices,discovery);
    }


    public List<JsonObject> startDiscovery(List<JsonObject> devices,JsonObject discovery){
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
//                Boolean responseFromGo = metricPollerTimeout(reader,metricPollerTimeout,TimeUnit.SECONDS);

                    String line;
                    while ((line = reader.readLine()) != null) {
                        JsonObject object = new JsonObject(line);
                        object.put("timestamp", DateUtils.getCurrentTimeStamp());

                        listOfMetrics.add(object);
                        System.out.println(object);
                    }
                    System.out.println("------------------------------------------------");

        } catch (IOException  e) {
            throw new RuntimeException(e);
        }

        return listOfMetrics;
    }


//    public void updateMonitor(JsonObject object){
//        JsonObject updateMonitorDetails = new JsonObject();
//
//        updateMonitorDetails.put(Constants.DAO_KEY,Constants.MONITOR_DAO_NAME);
//        updateMonitorDetails.put("monitorId",object.getString("monitorId"));
//
//        updateMonitorDetails.put("status",object.getString("status"));
//        vertx.eventBus().send(EventBusAddresses.DATABASE_UPDATE,updateMonitorDetails);
//
//    }


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


    public boolean metricPollerTimeout(BufferedReader reader, long timeout, TimeUnit unit) throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Boolean> future = executor.submit(() -> {
            try {
                while (!reader.ready()) {
                    Thread.sleep(50); // Sleep for a short time to avoid busy waiting
                }
                return true; // Ready to read
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false; // Handle interruption
            }
        });

        Boolean flag = false;
        try {
            flag = future.get(timeout, unit);
        } catch (TimeoutException e) {
            future.cancel(true); // Cancel the task
        } catch (ExecutionException e) {
            throw new IOException("Error while reading input", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Thread was interrupted", e);
        } finally {
            executor.shutdownNow(); // Properly shut down the executor
        }
        return flag;
    }

    public List<JsonObject> getDevices() {
        return devices;
    }

    public void setDevices(List<JsonObject> devices) {
        this.devices = devices;
    }

}
