package org.example.vertical;

import com.google.common.collect.Lists;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;
import org.example.config.ApplicationConfig;
import org.example.constant.EventBusAddresses;
import org.example.runnable.poller.MetricPoller;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MetricPollerVerticle extends AbstractVerticle {

    private List<List<JsonObject>> listOfBatchDevices;
    private int batchSize = ApplicationConfig.METRIC_POLLER_BATCH_SIZE;
    private List<JsonObject> devices = new ArrayList<>(batchSize);

    public void start(Promise<Void> startPromise) throws Exception {
        Thread.currentThread().setName(Thread.currentThread().getName() + "-" + "MetricPollerVerticle");

        vertx.eventBus().consumer(EventBusAddresses.METRIC_POLLER, this::startPolling);

        vertx.setPeriodic(5000,id -> {
            if(listOfBatchDevices != null){
                for(List<JsonObject> batchDevices:listOfBatchDevices){

                    vertx.executeBlocking(promise -> {
                        try {
                            MetricPoller metricPoller = new MetricPoller();
                            metricPoller.setDevices(batchDevices);
                            metricPoller.run();
                            promise.complete();
                        } catch (Exception e) {
                            promise.fail(e);
                            log.error("Error during metric polling", e);
                        }
                    }, true, res -> {
                        if (res.failed()) {
                            log.error("Metric polling failed", res.cause());
                        }
                    });
                }
            }
        });
    }

    private void startPolling(Message<Object> message) {
        List<JsonObject> devices = ((JsonArray) message.body()).getList();
        this.devices.addAll(devices);
        listOfBatchDevices = bacthingDevices(this.devices);
        log.info("Number Batch Created : {}",listOfBatchDevices.size());
    }

    private List<List<JsonObject>> bacthingDevices(List<JsonObject> originalList){
        return Lists.partition(originalList, batchSize);
    }
}
