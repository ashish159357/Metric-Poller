package org.example.runnable.poller;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.example.config.ApplicationConfig;
import org.example.config.ProcessBuilderConfig;
import org.example.constant.Constants;
import org.example.constant.EventBusAddresses;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MetricPoller implements Runnable{

    private List<JsonObject> devices;
    private JsonObject discovery;
    private Process process;
    private long metricPollerTimeout = ApplicationConfig.METRIC_POLLER_TIMEOUT.value;
    private Vertx vertx;

    public MetricPoller(List<JsonObject> device, JsonObject discovery, long schedulerPeriod, Vertx vertx){
        this.devices = device;
        this.discovery = discovery;
        this.vertx = vertx;
        if(schedulerPeriod < metricPollerTimeout){
            throw new RuntimeException("Poller Scheduler Period should always more than Metric Poller Timeout");
        }
    }

    @Override
    public void run() {
        startDiscovery(devices,discovery);
    }


    public JsonObject startDiscovery(List<JsonObject> devices,JsonObject discovery){
        JsonObject jsonObject = null;
        try {
            String command = "go run /home/ashish/a4h-personal/Prototype/go-proto/myproject/main.go";
            ProcessBuilderConfig processBuilderConfig = new ProcessBuilderConfig(command);

            // Start the process
            process = processBuilderConfig.getAndStartProcess();

            JsonObject listOfdevices = new JsonObject();
            listOfdevices.put("devices",devices);

            // Send JSON to Go application
            sendData(process,listOfdevices,discovery);

            // Read JSON from GO
            jsonObject = readResponse(process);

            // waiting for process to complete
            process.waitFor();
        } catch (Exception e) {
            System.out.println("Unable to Start Discovery : " + e.getMessage());
        }
        return jsonObject;
    }


    public JsonObject readResponse(Process process){
        JsonObject jsonObject = new JsonObject();

        // current timestamp
        LocalDateTime timestamp = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTimestamp = timestamp.format(formatter);

        // Read the output from Go application (if any)
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {

                Boolean responseFromGo = metricPollerTimeout(reader,metricPollerTimeout,TimeUnit.SECONDS);

                if(responseFromGo == true){
                    String line;
                    List<JsonObject> listOfMetrics = new ArrayList<>();
                    while ((line = reader.readLine()) != null) {
                        JsonObject object = new JsonObject(line);
                        object.put("timestamp",formattedTimestamp);

                        updateMonitor(object);

                        listOfMetrics.add(object);
                        System.out.println(object);
                    }
                    System.out.println("------------------------------------------------");
                }
                else {
                    process.destroyForcibly();
                    System.out.println("Not able poll metric within given interval : " + metricPollerTimeout + " seconds");
                }
                jsonObject.put("status",responseFromGo);
                return jsonObject;
        } catch (IOException  e) {
            throw new RuntimeException(e);
        }
    }


    public void updateMonitor(JsonObject object){
        JsonObject updateMonitorDetails = new JsonObject();

        updateMonitorDetails.put(Constants.DAO_KEY,Constants.MONITOR_DAO_NAME);
        updateMonitorDetails.put("monitorId",object.getString("monitorId"));

        updateMonitorDetails.put("status",object.getString("status"));
        vertx.eventBus().send(EventBusAddresses.DATABASE_UPDATE,updateMonitorDetails);

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

}
