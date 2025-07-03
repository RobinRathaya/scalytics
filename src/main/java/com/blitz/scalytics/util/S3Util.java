package com.blitz.scalytics.util;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class S3Util {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.reports.folder}")
    private String reportsFolder;

    @Value("${aws.region}")
    private String region;

    public S3Util(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(String s3Key, File file) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build(),
            RequestBody.fromFile(file)
        );
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
    }

    public void deleteFolder() {
        var listing = s3Client.listObjectsV2(
            ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(reportsFolder)
                .build()
        );
        listing.contents().forEach(s -> {
            s3Client.deleteObject(
                DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s.key())
                    .build()
            );
        });
    }

    public byte[] downloadFile(String filename) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(reportsFolder + filename)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
        return objectBytes.asByteArray();
    }

    public String getChartUrl(String filename) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s%s", bucketName, region, reportsFolder, filename);
    }

}