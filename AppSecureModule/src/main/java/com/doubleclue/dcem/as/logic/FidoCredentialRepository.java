package com.doubleclue.dcem.as.logic;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.entities.FidoAuthenticatorEntity;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;

public class FidoCredentialRepository implements CredentialRepository {

	private static Logger logger = LogManager.getLogger(AsModule.class);

	@Override
	public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
		try {
			UserLogic userLogic = CdiUtils.getReference(UserLogic.class);
			AsFidoLogic fidoLogic = CdiUtils.getReference(AsFidoLogic.class);
			DcemUser user = userLogic.getUser(username);
			List<FidoAuthenticatorEntity> entities = fidoLogic.getFidoAuthenticators(user);
			HashSet<PublicKeyCredentialDescriptor> descriptors = new HashSet<>();
			for (FidoAuthenticatorEntity entity : entities) {
				descriptors.add(fidoLogic.getDescriptorFromFidoAuthenticator(entity));
			}
			return descriptors;
		} catch (DcemException e) {
			logger.log(Level.DEBUG, e);
		}
		return new HashSet<>();
	}

	@Override
	public Optional<ByteArray> getUserHandleForUsername(String username) {
		try {
			UserLogic userLogic = CdiUtils.getReference(UserLogic.class);
			AsFidoLogic fidoLogic = CdiUtils.getReference(AsFidoLogic.class);
			DcemUser user = userLogic.getUser(username);
			return Optional.ofNullable(user != null ? fidoLogic.getUserHandleFromUser(user) : null);
		} catch (DcemException e) {
			logger.log(Level.DEBUG, e);
		}
		return Optional.ofNullable(null);
	}

	@Override
	public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
		AsFidoLogic fidoLogic = CdiUtils.getReference(AsFidoLogic.class);
		DcemUser user = fidoLogic.getUserFromUserHandle(userHandle);
		return Optional.ofNullable(user != null ? user.getLoginId() : null);
	}

	@Override
	public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
		try {
			AsFidoLogic fidoLogic = CdiUtils.getReference(AsFidoLogic.class);
			DcemUser user = fidoLogic.getUserFromUserHandle(userHandle);
			FidoAuthenticatorEntity entity = fidoLogic.getFidoAuthenticator(user, credentialId);
			return Optional.ofNullable(entity != null ? fidoLogic.getRegisteredCredentialFromFidoAuthenticator(entity) : null);
		} catch (DcemException e) {
			logger.log(Level.DEBUG, e);
		}
		return Optional.ofNullable(null);
	}

	@Override
	public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
		try {
			AsFidoLogic fidoLogic = CdiUtils.getReference(AsFidoLogic.class);
			List<FidoAuthenticatorEntity> entities = fidoLogic.getFidoAuthenticatorsByCredentialID(credentialId);
			if (entities != null) {
				HashSet<RegisteredCredential> credentials = new HashSet<>();
				for (FidoAuthenticatorEntity entity : entities) {
					credentials.add(fidoLogic.getRegisteredCredentialFromFidoAuthenticator(entity));
				}
				return credentials;
			}
		} catch (DcemException e) {
			logger.log(Level.DEBUG, e);
		}
		return new HashSet<>();
	}
}
