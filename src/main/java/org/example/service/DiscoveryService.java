package org.example.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.example.constant.Constants;
import org.example.model.Discovery;
import org.example.utils.SSHUtil;

public class DiscoveryService {

    private Vertx vertx;

    public DiscoveryService(Vertx vertx){
        this.vertx = vertx;
    }

    public void createDiscovery(JsonObject body, RoutingContext routingContext){

        vertx.eventBus().request(Constants.DATABASE_SELECT_CREDENTIALPROIFILE, body, reply -> {

            if (reply.succeeded()) {

                JsonObject credentialProfile = (JsonObject) reply.result().body();

                if(body.getString("protocol").equals("SSH")){
                    int status = SSHUtil.executeCommand(credentialProfile.getString("username"),body.getString("ip"),credentialProfile.getString("password"),"ls");
                    if(status == 0){
                        routingContext.response().setStatusCode(200).end("Discovery Done Successfully");
                    }
                }


            } else {

                routingContext.response().setStatusCode(500).end(reply.cause().getMessage());

            }

        });

    }
}
