package org.jobai.skillbridge.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class TebiFileStorageService {

    @Value("${tebi.access.key}")
    private String accessKey;

    @Value("${tebi.secret.key}")
    private String secretKey;

    @Value("${tebi.bucket.name}")
    private String bucketName;

    @Value("${tebi.endpoint.url}")
    private String endpointUrl;

    private AmazonS3 s3Client;

    @PostConstruct
    public void initializeS3Client() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpointUrl, "US"))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .enablePathStyleAccess()
                .build();
    }

    /**
     * Upload a file to Tebi storage
     * 
     * @param file   The file to upload
     * @param folder The folder path (e.g., "resumes", "documents")
     * @param userId The user ID for organization
     * @return The URL of the uploaded file
     */
    public String uploadFile(MultipartFile file, String folder, Long userId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String fileName = generateFileName(file.getOriginalFilename(), userId);
        String keyPath = folder + "/" + userId + "/" + fileName;

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest request = new PutObjectRequest(bucketName, keyPath, file.getInputStream(), metadata);
            request.setCannedAcl(CannedAccessControlList.PublicRead);

            s3Client.putObject(request);

            return getFileUrl(keyPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to Tebi: " + e.getMessage(), e);
        }
    }

    /**
     * Upload resume specifically
     * 
     * @param file   The resume file
     * @param userId The user ID
     * @return The URL of the uploaded resume
     */
    public String uploadResume(MultipartFile file, Long userId) throws IOException {
        return uploadFile(file, "resumes", userId);
    }

    /**
     * Upload profile picture
     * 
     * @param file   The profile picture file
     * @param userId The user ID
     * @return The URL of the uploaded profile picture
     */
    public String uploadProfilePicture(MultipartFile file, Long userId) throws IOException {
        return uploadFile(file, "profiles", userId);
    }

    /**
     * Upload portfolio item
     * 
     * @param file   The portfolio file
     * @param userId The user ID
     * @return The URL of the uploaded portfolio item
     */
    public String uploadPortfolioItem(MultipartFile file, Long userId) throws IOException {
        return uploadFile(file, "portfolios", userId);
    }

    /**
     * Download a file from Tebi storage
     * 
     * @param keyPath The full path to the file
     * @return InputStream of the file
     */
    public InputStream downloadFile(String keyPath) {
        try {
            S3Object s3Object = s3Client.getObject(bucketName, keyPath);
            return s3Object.getObjectContent();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from Tebi: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a file from Tebi storage
     * 
     * @param keyPath The full path to the file
     */
    public void deleteFile(String keyPath) {
        try {
            s3Client.deleteObject(bucketName, keyPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from Tebi: " + e.getMessage(), e);
        }
    }

    /**
     * List all files for a user in a specific folder
     * 
     * @param folder The folder name
     * @param userId The user ID
     * @return List of file keys
     */
    public List<String> listUserFiles(String folder, Long userId) {
        try {
            String prefix = folder + "/" + userId + "/";
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(bucketName)
                    .withPrefix(prefix);

            ListObjectsV2Result result = s3Client.listObjectsV2(request);

            return result.getObjectSummaries().stream()
                    .map(S3ObjectSummary::getKey)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list files from Tebi: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a presigned URL for temporary access to a file
     * 
     * @param keyPath           The full path to the file
     * @param expirationMinutes How long the URL should be valid (in minutes)
     * @return Presigned URL
     */
    public String generatePresignedUrl(String keyPath, int expirationMinutes) {
        try {
            Date expiration = new Date();
            long expTimeMillis = expiration.getTime() + (1000L * 60 * expirationMinutes);
            expiration.setTime(expTimeMillis);

            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName,
                    keyPath)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);

            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
            return url.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }

    /**
     * Get the public URL of a file
     * 
     * @param keyPath The full path to the file
     * @return Public URL
     */
    public String getFileUrl(String keyPath) {
        return endpointUrl + "/" + bucketName + "/" + keyPath;
    }

    /**
     * Extract file path from URL
     * 
     * @param url The file URL
     * @return File path/key
     */
    public String extractFilePathFromUrl(String url) {
        if (url == null || !url.contains(bucketName)) {
            return null;
        }

        String[] parts = url.split("/" + bucketName + "/");
        return parts.length > 1 ? parts[1] : null;
    }

    /**
     * Check if a file exists
     * 
     * @param keyPath The full path to the file
     * @return true if file exists
     */
    public boolean fileExists(String keyPath) {
        try {
            s3Client.getObjectMetadata(bucketName, keyPath);
            return true;
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            throw new RuntimeException("Failed to check file existence: " + e.getMessage(), e);
        }
    }

    /**
     * Get file metadata
     * 
     * @param keyPath The full path to the file
     * @return ObjectMetadata
     */
    public ObjectMetadata getFileMetadata(String keyPath) {
        try {
            return s3Client.getObjectMetadata(bucketName, keyPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get file metadata: " + e.getMessage(), e);
        }
    }

    private String generateFileName(String originalFileName, Long userId) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + extension;
    }

    /**
     * Validate file type for security
     * 
     * @param file         The file to validate
     * @param allowedTypes Allowed MIME types
     * @return true if file type is allowed
     */
    public boolean isFileTypeAllowed(MultipartFile file, String[] allowedTypes) {
        String fileType = file.getContentType();
        if (fileType == null) {
            return false;
        }

        for (String allowedType : allowedTypes) {
            if (fileType.equals(allowedType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get allowed file types for resumes
     */
    public static final String[] RESUME_ALLOWED_TYPES = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    };

    /**
     * Get allowed file types for images
     */
    public static final String[] IMAGE_ALLOWED_TYPES = {
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif"
    };
}