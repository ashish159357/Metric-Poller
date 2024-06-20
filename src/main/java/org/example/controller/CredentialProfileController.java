package org.example.controller;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.example.constant.Constants;
import org.example.service.CredentialProfileService;

public class CredentialProfileController {

    private CredentialProfileService credentialProfileService;

    public CredentialProfileController(Router router,Vertx vertx){

        credentialProfileService = new CredentialProfileService(vertx);

        router.post(Constants.baseUrl + "/credential-profile/create").handler(this::createCredentialProfile);

    }

    public void createCredentialProfile(RoutingContext rc){

        // Get the JSON body from the request
        JsonObject body = rc.getBodyAsJson();

        if (body == null) {
            rc.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("error", "Invalid JSON body").encodePrettily());
            return;
        }

        credentialProfileService.createCredentialProfile(body,rc);
    }
}
