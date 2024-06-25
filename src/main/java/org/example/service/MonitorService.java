package org.example.service;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.example.constant.Constants;
import org.example.constant.EventBusAddresses;

import java.util.List;

public class MonitorService {

    private Vertx vertx;

    public MonitorService(Vertx vertx){
        this.vertx = vertx;
    }

    public void createMonitor(List<JsonObject> devices){
        for(JsonObject device: devices){
            device.put(Constants.DAO_KEY,Constants.MONITOR_DAO_NAME);
            vertx.eventBus().request(EventBusAddresses.DATABASE_INSERT,device, reply->{
                if(reply.succeeded()){
                    JsonObject id = (JsonObject) reply.result().body();
                    device.put("monitorId",id.getString("generatedId"));
                }
            });
        }
    }
}
