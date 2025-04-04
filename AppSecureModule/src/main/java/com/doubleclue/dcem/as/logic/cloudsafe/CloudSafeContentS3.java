package com.doubleclue.dcem.as.logic.cloudsafe;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
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
import software.amazon.awssdk.awscore.AwsRequestOverrideConfiguration;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.BucketLifecycleConfiguration;
import software.amazon.awssdk.services.s3.model.BucketVersioningStatus;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ExpirationStatus;
import software.amazon.awssdk.services.s3.model.GetBucketLifecycleConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLifecycleConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketVersioningRequest;
import software.amazon.awssdk.services.s3.model.GetBucketVersioningResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.LifecycleRule;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoncurrentVersionExpiration;
import software.amazon.awssdk.services.s3.model.ObjectVersion;
import software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationResponse;
import software.amazon.awssdk.services.s3.model.PutBucketVersioningRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.VersioningConfiguration;

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
//			GetBucketVersioningRequest bucketVersioningRequest = GetBucketVersioningRequest.builder().bucket(bucket.name()).build();
//			GetBucketVersioningResponse bucketVersioningResponse = s3Client.getBucketVersioning(bucketVersioningRequest);
//			BucketVersioningStatus bucketVersioningStatus = bucketVersioningResponse.status();
//			if (bucketVersioningStatus != BucketVersioningStatus.ENABLED) {
//				enableVersioning(bucket.name());
//			}
//	//		addVersionRule(bucket.name());
//			getVersionRule(bucket.name());
//		}
	}

	private void addVersionRule(String bucketName) {
		if (bucketName == null) {
			bucketName = awsS3BucketPrefix + TenantIdResolver.getCurrentTenantName().toLowerCase();
		}
		NoncurrentVersionExpiration noncurrentVersion = NoncurrentVersionExpiration.builder().newerNoncurrentVersions(3).noncurrentDays(14).build();
		LifecycleRule lifecycleRule = LifecycleRule.builder().status(ExpirationStatus.ENABLED).prefix(CLOUDSAFEFILE)
				.noncurrentVersionExpiration(noncurrentVersion).build();

		NoncurrentVersionExpiration noncurrentVersion2 = NoncurrentVersionExpiration.builder().noncurrentDays(60).build();
		LifecycleRule lifecycleRule2 = LifecycleRule.builder().status(ExpirationStatus.ENABLED).prefix(CLOUDSAFEFILE)
				.noncurrentVersionExpiration(noncurrentVersion2).build();

		List<LifecycleRule> rules = new ArrayList<LifecycleRule>();
		rules.add(lifecycleRule2);
		rules.add(lifecycleRule);
		BucketLifecycleConfiguration bucketLifecycleConfiguration = BucketLifecycleConfiguration.builder().rules(rules).build();
		AwsRequestOverrideConfiguration awsRequestOverrideConfiguration = AwsRequestOverrideConfiguration.builder().build();
		PutBucketLifecycleConfigurationRequest putBucketLifecycleConfigurationRequest = PutBucketLifecycleConfigurationRequest.builder().bucket(bucketName)
				.lifecycleConfiguration(bucketLifecycleConfiguration).overrideConfiguration(awsRequestOverrideConfiguration).build();
		PutBucketLifecycleConfigurationResponse response = s3Client.putBucketLifecycleConfiguration(putBucketLifecycleConfigurationRequest);
	//	System.out.println("CloudSafeContentS3.addVersionRule() " + response.responseMetadata().toString());
	}
	
	private void getVersionRule(String bucketName) {
		GetBucketLifecycleConfigurationRequest bucketLifecycleConfigurationRequest = GetBucketLifecycleConfigurationRequest.builder().bucket(bucketName).build();
		GetBucketLifecycleConfigurationResponse response = s3Client.getBucketLifecycleConfiguration(bucketLifecycleConfigurationRequest);
		System.out.println("CloudSafeContentS3.getVersionRule() " + response.rules().size());
		for (LifecycleRule lifecycleRule: response.rules()) {
			System.out.println("CloudSafeContentS3.getVersionRule() " + bucketName + " " + lifecycleRule.noncurrentVersionExpiration().toString());
		}
	}
	

	@Override
	public void initiateTenant(String tenantName) throws Exception {
		String bucketName = awsS3BucketPrefix + tenantName.toLowerCase();
		if (checkAccessBucket(bucketName) == true) {
			addVersionRule(bucketName);
			return;
		}
		// Create Bucket
		CreateBucketRequest bucketRequest = CreateBucketRequest.builder().bucket(bucketName).build();
		s3Client.createBucket(bucketRequest);
		enableVersioning(bucketName);
		addVersionRule(bucketName);
	}

	private void enableVersioning(String bucketName) {
		VersioningConfiguration versioningConfiguration = VersioningConfiguration.builder().status(BucketVersioningStatus.ENABLED).build();
		PutBucketVersioningRequest request = PutBucketVersioningRequest.builder().bucket(bucketName).versioningConfiguration(versioningConfiguration).build();
		s3Client.putBucketVersioning(request);
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
	public InputStream getS3ContentInputStream(int id, String prefix, String versionId) throws DcemException {
		String bucketName = awsS3BucketPrefix + TenantIdResolver.getCurrentTenantName().toLowerCase();
		GetObjectRequest objectRequest = GetObjectRequest.builder().bucket(bucketName).key(getObjectKey(id, prefix)).versionId(versionId).build();
		ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(objectRequest);
		return inputStream;
	}

	@Override
	public List<DocumentVersion> getS3Versions(int id) throws DcemException {
		String bucketName = awsS3BucketPrefix + TenantIdResolver.getCurrentTenantName().toLowerCase();
		ListObjectVersionsRequest versionsRequest = ListObjectVersionsRequest.builder().bucket(bucketName).prefix(getObjectKey(id, null)).build();
		ListObjectVersionsResponse listS3 = s3Client.listObjectVersions(versionsRequest);
		List<DocumentVersion> listDocuments = new ArrayList<DocumentVersion>();
		for (ObjectVersion objectVersion : listS3.versions()) {
			listDocuments.add(new DocumentVersion(objectVersion.lastModified(), objectVersion.versionId(), objectVersion.size(), objectVersion.eTag(),
					objectVersion.isLatest()));
		}
		return listDocuments;
	}

	@Override
	public int writeContentOutput(EntityManager em, CloudSafeEntity cloudSafeEntity, InputStream inputStream) throws DcemException {
		String bucketName = awsS3BucketPrefix + TenantIdResolver.getCurrentTenantName().toLowerCase();
		String key = getObjectKey(cloudSafeEntity.getId(), null);
		long length = cloudSafeEntity.getLength();
		if (cloudSafeEntity.isOption(CloudSafeOptions.ENC) || cloudSafeEntity.isOption(CloudSafeOptions.PWD) && cloudSafeEntity.isGcm()) {
			length += 16;
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
		delete(null, id, prefix);
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
	public void writeS3Data(int id, String prefix, InputStream inputStream, int length) throws DcemException {
		String bucketName = awsS3BucketPrefix + TenantIdResolver.getCurrentTenantName().toLowerCase();
		String key = getObjectKey(id, prefix);
		PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(key).build();
		RequestBody requestBody = RequestBody.fromInputStream(inputStream, length);
		s3Client.putObject(request, requestBody);
		return;

	}

}
