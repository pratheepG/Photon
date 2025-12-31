//package com.photon.console.deployer.impl;
//
//import com.google.cloud.tools.jib.api.*;
//import com.google.cloud.tools.jib.api.buildplan.FileEntriesLayer;
//import com.google.cloud.tools.jib.api.buildplan.FilePermissions;
//import com.google.cloud.tools.jib.api.buildplan.AbsoluteUnixPath;
//import com.photon.console.deployer.ImageBuilder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.nio.file.Path;
//import java.nio.file.Files;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//public class DockerImageBuilderImpl implements ImageBuilder {
//
//    private static final Logger log = LoggerFactory.getLogger(DockerImageBuilderImpl.class);
//    private final String registry;
//    private final String registryUser;
//    private final String registryPassword;
//    private final boolean allowInsecure;
//
//    public DockerImageBuilderImpl(String registry, String user, String password, boolean allowInsecure) {
//        this.registry = registry;
//        this.registryUser = user;
//        this.registryPassword = password;
//        this.allowInsecure = allowInsecure;
//    }
//
//    public DockerImageBuilderImpl() {
//        this.registry = null;
//        this.registryUser = null;
//        this.registryPassword = null;
//        this.allowInsecure = false;
//    }
//
//    @Override
//    public String buildAndPushImage(Path tmpDir, String baseName, Map<String, String> env) throws Exception {
//        Path jarPath = tmpDir.resolve("app.jar");
//        if (!Files.exists(jarPath)) {
//            throw new IllegalStateException("app.jar not found in build context: " + jarPath);
//        }
//
//        String tagSuffix = UUID.randomUUID().toString().substring(0, 8);
//        String imageRef = registry + "/" + baseName + ":" + tagSuffix;
//
//        ImageReference target = ImageReference.parse(imageRef);
//        RegistryImage registryImage = RegistryImage.named(target);
//        if (registryUser != null && registryPassword != null) {
//            registryImage.addCredential(registryUser, registryPassword);
//        }
//
//        log.info("Building image {} from {}", imageRef, tmpDir);
//
//        FileEntriesLayer layer = FileEntriesLayer.builder()
//                .addEntry(jarPath.toFile().toPath(), AbsoluteUnixPath.get("/app/app.jar"),
//                        FilePermissions.fromOctalString("rw-r--r--"))
//                .build();
//
//        JibContainer container = Jib.from("eclipse-temurin:17-jre")
//                .addFileEntriesLayer(layer)
//                .setEntrypoint("java", "-jar", "/app/app.jar")
//                .containerize(
//                        Containerizer.to(registryImage)
//                                .setAllowInsecureRegistries(allowInsecure)
//                                // tune timeouts if you want
//                                .setToolName("photon-jib")
//                );
//
//        log.info("Pushed image {}", imageRef);
//        return imageRef;
//    }
//
//    @Override
//    public String buildImage(Path contextDir, String imageName) throws Exception {
//
//        String fullImageName = imageName + ":latest";
//
//        Jib.from("eclipse-temurin:17-jre")
//                .addLayer(
//                        List.of(contextDir.resolve("app.jar")),
//                        AbsoluteUnixPath.get("/app")
//                )
//                .setEntrypoint("java", "-jar", "/app/app.jar")
//                .containerize(Containerizer.to(DockerDaemonImage.named(fullImageName)));
//
//        return fullImageName;
//    }
//}