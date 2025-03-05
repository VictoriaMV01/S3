package com.s3.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3ServiceImp implements IS3Service{

    @Value("${spring.destination.folder}")
    private String destinationFolder;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    @Override
    public String createBucket(String bucketName) {
        CreateBucketResponse response = this.s3Client.createBucket(bucketBuilder -> bucketBuilder.bucket(bucketName));
        return "Bucket was created in the path " + response.location();
    }

    @Override
    public String checkIfBucketExist(String bucketName) {
        try{
            this.s3Client.headBucket(headBucket -> headBucket.bucket(bucketName));
            return "Bucket " + bucketName + " exists";
        }catch (S3Exception e){
            return "Bucket " + bucketName + " does NOT exist";
        }
    }

    @Override
    public List<String> getAllBuckets() {
        ListBucketsResponse bucketResponse = this.s3Client.listBuckets();

        if(bucketResponse.hasBuckets()){
            return bucketResponse.buckets()
                    .stream()
                    .map(Bucket::name)
                    .collect(Collectors.toList());
        }else
            return List.of();
    }

    @Override
    public Boolean uploadFile(String bucketName, String key, Path fileLocation) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        PutObjectResponse putObjectResponse = this.s3Client.putObject(putObjectRequest, fileLocation);
        return putObjectResponse.sdkHttpResponse().isSuccessful();
    }

    @Override
    public void downloadFile(String bucketName, String key) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = this.s3Client.getObjectAsBytes(getObjectRequest);

        String fileName;
        if(key.contains("/")){
            fileName = key.substring(key.lastIndexOf("/"), key.length());
        }else{
            fileName = key;
        }

        String filePath = Paths.get(destinationFolder, fileName).toString();

        File file = new File(filePath);
        file.getParentFile().mkdir();

        try(FileOutputStream fos = new FileOutputStream(file)){
            fos.write(objectBytes.asByteArray());
        }catch (IOException e){
            throw  new IOException("Error al descargar el archivo " + e.getCause());
        }

    }

    @Override
    public String generatePresignedUploadUrl(String bucketName, String key, Duration duration) {
         PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                 .bucket(bucketName)
                 .key(key)
                 .build();

        PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(duration)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedPutObjectRequest = this.s3Presigner.presignPutObject(putObjectPresignRequest);
        URL presignedUrl = presignedPutObjectRequest.url();

        return presignedUrl.toString();
    }

    @Override
    public String generatePresignedDownloadUrl(String bucketName, String key, Duration duration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignRequest = this.s3Presigner.presignGetObject(getObjectPresignRequest);
        URL presignedUrl = presignRequest.url();

        return presignedUrl.toString();
    }
}
