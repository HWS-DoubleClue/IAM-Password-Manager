package com.doubleclue.dcem.as.logic.cloudsafe;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.inject.Named;
import javax.persistence.EntityManager;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Named("cloudSafeContentNas")
public class CloudSafeContentS3 implements CloudSafeContentI {

	final static String AWS_S3_NAME = "doubleclue-";

	S3Client s3Client;

	public CloudSafeContentS3(String s3AccessKeyId, String s3SecretAccessKey) throws Exception {
		super();
		AwsCredentials awsCreds = AwsBasicCredentials.create(s3AccessKeyId, s3SecretAccessKey);
		AwsCredentialsProvider awsCredentialsProvider = new AwsCredentialsProvider() {
			@Override
			public AwsCredentials resolveCredentials() {
				return awsCreds;
			}
		};
		URI uri = new URI("https://nyc3.digitaloceanspaces.com");
		s3Client = S3Client.builder().region(Region.EU_CENTRAL_1).credentialsProvider(awsCredentialsProvider).endpointOverride(uri).build();
		ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
		List<Bucket> buckets = listBucketsResponse.buckets();
		System.out.println("Buckets:");
		for (Bucket bucket : buckets) {
			System.out.println(bucket.name());
		}
	}

	@Override
	public void initiateTenant(String tenantName) throws Exception {
		String bucketName = AWS_S3_NAME + TenantIdResolver.getCurrentTenantName().toLowerCase();
		if (checkAccessBucket(bucketName) == true) {
			return;
		}
		// Create Bucket
		CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
			    .bucket(bucketName)
			    .build();
		s3Client.createBucket(bucketRequest);
	}

	private boolean checkAccessBucket(String tenantName) {
		try {
			HeadBucketRequest request = HeadBucketRequest.builder().bucket(tenantName).build();
			HeadBucketResponse headBucketResponse = s3Client.headBucket(request);
			int result = headBucketResponse.sdkHttpResponse().statusCode();
			if (result >= 200 && result < 300) {
				return true;
			}
			return false;
		} catch (NoSuchBucketException ex) {
			return false;
		} catch (Exception ex) {
			// LOGGER.error("Cannot check access", ex);
			throw ex;
		}
	}

	@Override
	public InputStream getContentInputStream(EntityManager em, int id) throws DcemException {
		return getContentInputStream(em, id, null);
	}

	@Override
	public InputStream getContentInputStream(EntityManager em, int id, String prefix) throws DcemException {
		String bucketName = AWS_S3_NAME + TenantIdResolver.getCurrentTenantName().toLowerCase();
		GetObjectRequest objectRequest = GetObjectRequest.builder().bucket(bucketName).key(getObjectKey(id, prefix)).build();
		ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(objectRequest);
		return inputStream;

		// ResponseBytes<GetObjectResponse> responseResponseBytes = s3Client.getObjectAsBytes(objectRequest);

		// byte[] data = responseResponseBytes.asByteArray();

		// Write the data to a local file.
		// java.io.File myFile = new java.io.File("/Users/user/Desktop/hello.txt");
		// OutputStream os = new FileOutputStream(myFile);
		// os.write(data);
		// System.out.println("Successfully obtained bytes from an S3 object");
		// os.close();
	}

	@Override
	public int writeContentOutput(EntityManager em, CloudSafeEntity cloudSafeEntity, InputStream inputStream) throws DcemException {
		return writeContentOutput(em, cloudSafeEntity, null, inputStream);
	}

	@Override
	public int writeContentOutput(EntityManager em, CloudSafeEntity cloudSafeEntity, String prefix, InputStream inputStream) throws DcemException {
		String bucketName = AWS_S3_NAME + TenantIdResolver.getCurrentTenantName().toLowerCase();
		String key = getObjectKey(cloudSafeEntity.getId(), prefix);
		PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(key).build();
		RequestBody requestBody = RequestBody.fromInputStream(inputStream, cloudSafeEntity.getLength());
		s3Client.putObject(request, requestBody);
		return (int) cloudSafeEntity.getLength();
	}

	public void delete(EntityManager em, int id) {
		delete(em, id, null);
	}

	public void delete(EntityManager em, int id, String prefix) {
		String bucketName = AWS_S3_NAME + TenantIdResolver.getCurrentTenantName().toLowerCase();
		String key = getObjectKey(id, prefix);
		DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(key).build();
		s3Client.deleteObject(deleteObjectRequest);
	}

	public void deleteBucket() throws Exception {
		String bucketName = AWS_S3_NAME + TenantIdResolver.getCurrentTenantName().toLowerCase();
		DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucketName).build();
		s3Client.deleteBucket(deleteBucketRequest);
		System.out.println("Successfully deleted bucket : " + bucketName);
	}

	@Override
	public String toString() {
		return "CloudSafeContentS3";
	}

	private String getObjectKey(int id, String prefix) {
		if (prefix == null) {
			return Integer.toString(id);
		}
		return prefix + "-" + Integer.toString(id);
	}

}
