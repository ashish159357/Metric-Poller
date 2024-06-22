package org.example.service;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.example.constant.Constants;

public class CredentialProfileService {

    private Vertx vertx;

    public CredentialProfileService(Vertx vertx){
        this.vertx = vertx;
    }

    public void createCredentialProfile(JsonObject body, RoutingContext rc){

        vertx.eventBus().request(Constants.DATABASE_INSERT, body, reply -> {

            if (reply.succeeded()) {

                rc.response().setStatusCode(200).end(reply.result().body().toString());

            } else {

                rc.response().setStatusCode(500).end(reply.cause().getMessage());

            }
        });
    }
}
