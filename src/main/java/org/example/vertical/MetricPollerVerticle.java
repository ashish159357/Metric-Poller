package org.example.vertical;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;
import org.example.config.ApplicationConfig;
import org.example.constant.EventBusAddresses;
import org.example.runnable.poller.MetricPoller;
import org.example.utils.DateUtils;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class MetricPollerVerticle extends AbstractVerticle {

    private ConcurrentHashMap<Long,List<JsonObject>> longListConcurrentHashMap = new ConcurrentHashMap<>();
    private long pollingTime = ApplicationConfig.POLLING_TIME;

    public void start(Promise<Void> startPromise) throws Exception {
        Thread.currentThread().setName(Thread.currentThread().getName() + "-" + "MetricPollerVerticle");

        vertx.eventBus().consumer(EventBusAddresses.METRIC_POLLER, this::startPolling);

        vertx.setPeriodic(ApplicationConfig.SCHEDULER_PERIOD,id -> {

            for(long key:longListConcurrentHashMap.keySet()){

                if((key + pollingTime) <= DateUtils.getCurrentEpochValue()){
                   List<JsonObject> batchDevices = longListConcurrentHashMap.get(key);
                   longListConcurrentHashMap.remove(key);
                   longListConcurrentHashMap.put(key + pollingTime,batchDevices);

                    log.info("Metric poller start for -> " + key);
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
        longListConcurrentHashMap.put(DateUtils.getCurrentEpochValue(),devices);
    }

}
