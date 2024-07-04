package org.example.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.example.config.ApplicationConfig;
import org.example.constant.Constants;
import org.example.constant.EventBusAddresses;
import org.example.runnable.poller.MetricPoller;
import org.example.utils.IpRangeGenerator;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class DiscoveryService {

    private Vertx vertx;
    private final long schedulerPeriod = ApplicationConfig.SCHEDULER_PERIOD;
    private MonitorService monitorService;
    private ConcurrentHashMap<String, Long> metricPollerScheduler = new ConcurrentHashMap<>();

    public DiscoveryService(Vertx vertx){
        this.vertx = vertx;
        monitorService = new MonitorService(vertx);
    }


    public Future<String> createDiscovery(JsonObject discovery){
        Promise<String> promise = Promise.promise();

        discovery.put(Constants.DAO_KEY,Constants.DISCOVERY_DAO_NAME);

        // request to insert discovery in database
        log.info("Request to create discovery .....");
        vertx.eventBus().request(EventBusAddresses.DATABASE_INSERT,discovery,reply -> {
            if(reply.succeeded()){
                log.info("Successfully created discovery .....");
                JsonObject discoveryIdKeyValue = (JsonObject) reply.result().body();
                String discoveryId = discoveryIdKeyValue.getString("generatedId");

                discovery.put(Constants.DAO_KEY,Constants.CREDENTIAL_PROFILE_DAO_NAME);

                // request to get credential info
                log.info("Request to get Credentials .....");
                vertx.eventBus().request(EventBusAddresses.DATABASE_SELECT_CREDENTIAL_PROIFILE, discovery, reply2 -> {
                    if (reply2.succeeded()) {

                        log.info("Successfully get Credentials .....");

                        JsonObject device = (JsonObject) reply2.result().body();
                        device.put("discoveryId",discoveryId);

                        List<JsonObject> devices = getIpRanges(discovery,device);

                        // creating monitor with fail status
                        monitorService.createMonitor(devices).onComplete(isMonitorCreatedPromise -> {
                            if(isMonitorCreatedPromise.succeeded()){

                                    log.info("Using Worker Thread for discovery devices");

                                    // Running in worker Thread
                                    vertx.executeBlocking(promiseBlocking -> {

                                        MetricPoller metricPoller = new MetricPoller(devices);

                                        log.info("Starting Discovery .....");

                                        // start discovery and return list successfully discovered devices
                                        List<JsonObject> jsonObjects = startDiscovery(metricPoller);

                                        vertx.eventBus().send(EventBusAddresses.METRIC_POLLER, new JsonArray(jsonObjects));

                                    },false);

                                }
                            });
                        } else {
                            log.info("Unable to get the Credential Info to start discovery");
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
        for(int i=0 ; i < metrics.size() ; i++){
            if(!metrics.get(i).getString("status").equals("fail")){
                monitorIds.add(metrics.get(i).getString("monitorId"));
            }
        }

        // update monitor status in database
        List<JsonObject> filteredDevices = new ArrayList<>();
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
