package com.photon.console.deployer;

import java.util.Map;

public interface Deployer {
    /**
     * Deploy imageName into runtime.
     * For Docker this may create a container; for Kubernetes it may create/update a Deployment.
     *
     * @param imageName fully-qualified image tag
     * @param stableName stable service/container name (idempotency)
     * @param env environment variables for runtime
     */
    void deploy(String imageName, String stableName, Map<String,String> env) throws Exception;
}