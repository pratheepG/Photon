package com.photon.console.configuration;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.photon.console.deployer.Deployer;
import com.photon.console.deployer.ImageBuilder;
import com.photon.console.deployer.impl.DockerBuildKitImageBuilder;
import com.photon.console.deployer.impl.DockerDeployer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
//public class DeployerConfig {
//
//    @Bean
//    public ImageBuilder jibImageBuilder(@Value("${registry.host}") String registry,
//                                        @Value("${registry.user:}") String user,
//                                        @Value("${registry.password:}") String password,
//                                        @Value("${jib.allowInsecure:false}") boolean allowInsecure) {
//        return new JibImageBuilder(registry, user.isEmpty() ? null : user, password.isEmpty() ? null : password, allowInsecure);
//    }
//
////    @Bean
////    public Deployer k8sDeployer() {
////        return new K8sDeployer();
////    }
//
//    @Bean
//    public Deployer dockerDeployer() {
//        return new DockerDeployer();
//    }
//}

//@Configuration
//public class DeployerConfig {
//
//    //    @Bean
////    public ImageBuilder jibImageBuilder(@Value("${registry.host}") String registry,
////                                        @Value("${registry.user:}") String user,
////                                        @Value("${registry.password:}") String password,
////                                        @Value("${jib.allowInsecure:false}") boolean allowInsecure) {
////        return new JibImageBuilder(registry, user.isEmpty() ? null : user, password.isEmpty() ? null : password, allowInsecure);
////    }
//
//    @Bean
//    public ImageBuilder imageBuilder(
//            @Value("${photon.deploy.target:local}") String target
//    ) {
//        return switch (target.toLowerCase()) {
//            case "local" -> new DockerImageBuilderImpl();
//            //case "registry" -> new DockerImageBuilderImpl(registry, user, password, allowInsecure);
//            default -> throw new IllegalStateException("Unsupported deploy target: " + target);
//        };
//    }
//
//    @Bean
//    public Deployer dockerDeployer() {
//        return new DockerDeployer();
//    }
//}

@Configuration
public class DeployerConfig {

    @Bean
    public DockerClient dockerClient() {
        DefaultDockerClientConfig config =
                DefaultDockerClientConfig.createDefaultConfigBuilder().build();

        DockerHttpClient httpClient =
                new ApacheDockerHttpClient.Builder()
                        .dockerHost(config.getDockerHost())
                        .sslConfig(config.getSSLConfig())
                        .build();

        return DockerClientImpl.getInstance(config, httpClient);
    }

    @Bean
    public ImageBuilder imageBuilder(DockerClient dockerClient) {
        return new DockerBuildKitImageBuilder();
    }

    @Bean
    public Deployer dockerDeployer(DockerClient dockerClient) {
        return new DockerDeployer();
    }
}