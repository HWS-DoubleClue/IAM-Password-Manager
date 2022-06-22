package com.doubleclue.portaldemo.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ConnectException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.as.restapi.model.AsApiMsgStatus;
import com.doubleclue.as.restapi.model.DcemApiException;
import com.doubleclue.as.restapi.model.QueryLoginResponse;
import com.doubleclue.as.restapi.model.RequestLoginQrCodeResponse;
import com.doubleclue.portaldemo.AbstractPortalView;
import com.doubleclue.portaldemo.PortalDemoConstants;
import com.doubleclue.portaldemo.PortalSessionBean;
import com.doubleclue.portaldemo.utils.JsfUtils;
import com.doubleclue.portaldemo.utils.QrCodeUtils;

@SuppressWarnings("serial")
@Named("loginQrCodeView")
@SessionScoped
public class LoginQrCodeView extends AbstractPortalView {

	@Inject
	private PortalSessionBean portalSessionBean;

	// private static Logger logger = LogManager.getLogger(LoginQrCodeView.class);

	final static int MAX_SESSION_QR_CODE = 5 * 60;

	String name;

	private StreamedContent qrCodeContent;

	RequestLoginQrCodeResponse requestLoginQrCodeResponse;

	AsApiMsgStatus msgStatus;

	AsClientRestApi clientRestApi = AsClientRestApi.getInstance();

	// Stop creating and polling qr Code after this limit
	long sessionStarted;

	boolean stop = true;

	boolean newQrCodeRequired = false;

	String code;

	// int timeStampNewQrCode;()

	@PostConstruct
	public void init() {
		if (requestLoginQrCodeResponse == null) {
			try {
				requestNewQrCode();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void checkQrCode() {
		if (stop == false) {
			try {
				if (requestLoginQrCodeResponse == null) {
					requestNewQrCode();
				}
				QueryLoginResponse response = clientRestApi.queryLoginQrCode(JsfUtils.getSessionId(), false, PortalDemoConstants.WAIT_INTERVAL_MILLI_SECONDS);
				if (response.getUserLoginId() != null) {
					portalSessionBean.setLoggedIn(true);
					portalSessionBean.setUserName(response.getUserLoginId());
					portalSessionBean.setDeviceName(response.getDeviceName());
					onComplete();
				}

			} catch (DcemApiException e) {
				if (e.getCode() == 36) {
					stopQrCode();
				} else {
					JsfUtils.addErrorMessage(e.toString());
				}

			} catch (Exception exp) {
				JsfUtils.addErrorMessage(exp.toString());
			}
		}
	}

	private void onComplete() {
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		if (portalSessionBean.isLoggedIn()) {
			try {
				ec.redirect("welcome.xhtml");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				ec.redirect("qrCodelogin.xhtml");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		requestLoginQrCodeResponse = null;
		newQrCodeRequired = true;
	}

	public StreamedContent getQrCodeImage() {
		if (stop == false) {
			try {
				if (requestLoginQrCodeResponse == null) {
					return null;
				}
				byte[] image = QrCodeUtils.createQRCode(requestLoginQrCodeResponse.getData(), 200, 200);
				qrCodeContent = DefaultStreamedContent.builder().contentType("image/png")
						.stream(() -> new ByteArrayInputStream(image)).build();
				return qrCodeContent;

			} catch (Exception exp) {
				if (exp.getCause() instanceof ConnectException) {
					JsfUtils.addErrorMessage("Connection to Server failed, please check connection parameters. Go to portalConfig.xhtml.");
					return null;
				}
				JsfUtils.addErrorMessage(exp.toString());
			}
		}
		return null;

	}

	public void requestNewQrCode() throws Exception {
		requestLoginQrCodeResponse = clientRestApi.requestLoginQrCode(JsfUtils.getSessionId());
		stop = false;
		newQrCodeRequired = false;
	}

	public int getTimeToLive() {
		if (requestLoginQrCodeResponse != null) {
			return requestLoginQrCodeResponse.getTimeToLive();
		}
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void stopQrCode() {
		stop = true;
		requestLoginQrCodeResponse = null;
		PrimeFaces.current().ajax().update("loginForm:qrcodePanel");
	}
	
	public boolean isStop() {
		return stop;
	}

}
