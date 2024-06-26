package org.example.vertical;

import io.vertx.core.AbstractVerticle;
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

    String eventLoopThreadPrefix = "vert.x-eventloop-thread";

    @Override
    public void start(Promise<Void> startPromise) {

        Thread.currentThread().setName(eventLoopThreadPrefix + "-" + "DatabaseVerticle");

        vertx.eventBus().consumer(EventBusAddresses.DATABASE_INSERT,this::insertData);
        vertx.eventBus().consumer(EventBusAddresses.DATABASE_SELECT_CREDENTIALPROIFILE, this::getdata);
        vertx.eventBus().consumer(EventBusAddresses.DATABASE_UPDATE,this::updateData);
        startPromise.complete();
    }

    private void updateData(Message<Object> message) {
        JsonObject data = (JsonObject) message.body();
        DaoAbstract daoAbstract = getDaoInstance(data);
        daoAbstract.updateData(data,message);
    }


    public void insertData(Message<Object> message){
        JsonObject data = (JsonObject) message.body();
        DaoAbstract daoAbstract = getDaoInstance(data);
        daoAbstract.insertData(data, message);
    }

    public DaoAbstract getDaoInstance(JsonObject data){
        DaoAbstract daoAbstract = null;

        if(data.getString(Constants.DAO_KEY) == Constants.CREDENTIAL_PROFILE_DAO_NAME){
            daoAbstract = CredentialProfileDao.getInstance(vertx);
        } else if (data.getString(Constants.DAO_KEY) == Constants.DISCOVERY_DAO_NAME) {
            daoAbstract = DiscoveryDao.getInstance(vertx);
        } else if (data.getString(Constants.DAO_KEY) == Constants.MONITOR_DAO_NAME) {
            daoAbstract = MonitorDao.getInstance(vertx);
        }

        return daoAbstract;
    }


    public void getdata(Message<Object> message){
        try {
            JsonObject data = (JsonObject) message.body();
            DaoAbstract daoInterface = null;
            if(data.getString(Constants.DAO_KEY) == Constants.CREDENTIAL_PROFILE_DAO_NAME){
                daoInterface = CredentialProfileDao.getInstance(vertx);
            }
            daoInterface.selectData(data, message);
        }catch (Exception e){
            System.out.println(e.getMessage());
            message.fail(500,e.getMessage());
        }
    }


    @Override
    public void stop() {
        DataBaseConfig.getClient(vertx).close();
    }
}

