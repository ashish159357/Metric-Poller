package org.example.dao;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Tuple;
import org.example.config.DataBaseConfig;

public class DiscoveryDao extends DaoAbstract {

    private static DiscoveryDao discoveryDaoInstance = null;
    private String insertQuery = "INSERT INTO discovery (credential_id,type,hostname,protocol) VALUES ($1,$2,$3,$4)";

    private DiscoveryDao(){
    }

    public static synchronized DiscoveryDao getInstance(Vertx vertx){
        if(discoveryDaoInstance == null){
            client = DataBaseConfig.getClient(vertx);
            discoveryDaoInstance = new DiscoveryDao();
        }
        return discoveryDaoInstance;
    }

    @Override
    public void insertData(JsonObject data, Message<Object> message) {
        String host = data.getString("ip");
        long credentialId = Long.parseLong(data.getString("credentialId"));
        String type = data.getString("type");
        String hostname = data.getString("hostname");
        String protocol = data.getString("protocol");
        insert(insertQuery,message, Tuple.of(credentialId,type,host,protocol));
    }

    @Override
    public void selectData(JsonObject data, Message<Object> message) {

    }
}
