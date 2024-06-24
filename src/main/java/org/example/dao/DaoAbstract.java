package org.example.dao;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

public abstract class DaoAbstract {

    public static PgPool client;

    public abstract void insertData(JsonObject data, Message<Object> message);
    public abstract void selectData(JsonObject data, Message<Object> message);

    public void insert(String query,Message<Object> message,Tuple tuple){
        client.preparedQuery(query)
                .execute(tuple, ar -> {
                    if (ar.succeeded()) {
                        JsonObject jsonObject = new JsonObject();
                        for (Row row : ar.result()) {
                                int generatedId = (int) row.getValue(row.getColumnName(0));
                                jsonObject.put("generatedId",generatedId);
                        }

                        message.reply(jsonObject);
                    } else {
                        System.out.println(ar.cause().getMessage());
                        message.fail(500, ar.cause().getMessage());
                    }
                });
    }
}
