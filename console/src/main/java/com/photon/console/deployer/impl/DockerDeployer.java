package com.photon.console.deployer.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.RestartPolicy;
import com.photon.console.deployer.Deployer;

import com.github.dockerjava.api.model.HostConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.util.*;

import static java.lang.Thread.sleep;


@Slf4j
public class DockerDeployer implements Deployer {

    private static final String NETWORK = "photon-net";

    @Override
    public void deploy(String imageName, String containerName, Map<String, String> env) throws Exception {
        runIgnoreFailure("docker", "rm", "-f", containerName);

        List<String> command = new ArrayList<>();
        command.addAll(List.of(
                "docker", "run", "-d",
                "--name", containerName,
                "--network", NETWORK
        ));

        if (env != null) {
            env.forEach((k, v) -> command.addAll(List.of("-e", k + "=" + v)));
        }

        command.add(imageName);

        run(command);
        log.info("Container started: {} on network {}", containerName, NETWORK);
    }

    private void run(List<String> command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        try (Scanner sc = new Scanner(p.getInputStream())) {
            while (sc.hasNextLine()) {
                log.info("[docker] {}", sc.nextLine());
            }
        }

        int code = p.waitFor();
        if (code != 0) {
            throw new IllegalStateException("Docker command failed: " + command);
        }
    }

    private void runIgnoreFailure(String... cmd) {
        try {
            new ProcessBuilder(cmd).start().waitFor();
        } catch (Exception ignored) {}
    }

    private int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Unable to allocate free port", e);
        }
    }

    private void waitForHealth(String host, int port, int timeoutSeconds) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutSeconds * 1000L;

        while (System.currentTimeMillis() < end) {
            try {
                URL url = new URL("http://" + host + ":" + port + "/actuator/health");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(2000);
                con.setReadTimeout(2000);

                if (con.getResponseCode() == 200) {
                    log.info("Health check passed on port {}", port);
                    return;
                }
            } catch (Exception ignored) {}

            sleep(2000);
        }
        throw new RuntimeException("Service failed health check");
    }
}