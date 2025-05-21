package com.doubleclue.dcem.dm.logic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ResourceBundle;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.gui.EndMessageView;
import com.doubleclue.dcem.admin.gui.ForgotPasswordView;
import com.doubleclue.dcem.admin.gui.RegisterView;
import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.entities.UrlTokenEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.UrlTokenLogic;
import com.doubleclue.dcem.core.logic.UrlTokenType;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.utils.KaraUtils;

@WebServlet(name = "pdfDownloadServlet")
@RequestScoped
public class DmDownloadServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	private DocumentLogic documentLogic;

	private static final Logger logger = LogManager.getLogger(DmDownloadServlet.class);

	// private AsClientRestApi clientRestApi = AsClientRestApi.getInstance();

	@Override
	public void init() throws ServletException {
		System.out.println("PdfDownloadServlet.init()");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("DCUP - Received Request via GET: " + request.getQueryString());
		}
		String documentId = request.getParameter("documentId");
		response.setHeader("Access-Control-Allow-Origin", "*");
		if (documentId == null) {
			redirectToErrorPage(request, response, "", "No document id specified");
			return;
		}
		try {
			OutputStream outputStream = response.getOutputStream();
			documentLogic.convertDocumentToPdfStream(documentId, outputStream);
		} catch (Exception exp) {
			logger.error(documentId, exp);
			redirectToErrorPage(request, response, "Document Management", "Upps something get wrong. Cannot getDocuemnt");
			return;
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("DCUP - Received Request via POST: " + request.getParameterMap().toString());
		}
		doGet(request, response);
	}

	private void redirectToErrorPage(HttpServletRequest request, HttpServletResponse response, String title, String errorMessage) throws IOException {
		logger.error(title + "  \n" + errorMessage);
		// forgotPasswordErrorView.setTitle(title);
		// forgotPasswordErrorView.setMessage(errorMessage);
		// forgotPasswordErrorView.setError(true);
		response.sendRedirect(request.getContextPath() + DcemConstants.WEB_MGT_CONTEXT + "/error_.xhtml");
	}
}
