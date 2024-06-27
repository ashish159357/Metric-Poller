package org.example.controller;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.service.CredentialProfileService;

@Slf4j
public class CredentialProfileController {

    private final CredentialProfileService credentialProfileService;
    private static final String BASE_URL = "/api/v1";
    private static final String CREATE_CREDENTIAL_PROFILE = BASE_URL + "/credential-profile/create";

    public CredentialProfileController(Router router, CredentialProfileService credentialProfileService) {
        this.credentialProfileService = credentialProfileService;
        router.route().handler(BodyHandler.create()); // Ensure the body handler is added to parse request bodies
        router.post(CREATE_CREDENTIAL_PROFILE).handler(this::createCredentialProfile);
    }

    public void createCredentialProfile(RoutingContext rc) {
        JsonObject body;
        try {
            body = rc.getBodyAsJson();
            if (body == null) {
                throw new IllegalArgumentException("Invalid JSON body");
            }

            credentialProfileService.createCredentialProfile(body).onComplete(result -> {
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

        } catch (Exception e) {
            rc.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("error", "Invalid JSON body").encodePrettily());
            return;
        }
    }
}
