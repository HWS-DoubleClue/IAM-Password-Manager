package com.doubleclue.dcem.as.gui;

import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.as.comm.client.ProxyCommClient;
import com.doubleclue.dcem.as.comm.client.RpReport;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.subjects.ReverseProxySubject;

@SuppressWarnings("serial")
@Named("reverseProxyView")
@SessionScoped
public class ReverseProxyView extends DcemView {

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	ReverseProxySubject reverseProxySubject;

	@Inject
	ProxyCommClient proxyCommClient;

	@Inject
	ReverseProxyDialog reverseProxyDialog;

	@PostConstruct
	private void init() {

		subject = reverseProxySubject;
		reverseProxyDialog.setParentView(this);

		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		// ResourceBundle asResourceBundle = JsfUtils.getBundle(AsModule.RESOURCE_NAME);
		addAutoViewAction(DcemConstants.ACTION_CONFIGURE, resourceBundle, reverseProxyDialog,
				AsConstants.REVERSE_PROXY_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_CLEAR_LOG, resourceBundle, reverseProxyDialog, null);

	}

	@Override
	public void reload() {
		autoViewBean.reload();
	}

	public String getState() {
		return proxyCommClient.getState().name();
	}

	public List<RpReport> getReports() {
		return proxyCommClient.getReportList();
	}

	public void clear() {
		proxyCommClient.clearReportList();
	}

	public int getConnections() {
		return proxyCommClient.getConnections();
	}

}
