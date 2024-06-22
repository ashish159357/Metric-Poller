package org.example.config;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ProcessBuilderConfig {

    private ProcessBuilder processBuilder;

    public ProcessBuilderConfig(String command){

        processBuilder = new ProcessBuilder("bash", "-c", command);

        Map<String, String> env = processBuilder.environment();

        env.put("PATH",env.get("PATH") + ":/home/ashish/a4h-personal/Software/go1.22.4.linux-amd64/go/bin");

        processBuilder.directory(new File("/home/ashish/a4h-personal/Prototype/go-proto/myproject"));

        processBuilder.redirectErrorStream(true);
    }

    public Process getAndStartProcess(){
        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("Unable to Start Process");
        }
    }
}
