package org.example.vertical.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.example.config.DataBaseConfig;
import org.example.constant.Constants;
import org.example.constant.EventBusAddresses;
import org.example.dao.CredentialProfileDao;
import org.example.dao.DaoAbstract;
import org.example.dao.DiscoveryDao;
import org.example.dao.MonitorDao;

public class DatabaseVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.eventBus().consumer(EventBusAddresses.DATABASE_INSERT,this::insertData);
        vertx.eventBus().consumer(EventBusAddresses.DATABASE_SELECT_CREDENTIALPROIFILE, this::getdata);
        startPromise.complete();
    }


    public void insertData(Message<Object> message){
        JsonObject data = (JsonObject) message.body();
        DaoAbstract daoInterface = null;

        if(data.getString(Constants.DAO_KEY) == Constants.CREDENTIAL_PROFILE_DAO_NAME){
            daoInterface = CredentialProfileDao.getInstance(vertx);
        } else if (data.getString(Constants.DAO_KEY) == Constants.DISCOVERY_DAO_NAME) {
            daoInterface = DiscoveryDao.getInstance(vertx);
        } else if (data.getString(Constants.DAO_KEY) == Constants.MONITOR_DAO_NAME) {
            daoInterface = MonitorDao.getInstance(vertx);
        }

        daoInterface.insertData(data, message);
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
        DataBaseConfig.getClient(Vertx.vertx()).close();
    }
}

