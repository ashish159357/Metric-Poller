package org.example.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.example.config.ApplicationConfig;
import org.example.constant.Constants;
import org.example.constant.EventBusAddresses;
import org.example.runnable.poller.MetricPoller;
import org.example.utils.IpRangeGenerator;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

public class DiscoveryService {

    private Vertx vertx;
    private final long schedulerPeriod = ApplicationConfig.SCHEDULER_PERIOD.value;
    private MonitorService monitorService;
    private ConcurrentHashMap<String, ScheduledExecutorService> metricPollerScheduler = new ConcurrentHashMap<>();

    public DiscoveryService(Vertx vertx){
        this.vertx = vertx;
        monitorService = new MonitorService(vertx);
    }


    public Future<String> createDiscovery(JsonObject discovery){
        Promise<String> promise = Promise.promise();

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
                        monitorService.createMonitor(devices).onComplete(isMonitorCreatedPromise -> {
                                if(isMonitorCreatedPromise.succeeded()){
                                    vertx.executeBlocking(promiseBlocking -> {
                                        MetricPoller metricPoller = new MetricPoller(devices,discovery,schedulerPeriod);
                                        List<JsonObject> jsonObjects = startDiscovery(metricPoller);
                                        metricPoller.setDevices(jsonObjects);

                                        // Scheduling Metric Poller for 5 seconds
                                        vertx.setPeriodic(2000,id -> {
                                            vertx.executeBlocking(promise1 -> {
                                                metricPoller.run();
                                            });
                                        });
                                    },false);
                                }
                            });
                        } else {
                            System.out.println("Unable to get the Credential Info to start discovery");
                        }
                    });

                    promise.complete("Discovery Created Successfully");
                }else {
                    promise.fail("Unable to Create Discovery");
                }
            });

        return promise.future();
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


    public List<JsonObject> startDiscovery(MetricPoller metricPoller){
        List<JsonObject> metrics = metricPoller.run();
        List<JsonObject> devices = metricPoller.getDevices();
        List<String> monitorIds = new ArrayList<>();

        // filter out all monitor ids having status success
        List<JsonObject> filteredDevices = new ArrayList<>();
        for(int i=0 ; i < metrics.size() ; i++){
            if(!metrics.get(i).getString("status").equals("fail")){
                monitorIds.add(metrics.get(i).getString("monitorId"));
            }
        }

        // update monitor status in database
        for(JsonObject device:devices){
            if(monitorIds.contains(device.getString("monitorId"))){
                filteredDevices.add(device);
                device.put("status","success");
                monitorService.updateMonitor(device);
            }
        }
        return filteredDevices;
    }
}
