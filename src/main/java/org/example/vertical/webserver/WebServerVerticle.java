package org.example.vertical.webserver;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.example.controller.CredentialProfileController;
import org.example.controller.DiscoveryController;

public class WebServerVerticle extends AbstractVerticle {

    int port;

    public WebServerVerticle(int port){
        this.port = port;
    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        // This body handler will handle the parsing of request bodies
        router.route().handler(BodyHandler.create());

        // Register controller routes
        DiscoveryController discoveryController = new DiscoveryController(router,vertx);
        CredentialProfileController credentialProfileController = new CredentialProfileController(router,vertx);

        // Create the HTTP server and pass the "accept" method to the request handler
        vertx.createHttpServer().requestHandler(router).listen(8080);

    }
}
