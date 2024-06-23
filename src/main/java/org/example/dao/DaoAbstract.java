package org.example.dao;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Tuple;
import org.example.enums.CredentialProfileEnum;

public abstract class DaoAbstract {

    public static PgPool client;

    public abstract void insertData(JsonObject data, Message<Object> message);
    public abstract void selectData(JsonObject data, Message<Object> message);

    public void insert(String query,Message<Object> message,Tuple tuple){
        client.preparedQuery(query)

                .execute(tuple, ar -> {

                    if (ar.succeeded()) {
                        message.reply("Data inserted successfully");

                    } else {

                        System.out.println("query : " + query);
                        System.out.println("tuple : " + tuple);
                        message.fail(500, ar.cause().getMessage());

                    }

                });
    }
}
