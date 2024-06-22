package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.example.config.ProcessBuilderConfig;
import org.example.constant.Constants;
import org.example.constant.EventBusAddresses;
import org.example.model.CredentialProfile;
import org.example.model.Discovery;

import java.io.*;
import java.util.Map;

public class DiscoveryService {

    private Vertx vertx;

    public DiscoveryService(Vertx vertx){
        this.vertx = vertx;
    }

    public void createDiscovery(JsonObject dicovery, RoutingContext routingContext){

        vertx.eventBus().request(EventBusAddresses.DATABASE_SELECT_CREDENTIALPROIFILE, dicovery, reply -> {

            if (reply.succeeded()) {

                JsonObject credentialProfile = (JsonObject) reply.result().body();

                try {

                    String command = "go run /home/ashish/a4h-personal/Prototype/go-proto/myproject/main.go";

                    ProcessBuilderConfig processBuilderConfig = new ProcessBuilderConfig(command);

                    // Start the process
                    Process process = processBuilderConfig.getAndStartProcess();

                    // Send JSON to Go application
                    sendData(process,credentialProfile,dicovery);

                    JsonObject jsonObject = readResponse(process);

                    System.out.println("Waiting for process to complete......");

                    process.waitFor();

                    routingContext.response().setStatusCode(500).end(String.valueOf(jsonObject));

                } catch (Exception e) {
                    routingContext.response().setStatusCode(500).end(e.getMessage());
                }
            } else {
                routingContext.response().setStatusCode(500).end(reply.cause().getMessage());
            }
            System.out.println("Process is Completed Successfully");
        });
    }


    public JsonObject readResponse(Process process){

        // Read the output from Go application (if any)
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        try {

            String cpu = reader.readLine();

            String memory = reader.readLine();

            String disk = reader.readLine();

            JsonObject jsonObject = new JsonObject();

            jsonObject.put("cpu",cpu);

            jsonObject.put("memory",memory);

            jsonObject.put("disk",disk);

            System.out.println(cpu + " " + memory + " " + disk);

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
