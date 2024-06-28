package org.example.config;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import org.example.vertical.DatabaseVerticle;
import org.example.vertical.WebServerVerticle;

import java.util.HashMap;
import java.util.Map;

public class DeploymentOptionsConfig {

    // Map to store deployment options for each Verticle class
    private static final Map<Class<? extends Verticle>, DeploymentOptions> verticleOptionsMap = new HashMap<>();

    // Static initializer to configure deployment options
    static {
        verticleOptionsMap.put(DatabaseVerticle.class, new DeploymentOptions());
        verticleOptionsMap.put(WebServerVerticle.class, new DeploymentOptions().setInstances(2));
    }

    // Method to get deployment options for a specific Verticle class
    public static DeploymentOptions getDeploymentOptions(Class<? extends Verticle> verticleClass) {
        return verticleOptionsMap.getOrDefault(verticleClass, new DeploymentOptions());
    }
}
