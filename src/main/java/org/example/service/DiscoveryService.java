package org.example.service;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.example.config.ApplicationConfig;
import org.example.constant.Constants;
import org.example.constant.EventBusAddresses;
import org.example.runnable.poller.MetricPoller;
import org.example.utils.IpRangeGenerator;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class DiscoveryService {

    private Vertx vertx;
    private final long schedulerPeriod = ApplicationConfig.SCHEDULER_PERIOD.value;
    private MonitorService monitorService;
    private ConcurrentHashMap<String,ScheduledExecutorService> metricPollerScheduler = new ConcurrentHashMap<>();

    public DiscoveryService(Vertx vertx){
        this.vertx = vertx;
        monitorService = new MonitorService(vertx);
    }


    public void createDiscovery(JsonObject discovery, RoutingContext routingContext){
        discovery.put(Constants.DAO_KEY,Constants.DISCOVERY_DAO_NAME);

        // request to insert discovery in database
        vertx.eventBus().request(EventBusAddresses.DATABASE_INSERT,discovery,reply -> {
            if(reply.succeeded()){
                JsonObject discoveryIdKeyValue = (JsonObject) reply.result().body();
                String discoveryId = discoveryIdKeyValue.getString("generatedId");

                discovery.put(Constants.DAO_KEY,Constants.CREDENTIAL_PROFILE_DAO_NAME);

                // request to get credential info
                vertx.eventBus().request(EventBusAddresses.DATABASE_SELECT_CREDENTIAL_PROIFILE, discovery, reply2 -> {
                    if (reply2.succeeded()) {
                        JsonObject device = (JsonObject) reply2.result().body();
                        device.put("discoveryId",discoveryId);

                        List<JsonObject> devices = getIpRanges(discovery,device);

                        // creating monitor with fail status
                        monitorService.createMonitor(devices);

                        // Scheduling Metric Poller for 5 seconds
                        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                        metricPollerScheduler.put(discoveryId,scheduler);
                        scheduler.scheduleAtFixedRate(new MetricPoller(devices,discovery,schedulerPeriod,vertx), 1, schedulerPeriod, TimeUnit.SECONDS);

                    } else {
                        System.out.println("Unable to get the Credential Info to start discovery");
                    }
                });

                routingContext.response().setStatusCode(500).end("Discovery Created Successfully");
            }else {
                routingContext.response().setStatusCode(500).end("Unable to Create Discovery");
            }
        });
    }


    public List<JsonObject> getIpRanges(JsonObject discovery,JsonObject device){
        List<JsonObject> devices = new ArrayList<>();
        if(discovery.getString("ipRange") == ""){
            devices.add(device);
        }else {
            try {
                List<String> ips = IpRangeGenerator.getAllIpsInRange(discovery.getString("ipRange"));
                for(String ip:ips){
                    JsonObject newDevice = new JsonObject(device.toString());
                    newDevice.put("ip",ip);
                    devices.add(newDevice);
                }
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
        return devices;
    }
}
