package org.example.service;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.example.constant.Constants;
import org.example.constant.EventBusAddresses;

public class CredentialProfileService {

    private Vertx vertx;

    public CredentialProfileService(Vertx vertx){
        this.vertx = vertx;
    }

    public void createCredentialProfile(JsonObject credentialProfile, RoutingContext rc){

        vertx.eventBus().request(EventBusAddresses.DATABASE_INSERT, credentialProfile, reply -> {

            if (reply.succeeded()) {

                rc.response().setStatusCode(200).end(reply.result().body().toString());

            } else {

                rc.response().setStatusCode(500).end(reply.cause().getMessage());

            }
        });
    }
}
