package com.doubleclue.dcem.test.units;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.CloudSafeShareEntity;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;
import com.doubleclue.sdk.api.AsCloudSafeKey;

@ApplicationScoped
@Named("CloudSafeShareTest")
public class CloudSafeShareTest extends AbstractTestUnit {

	@Inject
	UserLogic userLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	AddTenUsersTest addTenUsersTest;

	private static final String FILENAME = "fileToShare.txt";
	private static final String FILE_CONTENT = "Hello there!";

	@Override
	public String getDescription() {
		return "This test will attempt to create, share, read and delete a Cloud Data file.";
	}

	@Override
	public String getAuthor() {
		return "Alan Ellul Pirotta";
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new LinkedList<String>();
		dependencies.add(AddTenUsersTest.class.getSimpleName());
		return dependencies;
	}	
	
	@Override
	public TestUnitGroupEnum getParent() {
		return TestUnitGroupEnum.USERPORTAL;
	}

	@Override
	public boolean isRunnableTest() {
		return true;
	}

	@Override
	public String start() throws Exception {

		DcemUser firstUser = userLogic.getUser(addTenUsersTest.getUser(0).getLoginId());

		// create cloud safe entity
		CloudSafeEntity cloudSafeEntity = new CloudSafeEntity();
		cloudSafeLogic.setCloudSafeByteArray(cloudSafeEntity, null, FILE_CONTENT.getBytes(DcemConstants.CHARSET_UTF8), firstUser, null);
		cloudSafeEntity = cloudSafeLogic.getCloudSafe(CloudSafeOwner.USER, FILENAME, firstUser, null, 0, null);

		// create share entity
		CloudSafeShareEntity cloudSafeShareEntity = new CloudSafeShareEntity();
		cloudSafeShareEntity.setCloudSafe(cloudSafeEntity);
		cloudSafeShareEntity.setWriteAccess(true);

		// share file with second user
		String secondUserId = addTenUsersTest.getUser(1).getLoginId();
		cloudSafeLogic.addOrEditShareCloudSafeFile(cloudSafeShareEntity, secondUserId, null);

		// check shared file
		//TODO enable this test again
//		List<CloudSafeShareEntity> sharedFiles = cloudSafeLogic.getShareCloudSafeFiles(secondUserId, null);
//		if (sharedFiles.size() != 1) {
//			throw new Exception("expected 1 shared file for " + secondUserId + " but has " + sharedFiles.size());
//		}

		// remove share
//		CloudSafeEntity sharedCloudSafeEntity = sharedFiles.get(0).getCloudSafeEntity();
//		cloudSafeLogic.deleteCloudShare(sharedCloudSafeEntity);

		// check shared file again
//		sharedFiles = cloudSafeLogic.getShareCloudSafeFiles(secondUserId, null);
//		if (sharedFiles.size() != 0) {
//			throw new Exception("expected 0 shared files for " + secondUserId + " but has " + sharedFiles.size());
//		}

		setInfo("OK, :-)");
		return null;
	}
}
