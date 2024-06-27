package org.example.dao;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Tuple;
import org.example.config.DataBaseConfig;

public class CredentialProfileDao extends DaoAbstract {

    private String insertQuery = "INSERT INTO credential_profile (credentialProfileName,username,password) VALUES ($1,$2,$3) returning credentialprofileid";
    private String selectQuery = "SELECT * from credential_profile where credentialprofileid=$1";

    private static CredentialProfileDao credentialProfileDaoInstance = null;

    private CredentialProfileDao(){}

    public static synchronized CredentialProfileDao getInstance(Vertx vertx){
        if(credentialProfileDaoInstance == null){
            client = DataBaseConfig.getClient(vertx);
            credentialProfileDaoInstance = new CredentialProfileDao();
        }
        return credentialProfileDaoInstance;
    }

    public void insertData(JsonObject data, Promise promise) {

        String credentialProfileName = data.getString("credentialProfileName");
        String username = data.getString("username");
        String password = data.getString("password");
        insert(insertQuery,Tuple.of(credentialProfileName,username,password),promise);
    }


    public void selectData(JsonObject data,Promise promise) {

        long id = Long.parseLong(data.getString("credentialId"));
        select(selectQuery,Tuple.of(id),promise);
    }

    @Override
    public void updateData(JsonObject data) {}
}
