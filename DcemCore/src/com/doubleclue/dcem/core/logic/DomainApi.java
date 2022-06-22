package com.doubleclue.dcem.core.logic;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DomainEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;

public interface DomainApi {

	public void close();

	public void testConnection() throws DcemException;

	public DcemUser verifyLogin(DcemUser dcemUser, byte[] password) throws DcemException;

	public void updateUserPassword(DcemUser dcemUser, String currentPassword, String newPassword) throws DcemException;

	public List<String> getUserNames(String userFilter) throws DcemException;

	public List<DcemGroup> getGroups(String filter, int PAGE_SIZE) throws DcemException;

	public List<DcemGroup> getUserGroups(DcemUser dcemUser, int pAGE_SIZE) throws DcemException;

	public HashSet<String> getUserGroupNames(DcemUser dcemUser, String filter, int pAGE_SIZE) throws DcemException;

	public DcemUser getUser(String loginId) throws DcemException;

	List<DcemUser> getUsers(String tree, DcemGroup dcemGroup, String userName, int pageSize) throws DcemException;

	public List<DcemUser> getGroupMembers(DcemGroup group, String filter) throws DcemException;

	public DomainEntity getDomainEntity();

	public List<String> getSelectedLdapTree(String treeFilter, int pAGE_SIZE) throws DcemException;

	public Map<String, String> getUserAttributes(DcemUser dcemUser, List<String> attributeList) throws DcemException;

	public void resetUserPassword(DcemUser dcemUser, String newPassword) throws DcemException;

	public void changeUserPhotoProfile(DcemUser dcemUser, byte[] photo, String password) throws DcemException;
	
	public byte []  getUserPhoto (DcemUser dcemUser) throws DcemException;

}
