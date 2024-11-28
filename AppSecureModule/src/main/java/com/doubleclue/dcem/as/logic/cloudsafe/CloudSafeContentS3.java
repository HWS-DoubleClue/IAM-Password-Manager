package com.doubleclue.dcem.as.logic.cloudsafe;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.inject.Named;
import javax.persistence.EntityManager;

import com.doubleclue.comm.thrift.CloudSafeOptions;
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

@Named("cloudSafeContentS3")
public class CloudSafeContentS3 implements CloudSafeContentI {

	static String awsS3BucketPrefix;

	public final static String DIGITAL_OCEAN_SPACES_URL = "https://nyc3.digitaloceanspaces.com";
	public final static String CLOUDSAFEFILE = "cloudsafe/";

	S3Client s3Client;

	public CloudSafeContentS3(String clusterName, String s3Url, String s3AccessKeyId, String s3SecretAccessKey) throws Exception {
		super();
		awsS3BucketPrefix = "doubleclue-" + clusterName.toLowerCase() + "-";
		AwsCredentials awsCreds = AwsBasicCredentials.create(s3AccessKeyId, s3SecretAccessKey);
		AwsCredentialsProvider awsCredentialsProvider = new AwsCredentialsProvider() {
			@Override
			public AwsCredentials resolveCredentials() {
				return awsCreds;
			}
		};
		if (s3Url == null || s3Url.isBlank()) {
			s3Client = S3Client.builder().region(Region.EU_CENTRAL_1).credentialsProvider(awsCredentialsProvider).build();
		} else {
			URI uri = new URI(s3Url);
			s3Client = S3Client.builder().region(Region.EU_CENTRAL_1).credentialsProvider(awsCredentialsProvider).endpointOverride(uri).build();
		}
		ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
//		List<Bucket> buckets = listBucketsResponse.buckets();
//		for (Bucket bucket : buckets) {
//			System.out.println(" Bucket: " + bucket.name());
//		}
	}

	@Override
	public void initiateTenant(String tenantName) throws Exception {
		String bucketName = awsS3BucketPrefix + tenantName.toLowerCase();
		if (checkAccessBucket(bucketName) == true) {
			return;
		}
		// Create Bucket
		CreateBucketRequest bucketRequest = CreateBucketRequest.builder().bucket(bucketName).build();
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
		String bucketName = awsS3BucketPrefix + TenantIdResolver.getCurrentTenantName().toLowerCase();
		GetObjectRequest objectRequest = GetObjectRequest.builder().bucket(bucketName).key(getObjectKey(id, null)).build();
		ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(objectRequest);
		return inputStream;
	}

	
	@Override
	public int writeContentOutput(EntityManager em, CloudSafeEntity cloudSafeEntity, InputStream inputStream) throws DcemException {
		String bucketName = awsS3BucketPrefix + TenantIdResolver.getCurrentTenantName().toLowerCase();
		String key = getObjectKey(cloudSafeEntity.getId(), null);
		long length = cloudSafeEntity.getLength();
		if (cloudSafeEntity.isOption(CloudSafeOptions.ENC) || cloudSafeEntity.isOption(CloudSafeOptions.PWD) && cloudSafeEntity.isGcm()) {
			length +=16;
		}
		PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(key).build();
		RequestBody requestBody = RequestBody.fromInputStream(inputStream, length);
		s3Client.putObject(request, requestBody);
		return (int) cloudSafeEntity.getLength();
	}

	public void delete(EntityManager em, int id) {
		delete(em, id, null);
	}

	public void delete(EntityManager em, int id, String prefix) {
		String bucketName = awsS3BucketPrefix + TenantIdResolver.getCurrentTenantName().toLowerCase();
		String key = getObjectKey(id, prefix);
		DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(key).build();
		s3Client.deleteObject(deleteObjectRequest);
		deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(key).build();
		s3Client.deleteObject(deleteObjectRequest);
	}
	
	@Override
	public void deleteS3Data(int id, String prefix) throws DcemException {
		delete (null, id, prefix);		
	}


	public void deleteBucket() throws Exception {
		String bucketName = awsS3BucketPrefix + TenantIdResolver.getCurrentTenantName().toLowerCase();
		DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucketName).build();
		s3Client.deleteBucket(deleteBucketRequest);
	}

	@Override
	public String toString() {
		return "CloudSafeContentS3";
	}

	private String getObjectKey(int id, String prefix) {
		if (prefix == null) {
			return CLOUDSAFEFILE + Integer.toString(id);
		}
		return CLOUDSAFEFILE + prefix + "-" + Integer.toString(id);
	}

	@Override
	public InputStream getS3Data(int id, String prefix) throws DcemException {
		String bucketName = awsS3BucketPrefix + TenantIdResolver.getCurrentTenantName().toLowerCase();
		GetObjectRequest objectRequest = GetObjectRequest.builder().bucket(bucketName).key(getObjectKey(id, prefix)).build();
		ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(objectRequest);
		return inputStream;
	}

	@Override
	public void writeS3Data(int id, String prefix, InputStream inputStream, int length) throws DcemException {
		String bucketName = awsS3BucketPrefix + TenantIdResolver.getCurrentTenantName().toLowerCase();
		String key = getObjectKey(id, prefix);
		PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(key).build();
		RequestBody requestBody = RequestBody.fromInputStream(inputStream, length);
		s3Client.putObject(request, requestBody);
		return;
		
	}

	
	
}
