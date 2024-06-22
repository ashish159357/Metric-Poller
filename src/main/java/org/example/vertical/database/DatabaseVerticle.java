package org.example.vertical.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.example.config.DataBaseConfig;
import org.example.constant.Constants;
import org.example.enums.CredentialProfileEnum;

public class DatabaseVerticle extends AbstractVerticle {

    private PgPool client;

    @Override
    public void start(Promise<Void> startPromise) {

        client = DataBaseConfig.getClient(vertx);

        vertx.eventBus().consumer(Constants.DATABASE_INSERT, message -> {

            JsonObject data = (JsonObject) message.body();

            insertData(data, message);

        });

        vertx.eventBus().consumer(Constants.DATABASE_SELECT_CREDENTIALPROIFILE, message -> {

            try {

                JsonObject data = (JsonObject) message.body();

                selectData(Long.parseLong(data.getString("credentialId")), message);

            }catch (Exception e){

                System.out.println(e.getMessage());

                message.fail(500,e.getMessage());

            }

        });

        startPromise.complete();

    }


    private void insertData(JsonObject data, Message<Object> message) {

        String credentialProfileName = data.getString("credentialProfileName");

        String username = data.getString("username");

        String password = data.getString("password");

        client.preparedQuery(CredentialProfileEnum.INSERT_CREDENTIAL_PROFILE.getQuery())

        .execute(Tuple.of(credentialProfileName,username,password), ar -> {

            if (ar.succeeded()) {

                message.reply("Data inserted successfully");

            } else {

                message.fail(500, ar.cause().getMessage());

            }

        });

    }

    private void selectData(long id, Message<Object> message) {

        client.preparedQuery(CredentialProfileEnum.SELECT_CREDENTIAL_PROFILE.getQuery())

                .execute(Tuple.of(id), res -> {

                    if (res.succeeded()) {

                        RowSet<Row> resultSet = res.result();

                        JsonObject jsonObject = new JsonObject();

                        for (Row row : resultSet) {

                            String username = row.getString("username");
                            String password = row.getString("password");

                            jsonObject.put("username",username)
                                    .put("password",password);
                        }

                        message.reply(jsonObject);

                    } else {
                        res.cause().printStackTrace();
                    }

                });
    }



    @Override
    public void stop() {
        client.close();
    }

}

