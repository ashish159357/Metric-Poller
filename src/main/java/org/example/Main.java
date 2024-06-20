package org.example;

import io.vertx.core.Vertx;
import org.example.vertical.database.DatabaseVerticle;
import org.example.vertical.webserver.WebServerVerticle;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        int port = 8080;
        vertx.deployVerticle(new WebServerVerticle(port));
        System.out.println("Web Server Started at : " + port);

        vertx.deployVerticle(new DatabaseVerticle());
    }
}