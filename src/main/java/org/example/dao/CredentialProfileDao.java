package org.example.dao;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.example.config.DataBaseConfig;

public class CredentialProfileDao extends DaoAbstract {

    private String insertQuery = "INSERT INTO credential_profile (credentialProfileName,username,password) VALUES ($1,$2,$3)";
    private String selectQuery = "SELECT * from credential_profile where credentialprofileid=$1";

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

        insert(insertQuery,message,Tuple.of(credentialProfileName,username,password));
    }


    public void selectData(JsonObject data, Message<Object> message) {

        long id = Long.parseLong(data.getString("credentialId"));

        client.preparedQuery(selectQuery)

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
    public void updateData(JsonObject data, Message<Object> message) {

    }
}
