package org.example.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.example.constant.Constants;
import org.example.constant.EventBusAddresses;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class MonitorService {

    private Vertx vertx;

    public MonitorService(Vertx vertx){
        this.vertx = vertx;
    }

    public Future<Boolean> createMonitor(List<JsonObject> devices) {
        Promise<Boolean> promise = Promise.promise();
        int totalDevices = devices.size();
        AtomicInteger counter = new AtomicInteger(0);

        for (JsonObject device : devices) {
            device.put(Constants.DAO_KEY, Constants.MONITOR_DAO_NAME);
            vertx.eventBus().request(EventBusAddresses.DATABASE_INSERT, device, reply -> {
                if (reply.succeeded()) {
                    JsonObject id = (JsonObject) reply.result().body();
                    device.put("monitorId", id.getString("generatedId"));
                } else {
                    // Handle failure scenario if needed
                }
                // Increment counter and check if all requests are done
                if (counter.incrementAndGet() == totalDevices) {
                    promise.complete(true);
                }
            });
        }

        // In case the devices list is empty
        if (totalDevices == 0) {
            promise.complete(true);
        }

        return promise.future();
    }



    public void updateMonitor(JsonObject object){
        JsonObject updateMonitorDetails = new JsonObject();

        updateMonitorDetails.put(Constants.DAO_KEY,Constants.MONITOR_DAO_NAME);
        updateMonitorDetails.put("monitorId",object.getString("monitorId"));

        updateMonitorDetails.put("status",object.getString("status"));
        vertx.eventBus().send(EventBusAddresses.DATABASE_UPDATE,updateMonitorDetails);

    }

}
