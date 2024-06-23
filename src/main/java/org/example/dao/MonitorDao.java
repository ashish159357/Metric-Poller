package org.example.dao;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Tuple;
import org.example.config.DataBaseConfig;

public class MonitorDao extends DaoAbstract{

    private static MonitorDao monitorDaoInstance = null;

    private String insertSql = "INSERT INTO monitor (discovery_id,type,hostname,protocol) VALUES ($1,$2,$3,$4)";

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
        String discoveryId = data.getString("discoveryId");
        String type = data.getString("type");
        String hostname = data.getString("hostname");
        String protocol = data.getString("protocol");
        insert(insertSql,message, Tuple.of(discoveryId,type,hostname,protocol));
    }

    @Override
    public void selectData(JsonObject data, Message<Object> message) {

    }
}
