package com.photon.console.deployer.impl;

import com.photon.console.deployer.Deployer;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Deploys an image into Kubernetes by creating/updating a Deployment named `stableName`.
 * Assumes in-cluster configuration or KUBECONFIG available.
 */
public class K8sDeployer implements Deployer {

    private static final Logger log = LoggerFactory.getLogger(K8sDeployer.class);
    private final KubernetesClient k8s;

    public K8sDeployer() {
        Config config = Config.autoConfigure(null);
        this.k8s = new DefaultKubernetesClient(config);
    }

    @Override
    public void deploy(String imageName, String stableName, Map<String, String> env) throws Exception {
        String namespace = Optional.ofNullable(System.getenv("DEPLOY_NAMESPACE")).orElse("default");

        // convert env map to K8s EnvVar
        List<EnvVar> envVars = env == null ? List.of()
                : env.entrySet().stream().map(e -> new EnvVar(e.getKey(), e.getValue(), null)).collect(Collectors.toList());

        Container container = new Container();
        container.setName(stableName);
        container.setImage(imageName);
        container.setEnv(envVars);
        // optional ports - remove if not needed or expose via service
        container.setPorts(List.of(new ContainerPort(8080, null, null, null, null)));

        PodTemplateSpec podTemplate = new PodTemplateSpecBuilder()
                .withNewMetadata()
                    .addToLabels("app", stableName)
                .endMetadata()
                .withSpec(new PodSpecBuilder().withContainers(container).build())
                .build();

        Deployment newDeployment = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(stableName)
                    .withNamespace(namespace)
                .endMetadata()
                .withNewSpec()
                    .withReplicas(1)
                    .withTemplate(podTemplate)
                    .withNewSelector()
                        .addToMatchLabels("app", stableName)
                    .endSelector()
                .endSpec()
                .build();

        // create-or-update
        try {
            Deployment existing = k8s.apps().deployments().inNamespace(namespace).withName(stableName).get();
            if (existing == null) {
                k8s.apps().deployments().inNamespace(namespace).create(newDeployment);
                log.info("Created deployment {}/{}", namespace, stableName);
            } else {
                // update image and env
                k8s.apps().deployments().inNamespace(namespace).withName(stableName).patch(newDeployment);
                log.info("Patched deployment {}/{}", namespace, stableName);
            }
        } catch (KubernetesClientException kce) {
            log.error("K8s deployment failed", kce);
            throw kce;
        }
    }
}
