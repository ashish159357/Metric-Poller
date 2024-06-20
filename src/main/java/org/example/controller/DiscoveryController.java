package org.example.controller;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.example.constant.Constants;
import org.example.service.DiscoveryService;

public class DiscoveryController {

    private DiscoveryService discoveryService;

    public DiscoveryController(Router router, Vertx vertx){

        discoveryService = new DiscoveryService(vertx);

        router.post(Constants.baseUrl + "/discovery/create").handler(this::createDiscovery);

    }

    public void createDiscovery(RoutingContext routingContext) {

        // Get the JSON body from the request
        JsonObject body = routingContext.getBodyAsJson();

        if (body == null) {
            routingContext.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("error", "Invalid JSON body").encodePrettily());
            return;
        }

        discoveryService.createDiscovery(body,routingContext);
    }
}
