package org.example.config;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

public class DataBaseConfig {

    public static PgPool getClient(Vertx vertx){
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(5432)
                .setHost("localhost")
                .setDatabase("mydb")
                .setUser("postgres")
                .setPassword("root");

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5);

        return PgPool.pool(vertx, connectOptions, poolOptions);
    }

}
