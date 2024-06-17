package codearise.openapispecgenerator.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.bucketName}")
    private String bucketName;

    public S3Service(
            @Value("${aws.accessKeyId}") String accessKeyId,
            @Value("${aws.secretKey}") String secretKey,
            @Value("${aws.bucketName}") String bucketName) {

        this.bucketName = bucketName;

        this.s3Client = S3Client.builder()
                .region(Region.EU_WEST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretKey)))
                .build();
    }

    public String uploadFile(byte[] fileData, String originalFilename) {
        String key = UUID.randomUUID().toString() + "_" + originalFilename;
        Path tempFile = Path.of(System.getProperty("java.io.tmpdir"), key);
        try {
            Files.write(tempFile, fileData);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.putObject(putObjectRequest, tempFile);
            Files.delete(tempFile);
            return "https://" + bucketName + ".s3.amazonaws.com/" + key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }
}
