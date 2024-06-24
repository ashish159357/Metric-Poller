package org.example.dao;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Tuple;
import org.example.config.DataBaseConfig;

public class MonitorDao extends DaoAbstract{

    private static MonitorDao monitorDaoInstance = null;

    private String insertSql = "INSERT INTO monitor (discovery_id,hostname,protocol,username,password) VALUES ($1,$2,$3,$4,$5)";

    private MonitorDao(){
    }

    public static synchronized MonitorDao getInstance(Vertx vertx){
        if(monitorDaoInstance == null){
            client = DataBaseConfig.getClient(vertx);
            monitorDaoInstance = new MonitorDao();
        }
        return monitorDaoInstance;
    }

    @Override
    public void insertData(JsonObject data, Message<Object> message) {
        long discoveryId = Long.parseLong(data.getString("discoveryId"));
        String hostname = data.getString("ip");
        String protocol = data.getString("protocol");
        String username = data.getString("username");
        String password = data.getString("password");

        insert(insertSql,message, Tuple.of(discoveryId,hostname,protocol,username,password));
    }

    @Override
    public void selectData(JsonObject data, Message<Object> message) {

    }
}
