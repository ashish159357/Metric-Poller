package org.example.dao;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.example.config.DataBaseConfig;
import org.example.enums.CredentialProfileEnum;

public class CredentialProfileDao {

    private static PgPool client;

    private static CredentialProfileDao credentialProfileDaoInstance = null;

    private CredentialProfileDao(){
    }

    public static synchronized CredentialProfileDao getInstance(Vertx vertx){
        if(credentialProfileDaoInstance == null){
            client = DataBaseConfig.getClient(vertx);
            credentialProfileDaoInstance = new CredentialProfileDao();
        }
        return credentialProfileDaoInstance;
    }

    public void insertData(JsonObject data, Message<Object> message) {

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


    public void selectData(JsonObject data, Message<Object> message) {

        long id = Long.parseLong(data.getString("credentialId"));

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
}
