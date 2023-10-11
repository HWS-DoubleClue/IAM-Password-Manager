package com.doubleclue.dcup.logic;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.CloudSafeOptions;
import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.userportal.logic.UserPortalModule;
import com.doubleclue.dcup.gui.PasswordSafeRecentFile;
import com.doubleclue.dcup.gui.PortalSessionBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.KeePassFile;

@ApplicationScoped
public class UserPortalKeePassLogic {

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	private PortalSessionBean portalSessionBean;

	@Inject
	AsModule asModule;

	private Logger logger = LogManager.getLogger(UserPortalKeePassLogic.class);

	/**
	 * @param _keePassFile
	 * @param fileName
	 * @param _password
	 * @return null if error happens
	 * @throws DcemException 
	 */
	public byte[] saveDatabaseFile(CloudSafeEntity cloudSafeEntity, KeePassFile _keePassFile, String fileName, String _password) throws DcemException {
		if (cloudSafeEntity.isWriteAccess() == false) {
			throw new DcemException(DcemErrorCodes.NO_WRITE_ACCESS, fileName);
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		KeePassDatabase.write(_keePassFile, _password, byteArrayOutputStream);

		cloudSafeLogic.setCloudSafeByteArray(cloudSafeEntity, null, byteArrayOutputStream.toByteArray(), portalSessionBean.getDcemUser(), null);
		cloudSafeEntity.setLastModified(LocalDateTime.now().plusSeconds(1));
		return byteArrayOutputStream.toByteArray();
	}

	public CloudSafeEntity createCloudSafeEntity(String name) throws DcemException {
		CloudSafeEntity cloudSafeEntity = new CloudSafeEntity();
		cloudSafeEntity.setOwner(CloudSafeOwner.USER);
		cloudSafeEntity.setUser(portalSessionBean.getDcemUser());
		cloudSafeEntity.setName(name);
		cloudSafeEntity.setOptions(CloudSafeOptions.ENC.name());
		cloudSafeEntity.setParent(cloudSafeLogic.getCloudSafeRoot());
		cloudSafeEntity.setDevice(cloudSafeLogic.getRootDevice());
		return cloudSafeEntity;
	}

	public String updatePsHistory(int id, String fileName, String password, boolean remove, List<PasswordSafeRecentFile> recentFiles, String groupName)
			throws Exception {
		if (recentFiles == null) {
			throw new Exception("recentFiles cannot be null");
		}
		String encPassword = null;
		if (password != null) {
			byte[] data = SecureServerUtils.encryptDataSalt(asModule.getConnectionKeyArray(), password.getBytes(DcemConstants.CHARSET_UTF8));
			encPassword = Base64.getEncoder().encodeToString(data);
		}
		PasswordSafeRecentFile historyFile = new PasswordSafeRecentFile(id, fileName, encPassword, groupName);
		recentFiles.remove(historyFile);
		if (remove == false) {
			recentFiles.add(0, historyFile);
		}
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(recentFiles);
	}

	public String escapeJson(String raw) {
		String escaped = raw;
		escaped = escaped.replace("\\", "\\\\");
		escaped = escaped.replace("\"", "\\\"");
		escaped = escaped.replace("\n", "\\n");
		escaped = escaped.replace("\r", "\\r");
		return escaped;
	}

}
