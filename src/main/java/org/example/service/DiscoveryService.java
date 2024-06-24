package org.example.service;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.example.constant.Constants;
import org.example.constant.EventBusAddresses;
import org.example.runnable.poller.MetricPoller;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class DiscoveryService {

    private Vertx vertx;


    public DiscoveryService(Vertx vertx){
        this.vertx = vertx;
    }


    public void createDiscovery(JsonObject discovery, RoutingContext routingContext){
        discovery.put(Constants.DAO_KEY,Constants.DISCOVERY_DAO_NAME);

        // request to insert discovery in database
        vertx.eventBus().request(EventBusAddresses.DATABASE_INSERT,discovery,reply -> {
            if(reply.succeeded()){

                discovery.put(Constants.DAO_KEY,Constants.CREDENTIAL_PROFILE_DAO_NAME);

                // request to get credential info
                vertx.eventBus().request(EventBusAddresses.DATABASE_SELECT_CREDENTIALPROIFILE, discovery, reply2 -> {
                    if (reply2.succeeded()) {
                        JsonObject credentialProfile = (JsonObject) reply2.result().body();

                        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                        scheduler.scheduleAtFixedRate(new MetricPoller(credentialProfile,discovery), 1, 5, TimeUnit.SECONDS);
                    } else {
                        System.out.println("Unable to get the Credential Info to start discovery");
                    }
                });

                routingContext.response().setStatusCode(500).end("Discovery Done Successfully");
            }else {
                routingContext.response().setStatusCode(500).end("Discovery Failed");
            }
        });
    }
}
