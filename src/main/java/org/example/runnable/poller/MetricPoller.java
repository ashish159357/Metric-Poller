package org.example.runnable.poller;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.example.config.ProcessBuilderConfig;
import org.example.constant.Constants;
import org.example.constant.EventBusAddresses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class MetricPoller implements Runnable{

    private JsonObject device;
    private JsonObject discovery;
    private Process process;
    private long metricPollerTimeout = 4;
    private boolean monitorStatus;
    private Vertx vertx;

    public MetricPoller(JsonObject device, JsonObject discovery, long schedulerPeriod, Vertx vertx,boolean monitorStatus){
        this.device = device;
        this.discovery = discovery;
        this.vertx = vertx;
        this.monitorStatus = monitorStatus;
        if(schedulerPeriod < metricPollerTimeout){
            throw new RuntimeException("Poller Scheduler Period should always more than Metric Poller Timeout");
        }
    }

    @Override
    public void run() {
        startDiscovery(device,discovery);
    }

    public JsonObject startDiscovery(JsonObject device,JsonObject discovery){
        JsonObject jsonObject = null;
        try {
            String command = "go run /home/ashish/a4h-personal/Prototype/go-proto/myproject/main.go";
            ProcessBuilderConfig processBuilderConfig = new ProcessBuilderConfig(command);

            // Start the process
            process = processBuilderConfig.getAndStartProcess();

            // Send JSON to Go application
            sendData(process,device,discovery);

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

                Boolean status = metricPollerTimeout(reader,metricPollerTimeout,TimeUnit.SECONDS);

                if(status == true){
                    String cpu = reader.readLine();
                    String memory = reader.readLine();
                    String disk = reader.readLine();

                    // creating jsonobject
                    jsonObject.put("cpu",cpu);
                    jsonObject.put("memory",memory);
                    jsonObject.put("disk",disk);
                    System.out.println(formattedTimestamp + " -> " + "cpu = " + cpu + " , " + "memory = " + memory + " , " + "disk = " + disk);

                    if(monitorStatus == false){
                        device.put(Constants.DAO_KEY,Constants.MONITOR_DAO_NAME);
                        vertx.eventBus().send(EventBusAddresses.DATABASE_INSERT,device);
                        monitorStatus = true;
                    }
                }
                else {
                    process.destroyForcibly();
                    System.out.println("Status : " + status);
                }

                jsonObject.put("status",status);
                return jsonObject;
        } catch (IOException  e) {
            throw new RuntimeException(e);
        }
    }


    public void sendData(Process process, JsonObject device, JsonObject discovery){

        // Send JSON to Go application
        OutputStream os = process.getOutputStream();
        device.put("ip",discovery.getString("ip"));
        try {
            os.write(device.toString().getBytes());
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
