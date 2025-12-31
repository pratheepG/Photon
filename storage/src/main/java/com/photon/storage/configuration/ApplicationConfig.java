package com.photon.storage.configuration;

import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.storage.constants.BucketProvider;
import com.photon.storage.repository.FileMetadataRepository;
import com.photon.storage.service.api.MediaService;
import com.photon.storage.service.impl.ConsoleMediaService;
import com.photon.storage.service.impl.S3MediaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@Configuration
public class ApplicationConfig {

    @Value("${aws.accessKeyId:#{null}}")
    private String accessKeyId;

    @Value("${aws.secretKey:#{null}}")
    private String secretKey;

    @Value("${aws.region:#{null}}")
    private String region;

    @Bean
    @ConditionalOnProperty(name = "photon.bucket.provider", havingValue = BucketProvider.AWS_S3)
    public MediaService getS3MediaService(FileMetadataRepository fileMetadataRepository) {
        return new S3MediaService(s3Client(), fileMetadataRepository);
    }

    public S3AsyncClient s3Client() throws ApplicationException {
        if(accessKeyId == null || accessKeyId.isBlank() || secretKey == null || secretKey.isBlank() || region == null || region.isBlank())
            throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("failed to initialize the S3 instance"), HttpStatus.INTERNAL_SERVER_ERROR);

        return S3AsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(this.accessKeyId, this.secretKey)))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "photon.bucket.provider", havingValue = BucketProvider.CONSOLE, matchIfMissing = true)
    public MediaService getConsoleMediaService(FileMetadataRepository fileMetadataRepository) {
        return new ConsoleMediaService(fileMetadataRepository);
    }

}