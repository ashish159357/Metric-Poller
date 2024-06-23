package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.example.config.ProcessBuilderConfig;
import org.example.constant.Constants;
import org.example.constant.EventBusAddresses;

import java.io.*;

public class DiscoveryService {

    private Vertx vertx;


    public DiscoveryService(Vertx vertx){
        this.vertx = vertx;
    }


    public void createDiscovery(JsonObject discovery, RoutingContext routingContext){
        discovery.put(Constants.DAO_KEY,Constants.DISCOVERY_DAO_NAME);

        vertx.eventBus().request(EventBusAddresses.DATABASE_INSERT,discovery,reply -> {
            if(reply.succeeded()){

                discovery.put(Constants.DAO_KEY,Constants.CREDENTIAL_PROFILE_DAO_NAME);
                vertx.eventBus().request(EventBusAddresses.DATABASE_SELECT_CREDENTIALPROIFILE, discovery, reply2 -> {
                    if (reply2.succeeded()) {
                        JsonObject credentialProfile = (JsonObject) reply2.result().body();
                        JsonObject jsonObject = startDiscovery(credentialProfile,discovery,routingContext);

//                        discovery.put("discovery_id",reply.result().body());
//                        discovery.put(Constants.DAO_KEY,Constants.MONITOR_DAO_NAME);
//                        vertx.eventBus().request(EventBusAddresses.DATABASE_INSERT,discovery,reply3 -> {
//                            if(reply3.succeeded()){
//                                System.out.println("Discovery Done Successfully");
//                            }else {
//                                System.out.println("Discovery Fail");
//                            }
//                        });
//                        routingContext.response().setStatusCode(500).end(String.valueOf(jsonObject));
                    } else {
//                        routingContext.response().setStatusCode(500).end(reply2.cause().getMessage());
                    }
                });

                routingContext.response().setStatusCode(500).end("Done");
            }else {
                routingContext.response().setStatusCode(500).end("fail");
            }
        });


    }

    public JsonObject startDiscovery(JsonObject credentialProfile,JsonObject discovery,RoutingContext routingContext){
        JsonObject jsonObject = null;
        try {
            String command = "go run /home/ashish/a4h-personal/Prototype/go-proto/myproject/main.go";
            ProcessBuilderConfig processBuilderConfig = new ProcessBuilderConfig(command);

            // Start the process
            Process process = processBuilderConfig.getAndStartProcess();

            // Send JSON to Go application
            sendData(process,credentialProfile,discovery);
            jsonObject = readResponse(process);

            // waiting for process to complete
            process.waitFor();
        } catch (Exception e) {
            routingContext.response().setStatusCode(500).end(e.getMessage());
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
            System.out.println("cpu = " + cpu + " , " + "memory = " + memory + " , " + "disk = " + disk);

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
