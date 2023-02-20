package com.doubleclue.dcem.core.as;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.doubleclue.dcem.admin.gui.WelcomeView.SelectedFormat;
import com.doubleclue.dcem.admin.logic.SendByEnum;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.logic.JndiProxyParam;

public interface AsModuleApi {
	public String createActivationCode(DcemUser dcemUser, Date validTill, SendByEnum sendBy, String info) throws DcemException;

	public Date getActivationCodeDefaultValidTill();

	public AuthenticateResponse authenticate(AuthApplication authApplication, int subId, String userLoginId, AuthMethod authMethod, String password, String passcode, AuthRequestParam requestParam)
			throws DcemException;

	public AsMessageResponse getMessageResponse(Long msgId, int pollInterval) throws DcemException;

	public void cancelMessage(long msgId) throws DcemException;

	public QrCodeResponse requestQrCode(String source, String sessionId) throws DcemException;

	public void resetStayLogin(DcemUser user) throws DcemException;

	public void modifiedUser(DcemUser preUser, DcemUser newUser);

	public void onCreateTenant(TenantEntity tenantEntity) throws Exception;

	public String onCreateActivationCodeTenant(TenantEntity tenantEntity, String email, String phone, SendByEnum activationCodeSendBy, SupportedLanguage supportedLanguage, boolean sendPasswordBySms,
			String superAdminPassword, String loginId, boolean selfCreateTenant) throws Exception;

	public void onRecoverSuperAdminAccess(TenantEntity tenantEntity) throws Exception;

	public QueryLoginResponse queryLoginQrCode(String source, String sessionId, boolean pollOnly, int waitMaxTime) throws DcemException;

	public void setUserCloudSafe(String name, String options, Date discardAfter, DcemUser dcemUser, boolean withAuditing, char[] password, byte[] content) throws DcemException;

	public List<AuthMethod> getAllowedAuthMethods(AuthApplication authApplication, int subId, DcemUser dcemUser);

	public void closeAuthProxyConnection(JndiProxyParam proxyParam);

	public void sendDataAuthProxy(JndiProxyParam proxyParam, byte[] data, int offset, int length) throws Exception;

	public JndiProxyParam openAuthProxyConnection(String authConnectorName, String host, int port, boolean secure, boolean verifCertificate, AuthProxyListener authProxyListener) throws DcemException;

	public List<String> getAuthConnectorNames();

	public void killUserDevices(DcemUser dcemUser);
	
	public long getCloudSafeUsageMb();
}