package org.example.dao;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public abstract class DaoAbstract {

    public static PgPool client;

    public abstract void insertData(JsonObject data,Promise promise);
    public abstract void selectData(JsonObject data,Promise promise);
    public abstract void updateData(JsonObject data);


    public void insert(String query, Tuple tuple, Promise promise){
        client.preparedQuery(query)
                .execute(tuple, ar -> {
                    if (ar.succeeded()) {
                        JsonObject jsonObject = new JsonObject();
                        for (Row row : ar.result()) {
                                int generatedId = (int) row.getValue(row.getColumnName(0));
                                jsonObject.put("generatedId",generatedId);
                                promise.complete(jsonObject);
                        }
                    } else {
                        promise.fail(ar.cause().getMessage());
                    }
                });
    }


    public void update(String query,Tuple tuple){
        client.preparedQuery(query)
                .execute(tuple, ar -> {
                    if (ar.succeeded()) {
                    } else {
                        System.out.println(ar.cause().getMessage());
                    }
                });
    }


    public void select(String query,Tuple tuple,Promise promise){
        client.preparedQuery(query)
                .execute(tuple, res -> {
                    if (res.succeeded()) {
                        RowSet<Row> resultSet = res.result();
                        JsonObject jsonObject = new JsonObject();
                        for (Row row : resultSet) {
                            String username = row.getString("username");
                            String password = row.getString("password");
                            jsonObject.put("username",username)
                                    .put("password",password);
                            promise.complete(jsonObject);
                        }
                    } else {
                        promise.fail(res.cause().getMessage());
                    }
                });
    }
}
