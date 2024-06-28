package org.example.controller;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.example.service.DiscoveryService;

public class DiscoveryController {

    private DiscoveryService discoveryService;
    private static final String BASE_URL = "/api/v1";
    private static final String CREATE_DISCOVERY = BASE_URL + "/discovery/create";

    public DiscoveryController(Router router, DiscoveryService discoveryService){
        this.discoveryService = discoveryService;
        router.post(CREATE_DISCOVERY).handler(this::createDiscovery);
    }

    public void createDiscovery(RoutingContext rc) {
        JsonObject body;
        try{
            body = rc.getBodyAsJson();
            if (body == null) {
                throw new IllegalArgumentException("Invalid JSON body");
            }

            discoveryService.createDiscovery(body).onComplete(result -> {
                if(result.succeeded()){
                    rc.response()
                            .setStatusCode(201)
                            .putHeader("content-type", "application/json")
                            .end(result.result().toString());
                }else {
                    rc.response()
                            .setStatusCode(500)
                            .putHeader("content-type", "application/json")
                            .end(new JsonObject().put("error", result.cause().getMessage()).encodePrettily());
                }
            });

        }catch (Exception e){
            rc.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("error", "Invalid JSON body").encodePrettily());
            return;
        }

    }
}
