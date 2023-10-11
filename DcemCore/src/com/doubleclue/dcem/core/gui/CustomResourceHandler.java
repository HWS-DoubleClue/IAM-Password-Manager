package com.doubleclue.dcem.core.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.time.ZoneOffset;
import java.util.Locale;

import javax.faces.FacesException;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;
import javax.faces.application.ResourceWrapper;
import javax.faces.application.ViewResource;
import javax.faces.context.FacesContext;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.sun.faces.util.Util;

/**
 * 
 *  NOT IN USE
 * 
 * Custom JSF ResourceHandler.
 * 
 * This handler bridges between Spring MVC and JSF managed resources. The
 * handler takes care of the case when a JSF facelet is used as a view by a
 * Spring MVC Controller and the view uses components like h:outputScript and
 * h:outputStylesheet by correctly pointing the resource URLs generated to the
 * JSF resource handler.
 * 
 * The reason this custom handler wrapper is needed is because the JSF internal
 * logic assumes that the request URL for the current page/view is a JSF url. If
 * it is a Spring MVC request, JSF will create URLs that incorrectly includes
 * the Spring controller context.
 * 
 * This handler will strip out the Spring context for the URL and add the ".jsf"
 * suffix, so the resource request will be routed to the FacesServlet with a
 * correct resource context (assuming the faces servlet is mapped to the *.jsf
 * pattern).
 * 
 * 
 */
public class CustomResourceHandler extends ResourceHandlerWrapper {

	private static final String RESOURCE_POSTFIX = ".jsf";

	private ResourceHandler wrapped;

	public CustomResourceHandler(ResourceHandler wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public ResourceHandler getWrapped() {
		return this.wrapped;
	}

	@Override
	public ViewResource createViewResource(FacesContext context, final String name) {
		ViewResource viewResource = wrapped.createViewResource(context, name);
		if (viewResource == null) {

			if (name.startsWith("/dcdb_")) {
				try {
					Locale browserLocale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();

					final URL url = new URL(null, DcemConstants.DCDB_URL_PROTOCOL + ":" + name + "/" + browserLocale, new MyCustomHandler());

					// JsfUtils.refreshCurrentPage();
					return new ViewResource() {
						@Override
						public URL getURL() {
							return url;
						}
					};
				} catch (IOException e) {
					throw new FacesException(e);
				}
			}
		}
		return viewResource;
	}

	@Override
	public Resource createResource(String resourceName, String libraryName) {
		// System.out.println("CustomResourceHandler.createResource() " + resourceName);
		Resource wrapped = super.createResource(resourceName, libraryName);

		if (resourceName.endsWith(RESOURCE_POSTFIX)) {
			return wrapped;
		}

		return new CustomResource(super.createResource(resourceName, libraryName));
	}

	@Override
	public Resource createResource(String resourceName, String libraryName, String contentType) {
		Resource wrapped = super.createResource(resourceName, libraryName, contentType);
		if (resourceName.endsWith(RESOURCE_POSTFIX)) {
			return wrapped;
		}
		return new CustomResource(super.createResource(resourceName, libraryName, contentType));
	}

	private static class CustomResource extends ResourceWrapper {

		private Resource wrapped;

		private CustomResource(Resource wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public Resource getWrapped() {
			return this.wrapped;
		}

		@Override
		public String getRequestPath() {
			String path = super.getRequestPath();
			FacesContext context = FacesContext.getCurrentInstance();
			String facesServletMapping = Util.getFacesMapping(context);
			// if prefix-mapped, this is a resource that is requested from a
			// faces page
			// rendered as a view to a Spring MVC controller.
			// facesServletMapping will, in fact, be the Spring mapping
			if (Util.isPrefixMapped(facesServletMapping)) {
				// remove the Spring mapping
				path = path.replaceFirst("(" + facesServletMapping + ")/", "/");
				// append .jsf to route this URL to the FacesServlet
				path = path.replace(wrapped.getResourceName(), wrapped.getResourceName() + RESOURCE_POSTFIX);
			}
			return path;
		}
	}

	static class MyCustomHandler extends URLStreamHandler {

		public MyCustomHandler() {
		}

		@Override
		protected URLConnection openConnection(URL url) throws IOException {
			return new UserURLConnection(url);
		}

		private static class UserURLConnection extends URLConnection {

			private URL url;			
			long lastModified;

			public UserURLConnection(URL url) {
				super(url);
				this.url = url;
			}

			@Override
			public void connect() throws IOException {
			}

			@Override
	        public InputStream getInputStream() throws IOException {
	        	WeldRequestContext requestContext = null;
	        
	        	try {
	    			requestContext = WeldContextUtils.activateRequestContext();

	    			TemplateLogic templateLogic = CdiUtils.getReference(TemplateLogic.class);
	    			String templateName = url.getPath().substring(DcemConstants.DCDB_URL_PROTOCOL.length() + 2);
	    			int localeIndex = templateName.lastIndexOf('/');
	    			SupportedLanguage language = null;
	    			if (localeIndex > 0) {
	    				language = DcemUtils.getSuppotedLanguage(templateName.substring(localeIndex + 1));
	    				templateName = templateName.substring(0, localeIndex - 6);
	    			} else {
	    				templateName = templateName.substring(0, 6);
	    			}

	    			ByteArrayInputStream byteArrayInputStream;
	    			DcemTemplate dcemTemplate = templateLogic.getTemplateByNameLanguage(templateName, language);
	    			if (dcemTemplate != null) {
	    				lastModified = dcemTemplate.getLastModified().toEpochSecond(ZoneOffset.UTC) * 1000;
	    				byteArrayInputStream = new ByteArrayInputStream(dcemTemplate.getContent().getBytes());
	    				return byteArrayInputStream;
	    			}
	        	} catch (Throwable t) {
	    			t.printStackTrace();
	    		} finally {
	    			WeldContextUtils.deactivateRequestContext(requestContext);
	    		}
	        	
	        	return null;
	        }

			@Override
			public long getLastModified() {
				return lastModified;
			}

		}

	}
}