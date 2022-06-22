package com.doubleclue.portaldemo.gui;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.portaldemo.AbstractPortalView;
import com.doubleclue.portaldemo.PortalDemoConfig;
import com.doubleclue.portaldemo.radius.AccessRequest;
import com.doubleclue.portaldemo.radius.RadiusAttribute;
import com.doubleclue.portaldemo.radius.RadiusAttributeEnum;
import com.doubleclue.portaldemo.radius.RadiusClient;
import com.doubleclue.portaldemo.radius.RadiusPacket;
import com.doubleclue.portaldemo.utils.ConfigUtil;
import com.doubleclue.portaldemo.utils.JsfUtils;

@SuppressWarnings("serial")
@Named("loginRadiusView")
@SessionScoped
public class LoginRadiusView extends AbstractPortalView {

	String name;
	String password;

	PortalDemoConfig portalDemoConfig;

	RadiusClient rc;

	String challengeText;

	RadiusPacket pendingResponse;

	AsClientRestApi clientRestApi = AsClientRestApi.getInstance();

	@PostConstruct
	public void init() {
		try {
			portalDemoConfig = ConfigUtil.getPortalDemoConfig();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPath() {
		return "radiusLogin.xhtml";
	}

	public void actionLogin() {
		try {
			portalDemoConfig = ConfigUtil.getPortalDemoConfig();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		rc = new RadiusClient(portalDemoConfig.getRadiusHost(), portalDemoConfig.getRadiusSharedSecret());
		rc.setAuthPort(portalDemoConfig.getRadiusPort());
		if (name.isEmpty()) {
			JsfUtils.addErrorMessage("Please give a login name");
		}
		// JsfUtils.addErrorMessage("Not implemented yet!");
		AccessRequest ar = new AccessRequest(name, password);
		ar.setAuthProtocol(AccessRequest.AUTH_PAP); // or AUTH_CHAP

		RadiusPacket response;
		try {
			pendingResponse = null;
			rc.setSocketTimeout(portalDemoConfig.getRadiusTimeout());
			response = rc.authenticate(ar);
			switch (response.getRadiusPacketType()) {
			case ACCESS_REJECT:
				RadiusAttribute attribute = response.getAttribute(RadiusAttributeEnum.ReplyMessage.getType());
				if (attribute != null) {
					challengeText = attribute.getAttributeValue();
				} else {
					challengeText = "";
				}
				throw new Exception("Authentication rejected: " + challengeText);
			case ACCESS_CHALLENGE:
				pendingResponse = response;
				challengeText = response.getAttribute(RadiusAttributeEnum.ReplyMessage.getType()).getAttributeValue();
				PrimeFaces.current().executeScript("PF('challengeDlg').show();");
			case ACCESS_ACCEPT:
				JsfUtils.addInfoMessage("Login succesful");
				break;
			default:
				throw new Exception("Waiting for Radius Challenge: Invalid Packet Typre received.");
			}

		} catch (Exception e) {
			JsfUtils.addErrorMessage("Error: " + e.getMessage());
		}

	}

	public void actionChallenge() {
		RadiusAttribute attr = pendingResponse.getAttribute(RadiusAttributeEnum.State);
		AccessRequest ar = new AccessRequest(name, password);
		ar.setAuthProtocol(AccessRequest.AUTH_PAP); // or AUTH_CHAP
		ar.addAttribute(attr);
		RadiusPacket response;
		
		try {
			response = rc.authenticate(ar);

			switch (response.getRadiusPacketType()) {
			case ACCESS_REJECT:
				PrimeFaces.current().executeScript("PF('challengeDlg').hide();");
				challengeText = response.getAttribute(RadiusAttributeEnum.ReplyMessage.getType()).getAttributeValue();
				throw new Exception("Authentication rejected: " + challengeText);
			case ACCESS_CHALLENGE:
				pendingResponse = response;
				challengeText = response.getAttribute(RadiusAttributeEnum.ReplyMessage.getType()).getAttributeValue();
				PrimeFaces.current().executeScript("PF('challengeDlg').show();");
				break;
			case ACCESS_ACCEPT:
				PrimeFaces.current().executeScript("PF('challengeDlg').hide();");
				JsfUtils.addInfoMessage("Login succesful");
				break;
			default:
				throw new Exception("Waiting for Radius Challenge: Invalid Packet Typre received.");
			}
		} catch (Exception e) {
			PrimeFaces.current().executeScript("PF('challengeDlg').hide();");
			JsfUtils.addErrorMessage("Error: " + e.getMessage());
		}
	}

	public String getChallengeText() {
		return challengeText;
	}

	public void setChallengeText(String challengeText) {
		this.challengeText = challengeText;
	}
}
