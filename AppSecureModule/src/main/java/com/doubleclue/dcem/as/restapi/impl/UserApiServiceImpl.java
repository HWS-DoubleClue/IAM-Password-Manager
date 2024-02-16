package com.doubleclue.dcem.as.restapi.impl;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.subjects.UserSubject;
import com.doubleclue.dcem.as.comm.AppServices;
import com.doubleclue.dcem.as.entities.ActivationCodeEntity;
import com.doubleclue.dcem.as.logic.AsActivationLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.restapi.model.AsApiActivationCode;
import com.doubleclue.dcem.as.restapi.model.AsApiUrlToken;
import com.doubleclue.dcem.as.restapi.model.AsApiUser;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.UrlTokenEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.jpa.JpaSelectProducer;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.UrlTokenLogic;
import com.doubleclue.dcem.core.logic.UrlTokenType;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.subjects.AsActivationSubject;
import com.doubleclue.utils.StringUtils;

public class UserApiServiceImpl {

	private static Logger logger = LogManager.getLogger(UserApiServiceImpl.class);

	@Inject
	AppServices appServices;

	@Inject
	OperatorSessionBean sessionBean;

	@Inject
	EntityManager entityManager;

	@Inject
	UserLogic userLogic;

	@Inject
	JpaLogic jpaLogic;

	@Inject
	UserSubject userSubject;

	@Inject
	AsActivationSubject activationSubject;

	@Inject
	AsActivationLogic activationLogic;

	@Inject
	AsModule asModule;

	@Inject
	AdminModule adminModule;

	@Inject
	DomainLogic domainLogic;

	@Inject
	UrlTokenLogic urlTokenLogic;

	@Inject
	RoleLogic roleLogic;

	public Response queryUsers(List<ApiFilterItem> filters, Integer offset, Integer maxResults, SecurityContext securityContext) throws DcemApiException {

		JpaSelectProducer<DcemUser> jpaSelectProducer = new JpaSelectProducer<DcemUser>(entityManager, DcemUser.class);
		int firstResult = 0;
		if (offset != null) {
			firstResult = offset.intValue();
		}
		int page = DcemConstants.MAX_DB_RESULTS;
		if (maxResults != null && maxResults.intValue() < page) {
			page = maxResults.intValue();
		}

		try {
			List<DcemUser> dcemUsers = jpaSelectProducer.selectCriteriaQueryFilters(filters, firstResult, page, null);
			List<AsApiUser> users = new LinkedList<>();
			for (DcemUser user : dcemUsers) {
				AsApiUser apiUser = new AsApiUser();
				try {
					DcemUtils.copyObject(user, apiUser);
					if (user.getLanguage() != null) {
						apiUser.setPreferedLanguage(user.getLanguage().name());
					}
					apiUser.setDomain(user.isDomainUser());
				} catch (Exception e) {
					throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString(), e);
				}
				users.add(apiUser);
			}
			return Response.ok().entity(users).build();

		} catch (DcemException exp) {
			logger.info(exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}

	}

	public Response addUser(AsApiUser apiUser, SecurityContext securityContext) {
		DcemUser dcemUser = null;
		try {
			dcemUser = userLogic.getUser(apiUser.getLoginId());
			if (dcemUser == null) {
				if (apiUser.isDomain()) {
					String[] domainUser = apiUser.getLoginId().split(DcemConstants.DOMAIN_SEPERATOR_REGEX);
					if (domainUser.length > 1) {
						domainLogic.getDomainApi(domainUser[0]);
					} else {
						dcemUser = domainLogic.getUserFromDomains(apiUser.getLoginId());
						if (dcemUser == null) {
							throw new DcemException(DcemErrorCodes.INVALID_USERID, domainUser[0]);
						}
					}

					try {
						domainLogic.verifyDomainLogin(dcemUser, apiUser.getInitialPassword().getBytes(DcemConstants.UTF_8));
					} catch (DcemException e) {
						if (e.getErrorCode() == DcemErrorCodes.DOMAIN_WRONG_AUTHENTICATION) {
							throw new DcemException(DcemErrorCodes.INVALID_PASSWORD, dcemUser.getLoginId());
						} else {
							throw e;
						}
					} 
				} else {
					dcemUser = new DcemUser(apiUser.getLoginId());
					dcemUser.setDisplayName(apiUser.getDisplayName());
					try {
						DcemUtils.copyObject(apiUser, dcemUser);
					} catch (Exception e) {
						throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString(), e);
					}
				}
				if (apiUser.getPreferedLanguage() == null) {
					dcemUser.setLanguage(null);
				} else {
					dcemUser.setLanguage(DcemUtils.getSuppotedLanguage(apiUser.getPreferedLanguage()));
				}
				dcemUser.setDcemRole(roleLogic.getDcemRole(DcemConstants.SYSTEM_ROLE_USER));
				userLogic.addOrUpdateUser(dcemUser, new DcemAction(userSubject, DcemConstants.ACTION_ADD), true,
						asModule.getPreferences().isNumericActivationCode(), adminModule.getPreferences().getUserPasswordLength(), false);
			} else {
				dcemUser.setInitialPassword("");
			}
		} catch (DcemException semExp) {
			if (semExp.getErrorCode() == DcemErrorCodes.CONSTRAIN_VIOLATION_DB) {
				semExp.setErrorCode(DcemErrorCodes.USER_EXISTS_ALREADY);
			}
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(semExp)).build();
		}
		return Response.ok().entity(dcemUser.getInitialPassword()).build();
	}

	public Response getUser(String loginId, SecurityContext securityContext) {
		DcemUser dcemUser = null;
		try {
			dcemUser = userLogic.getUser(loginId);
		} catch (DcemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (dcemUser == null) {
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION)
					.entity(new DcemApiException(DcemErrorCodes.INVALID_USERID.name(), DcemErrorCodes.INVALID_USERID.name(), null)).build();
		}
		AsApiUser asApiUser = new AsApiUser();
		DcemUtils.copyObject(dcemUser, asApiUser);
		if (dcemUser.getLanguage() != null) {
			asApiUser.setPreferedLanguage(dcemUser.getLanguage().getLocale().getLanguage());
		}
		asApiUser.setDomain(dcemUser.isDomainUser());
		return Response.ok().entity(asApiUser).build();
	}

	public Response modifyUser(AsApiUser apiUser, SecurityContext securityContext) {
		try {
			DcemUser dcemUser = userLogic.getUser(apiUser.getLoginId());
			if (dcemUser == null) {
				return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION)
						.entity(new DcemApiException(DcemErrorCodes.INVALID_USERID.name(), DcemErrorCodes.INVALID_USERID.name(), null)).build();
			}
			DcemUtils.copyObject(apiUser, dcemUser);
			if (apiUser.getPreferedLanguage() == null) {
				dcemUser.setLanguage(null);
			} else {
				dcemUser.setLanguage(DcemUtils.getSuppotedLanguage(apiUser.getPreferedLanguage()));
			}
			if (apiUser.getPrivateMobileNumber() == null) {
				dcemUser.setPrivateMobileNumber(null);
			} else {
				dcemUser.setPrivateMobileNumber(apiUser.getPrivateMobileNumber());
			}
			if (dcemUser.getDcemRole() == null) {
				dcemUser.setDcemRole(roleLogic.getDcemRole(DcemConstants.SYSTEM_ROLE_USER));
			}
			userLogic.addOrUpdateUser(dcemUser, new DcemAction(userSubject, DcemConstants.ACTION_EDIT), true,
					asModule.getPreferences().isNumericActivationCode(), adminModule.getPreferences().getUserPasswordLength(), false);
		} catch (DcemException exp) {
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
		return Response.ok().build();
	}

	public Response addActivationCode(AsApiActivationCode activationCode, SecurityContext securityContext) {
		try {
			DcemUser dcemUser = userLogic.getUser(activationCode.getUserLoginId());
			if (dcemUser == null) {
				throw new DcemException(DcemErrorCodes.INVALID_USERID, activationCode.getUserLoginId());
			}
			ActivationCodeEntity asActivationCode = new ActivationCodeEntity();
			DcemUtils.copyObject(activationCode, asActivationCode);
			asActivationCode.setUser(dcemUser);
			if (asActivationCode.getValidTill() == null) {
				asActivationCode.setValidTill(LocalDateTime.now().plusHours(asModule.getPreferences().getActivationCodeDefaultValidTill()));
			}
			activationLogic.addUpdateActivationCode(asActivationCode, new DcemAction(activationSubject, DcemConstants.ACTION_ADD), activationCode.getSendBy(),
					true);
			return Response.ok().entity(asActivationCode.getActivationCode()).build();
		} catch (DcemException exp) {
			logger.warn(exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
	}

	public Response deleteUser(String loginId, SecurityContext securityContext) {
		try {
			DcemUser user = userLogic.getUser(loginId);
			if (user == null) {
				throw new DcemException(DcemErrorCodes.INVALID_USERID, loginId);
			}
			List<Object> list = new LinkedList<>();
			list.add(user);
			jpaLogic.deleteEntities(list, new DcemAction(userSubject, DcemConstants.ACTION_DELETE));
			return Response.ok().build();
		} catch (DcemException exp) {
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
	}

	public Response queryActivationCodes(List<ApiFilterItem> filterItems, Integer offset, Integer maxResults, SecurityContext securityContext) {
		JpaSelectProducer<ActivationCodeEntity> jpaSelectProducer = new JpaSelectProducer<ActivationCodeEntity>(entityManager, ActivationCodeEntity.class);
		int firstResult = 0;
		if (offset != null) {
			firstResult = offset.intValue();
		}
		int page = DcemConstants.MAX_DB_RESULTS;
		if (maxResults != null && maxResults.intValue() < page) {
			page = maxResults.intValue();
		}

		try {
			List<ActivationCodeEntity> dbActivationCodes = jpaSelectProducer.selectCriteriaQueryFilters(filterItems, firstResult, page, null);
			List<AsApiActivationCode> activationCodes = new LinkedList<>();
			for (ActivationCodeEntity activationCodeEntity : dbActivationCodes) {
				AsApiActivationCode asApiActivationCode = new AsApiActivationCode();
				asApiActivationCode.setActivationCodeId(activationCodeEntity.getId());
				asApiActivationCode.setActivationCode(activationCodeEntity.getActivationCode());
				asApiActivationCode.setUserLoginId(activationCodeEntity.getUser().getLoginId());
				asApiActivationCode.setInfo(activationCodeEntity.getInfo());
				asApiActivationCode.setValidTill(activationCodeEntity.getValidTill());
				asApiActivationCode.setCreatedOn(activationCodeEntity.getCreatedOn());
				activationCodes.add(asApiActivationCode);
			}
			return Response.ok().entity(activationCodes).build();

		} catch (DcemException exp) {
			logger.info(exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}

	}

	public Response deleteActivationCode(int activationCodeId, SecurityContext securityContext) {

		try {
			activationLogic.deleteActivationCode(activationCodeId);
			return Response.ok().build();
		} catch (DcemException exp) {
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
	}

	public Response changePassword(String userLoginId, String currentPassword, String newPassword, SecurityContext securityContext) {
		try {
			userLogic.changePassword(userLoginId, currentPassword, newPassword);
			return Response.ok().build();
		} catch (DcemException exp) {
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		} 
	}

	public Response verifyUrlToken(AsApiUrlToken urlToken, SecurityContext securityContext) {
		try {
			UrlTokenEntity urlTokenEntity = urlTokenLogic.verifyUrlToken(urlToken.getToken(), urlToken.getUrlTokenUsage().toString());
			DcemUser dcemUser = userLogic.getUser(Integer.parseInt(urlTokenEntity.getObjectIdentifier()));
			AsApiUser asApiUser = new AsApiUser();
			DcemUtils.copyObject(dcemUser, asApiUser);
			if (dcemUser.getLanguage() != null) {
				asApiUser.setPreferedLanguage(dcemUser.getLanguage().getLocale().getLanguage());
			}
			asApiUser.setDomain(dcemUser.isDomainUser());
			return Response.ok().entity(asApiUser).build();
		} catch (DcemException e) {
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(e)).build();
		}
	}

	public Response setPassword(String userLoginId, String newPassword, SecurityContext securityContext) {
		try {
			DcemUser dcemUser = userLogic.getUser(userLoginId);
			if (dcemUser == null) {
				throw new DcemException(DcemErrorCodes.INVALID_USERID, userLoginId);
			}
			userLogic.setPassword(dcemUser, newPassword);
			return Response.ok().build();
		} catch (DcemException e) {
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(e)).build();
		}
	}

	public Response addUrlToken(AsApiUrlToken urlToken, SecurityContext securityContext) {
		try {
			if (urlToken.getUrlTokenUsage() == null) {
				throw new DcemException(DcemErrorCodes.INVALID_PARAMETER, "UrlTokenUsage");
			}
			UrlTokenType usage = UrlTokenType.valueOf(urlToken.getUrlTokenUsage().toString());
			DcemUser dcemUser = userLogic.getUser(urlToken.getUsername());
			if (dcemUser == null) {
				throw new DcemException(DcemErrorCodes.INVALID_USERID, urlToken.getUsername());
			}
			UrlTokenEntity entity = urlTokenLogic.addUrlTokenToDb(usage, urlToken.getValidMinutes(), urlToken.getToken(), dcemUser.getId().toString());
			urlTokenLogic.sendUrlTokenByEmail(dcemUser, urlToken.getUrl(), entity);
			return Response.ok().build();
		} catch (DcemException exp) {
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		} catch (Exception exp) {
			logger.warn(exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(null).build();
		}
	}
}
