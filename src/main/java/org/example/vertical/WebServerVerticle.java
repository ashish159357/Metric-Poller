package org.example.vertical;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.controller.CredentialProfileController;
import org.example.controller.DiscoveryController;
import org.example.service.CredentialProfileService;
import org.example.service.DiscoveryService;

@Slf4j
public class WebServerVerticle extends AbstractVerticle {

    private static final int DEFAULT_PORT = 8080;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        int port = config().getInteger("http.port", DEFAULT_PORT);

        Thread.currentThread().setName(Thread.currentThread().getName() + "-" + "WebServerVerticle");

        Router router = Router.router(vertx);

        // This body handler will handle the parsing of request bodies
        router.route().handler(BodyHandler.create());

        // Register controller routes
        new DiscoveryController(router, new DiscoveryService(vertx));
        new CredentialProfileController(router, new CredentialProfileService(vertx));

        // Create the HTTP server and pass the "accept" method to the request handler
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port, http -> {
                    if (http.succeeded()) {
                        log.info("HTTP server started on port {}", port);
                        startPromise.complete();
                    } else {
                        log.error("Failed to start HTTP server", http.cause());
                        startPromise.fail(http.cause().getMessage());
                    }
                });
    }
}
