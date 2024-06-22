package org.example.vertical.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.example.config.DataBaseConfig;
import org.example.constant.Constants;
import org.example.constant.EventBusAddresses;
import org.example.dao.CredentialProfileDao;
import org.example.enums.CredentialProfileEnum;

public class DatabaseVerticle extends AbstractVerticle {

//    private PgPool client;

    @Override
    public void start(Promise<Void> startPromise) {

//        client = DataBaseConfig.getClient(vertx);

        vertx.eventBus().consumer(EventBusAddresses.DATABASE_INSERT, message -> {

            JsonObject data = (JsonObject) message.body();

            CredentialProfileDao credentialProfileDao = CredentialProfileDao.getInstance(vertx);

            credentialProfileDao.insertData(data, message);

        });

        vertx.eventBus().consumer(EventBusAddresses.DATABASE_SELECT_CREDENTIALPROIFILE, message -> {

            try {

                JsonObject data = (JsonObject) message.body();
                CredentialProfileDao credentialProfileDao = CredentialProfileDao.getInstance(vertx);

                credentialProfileDao.selectData(data, message);

            }catch (Exception e){

                System.out.println(e.getMessage());

                message.fail(500,e.getMessage());

            }

        });

        startPromise.complete();
    }

    @Override
    public void stop() {
        DataBaseConfig.getClient(Vertx.vertx()).close();
    }
}

