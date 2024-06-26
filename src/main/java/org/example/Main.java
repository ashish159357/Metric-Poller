package org.example;

import io.vertx.core.*;
import lombok.extern.slf4j.Slf4j;
import org.example.config.DeploymentOptionsConfig;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        String packageName = "org.example.vertical";

        // Deploy the verticles
        deployVerticlesFromPackage(vertx, packageName)
                .onSuccess(v -> log.info("All verticles deployed successfully."))
                .onFailure(err -> log.error("Failed to deploy verticles.", err));
    }


    private static Future<Void> deployVerticlesFromPackage(Vertx vertx, String packageName) {
        Set<Class<? extends Verticle>> verticleClasses = getVerticleClasses(packageName);
        List<String> deployedVerticles = new ArrayList<>();
        Future<Void> future = Future.succeededFuture();

        for (Class<? extends Verticle> verticleClass : verticleClasses) {
            if (!verticleClass.getSimpleName().equals("AbstractVerticle")) {
                DeploymentOptions options = DeploymentOptionsConfig.getDeploymentOptions(verticleClass);
                future = future.compose(v -> deployVerticle(vertx, verticleClass, options, deployedVerticles));
            }
        }

        return future.recover(err -> undeployAll(vertx, deployedVerticles).compose(v -> Future.failedFuture(err)));
    }


    private static Set<Class<? extends Verticle>> getVerticleClasses(String packageName) {
        Reflections reflections = new Reflections(packageName);
        return reflections.getSubTypesOf(Verticle.class);
    }


    private static Future<Void> deployVerticle(Vertx vertx, Class<? extends Verticle> verticleClass, DeploymentOptions options,List<String> deployedVerticles) {
        Promise<Void> promise = Promise.promise();

        try {
            vertx.deployVerticle(verticleClass.getName(),options, res -> {
                if (res.succeeded()) {
                    log.info("{} deployed successfully.", verticleClass.getSimpleName());
                    deployedVerticles.add(res.result());
                    promise.complete();
                } else {
                    log.error("Failed to deploy {}.", verticleClass.getSimpleName(), res.cause());
                    promise.fail(res.cause());
                }
            });
        } catch (Exception e) {
            log.error("Failed to instantiate {}.", verticleClass.getSimpleName(), e);
            promise.fail(e);
        }
        return promise.future();
    }


    private static Future<Void> undeployAll(Vertx vertx, List<String> deploymentIds) {
        List<Future> futures = new ArrayList<>();
        for (String deploymentId : deploymentIds) {
            Promise<Void> promise = Promise.promise();
            vertx.undeploy(deploymentId, res -> {
                if (res.succeeded()) {
                    log.info("Verticle {} undeployed successfully.", deploymentId);
                    promise.complete();
                } else {
                    log.error("Failed to undeploy verticle {}.", deploymentId, res.cause());
                    promise.fail(res.cause());
                }
            });
            futures.add(promise.future());
        }
        return CompositeFuture.all(futures).mapEmpty();
    }
}