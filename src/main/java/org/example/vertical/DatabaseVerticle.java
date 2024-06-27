package org.example.vertical;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.example.config.DataBaseConfig;
import org.example.constant.Constants;
import org.example.constant.EventBusAddresses;
import org.example.dao.CredentialProfileDao;
import org.example.dao.DaoAbstract;
import org.example.dao.DiscoveryDao;
import org.example.dao.MonitorDao;

@Slf4j
public class DatabaseVerticle extends AbstractVerticle {

    private static final String EVENT_LOOP_THREAD_PREFIX = "vert.x-eventloop-thread-DatabaseVerticle";


    @Override
    public void start(Promise<Void> startPromise) {
        Thread.currentThread().setName(EVENT_LOOP_THREAD_PREFIX);
        vertx.eventBus().consumer(EventBusAddresses.DATABASE_INSERT, this::insertData);
        vertx.eventBus().consumer(EventBusAddresses.DATABASE_SELECT_CREDENTIAL_PROIFILE, this::selectData);
        vertx.eventBus().consumer(EventBusAddresses.DATABASE_UPDATE, this::updateData);
        startPromise.complete();
    }


    private void updateData(Message<Object> message) {
        vertx.executeBlocking(promise -> {
            processData((JsonObject) message.body(), "updateData",promise).onComplete(result -> {
                if(result.succeeded()){
                    message.reply(result.result().toString());
                }
                else {
                    message.reply(result.cause().getMessage());
                }
            });
        },false);
    }


    private void insertData(Message<Object> message) {
        vertx.executeBlocking(promise -> {
            JsonObject jsonObject = (JsonObject) message.body();
            processData(jsonObject, "insertData",promise).onComplete(result -> {
                if(result.succeeded()){
                    message.reply(result.result());
                }
                else {
                    message.fail(500,result.cause().getMessage());
                }
            });
        },false);
    }


    private void selectData(Message<Object> message) {
        vertx.executeBlocking(promise -> {
            processData((JsonObject) message.body(), "selectData",promise).onComplete(result -> {
                if(result.succeeded()){
                    message.reply(result.result());
                }
                else {
                    message.reply(result.cause().getMessage());
                }
            });
        },false);
    }


    private Future<String> processData(JsonObject data, String operation,Promise promise) {

        try {
            DaoAbstract dao = getDaoInstance(data);
            if (dao != null) {
                switch (operation) {
                    case "updateData":
                        dao.updateData(data);
                        promise.complete("Updated Successfully");
                        break;
                    case "insertData":
                        dao.insertData(data,promise);
                        break;
                    case "selectData":
                        dao.selectData(data,promise);
                        break;
                    default:
                        log.error("Unknown operation: {}", operation);
                        promise.fail("Unknown operation");
                }
            } else {
                log.error("No DAO instance found for key: {}", data.getString(Constants.DAO_KEY));
                promise.fail("No DAO instance found for key: " + data.getString(Constants.DAO_KEY));
            }
        } catch (Exception e) {
            log.error("Error processing data: {}", e.getMessage());
            promise.fail(e.getMessage());
        }

        return promise.future();
    }


    private DaoAbstract getDaoInstance(JsonObject data) {
        String daoKey = data.getString(Constants.DAO_KEY);
        switch (daoKey) {
            case Constants.CREDENTIAL_PROFILE_DAO_NAME:
                return CredentialProfileDao.getInstance(vertx);
            case Constants.DISCOVERY_DAO_NAME:
                return DiscoveryDao.getInstance(vertx);
            case Constants.MONITOR_DAO_NAME:
                return MonitorDao.getInstance(vertx);
            default:
                log.warn("Unknown DAO key: {}", daoKey);
                return null;
        }
    }


    @Override
    public void stop() {
        DataBaseConfig.getClient(vertx).close();
        log.info("DatabaseVerticle stopped and database client closed.");
    }
}
