package org.example.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.example.constant.Constants;
import org.example.constant.EventBusAddresses;

@Slf4j
public class CredentialProfileService {

    private final Vertx vertx;

    public CredentialProfileService(Vertx vertx) {
        this.vertx = vertx;
    }

    public Future<String> createCredentialProfile(JsonObject credentialProfile) {
        Promise<String> promise = Promise.promise();
        credentialProfile.put(Constants.DAO_KEY, Constants.CREDENTIAL_PROFILE_DAO_NAME);

        // Execute blocking operation
        vertx.executeBlocking(promiseBlocking -> {
            vertx.eventBus().request(EventBusAddresses.DATABASE_INSERT, credentialProfile, reply -> {
                if (reply.succeeded()) {
                    log.info("Successfully created credential profile.");
                    promiseBlocking.complete("Successfully Created Credential Profile");
                } else {
                    log.error("Failed to create credential profile: {}", reply.cause().getMessage());
                    promiseBlocking.fail("Unable to create Credential Profile: " + reply.cause().getMessage());
                }
            });
        }, false, result -> {
            if (result.succeeded()) {
                promise.complete(result.result().toString());
            } else {
                promise.fail(result.cause());
            }
        });

        return promise.future();
    }
}
