package org.example.runnable.poller;

import io.vertx.core.json.JsonObject;
import org.example.config.ProcessBuilderConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MetricPoller implements Runnable{

    private JsonObject credentialProfile;
    private JsonObject discovery;

    public MetricPoller(JsonObject credentialProfile,JsonObject discovery){
        this.credentialProfile = credentialProfile;
        this.discovery = discovery;
    }

    @Override
    public void run() {
        startDiscovery(credentialProfile,discovery);
    }

    public JsonObject startDiscovery(JsonObject credentialProfile,JsonObject discovery){
        JsonObject jsonObject = null;
        try {
            String command = "go run /home/ashish/a4h-personal/Prototype/go-proto/myproject/main.go";
            ProcessBuilderConfig processBuilderConfig = new ProcessBuilderConfig(command);

            // Start the process
            Process process = processBuilderConfig.getAndStartProcess();

            // Send JSON to Go application
            sendData(process,credentialProfile,discovery);

            // Read JSON from GO
            jsonObject = readResponse(process);

            // waiting for process to complete
            process.waitFor();
        } catch (Exception e) {
            System.out.println("Unable to Start Discovery");
        }
        return jsonObject;
    }


    public JsonObject readResponse(Process process){

        // Read the output from Go application (if any)
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {
            String cpu = reader.readLine();
            String memory = reader.readLine();
            String disk = reader.readLine();

            // creating jsonobject
            JsonObject jsonObject = new JsonObject();
            jsonObject.put("cpu",cpu);
            jsonObject.put("memory",memory);
            jsonObject.put("disk",disk);

            // current timestamp
            LocalDateTime timestamp = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTimestamp = timestamp.format(formatter);

            System.out.println(formattedTimestamp + " -> " + "cpu = " + cpu + " , " + "memory = " + memory + " , " + "disk = " + disk);

            return jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void sendData(Process process, JsonObject credentialProfile, JsonObject discovery){

        // Send JSON to Go application
        OutputStream os = process.getOutputStream();
        credentialProfile.put("ip",discovery.getString("ip"));
        try {
            os.write(credentialProfile.toString().getBytes());
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
