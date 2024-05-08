package com.doubleclue.dcem.core.logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.tasks.ReloadClassInterface;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.utils.ResourceBundleUtf8Control;

public class DbResourceBundle extends ResourceBundle implements ReloadClassInterface {

	private Properties properties;

	Locale locale;
	public String bundleName;
    protected static final Control UTF8_CONTROL = new ResourceBundleUtf8Control(); 


	// public DbResourceBundle(Properties inProperties) {
	// properties = inProperties;
	// }

	// public DbResourceBundle() {
	// this(Locale.getDefault(), SystemModule.MODULE_ID);
	// }

	public DbResourceBundle() {
		String className = this.getClass().getSimpleName();
		if (className.charAt(className.length() - 3) == '_') {
			locale = new Locale(className.substring(className.length() - 2));
		} else {
			locale = new Locale("en");
		}
		reload(null);
		AdminModule adminModule = CdiUtils.getReference(AdminModule.class);
		adminModule.getAdminTenantData().getBundleCache().put(locale.getDisplayLanguage(), this);
	}

	public DbResourceBundle(Locale locale) {
		this.locale = locale;
		reload(null);
		AdminModule adminModule = CdiUtils.getReference(AdminModule.class);
		adminModule.getAdminTenantData().getBundleCache().put(locale.getDisplayLanguage(), this);
	}
	
	protected DbResourceBundle(String bundleName) {
		this();
		setParent(ResourceBundle.getBundle(bundleName, 
                FacesContext.getCurrentInstance().getViewRoot().getLocale(), UTF8_CONTROL));
	}
	
	protected DbResourceBundle(Locale locale, String bundleName) {
		this(locale);
		setParent(ResourceBundle.getBundle(bundleName, locale, UTF8_CONTROL));
	}
	

	@Override
	public void reload(String info) {
		TextResourceLogic textResourceLogic = CdiUtils.getReference(TextResourceLogic.class);
		properties = textResourceLogic.loadResourceFromDB(locale);
	}

	@Override
	protected Object handleGetObject(String key) {
		Object object = properties.getProperty(key);
		if (object == null) {
			if (locale.getLanguage().equals(Locale.ENGLISH.getLanguage()) == false) {
				DbResourceBundle resourceBundleEnglisch = DbResourceBundle.getDbResourceBundle(Locale.ENGLISH);
				try {
					object = resourceBundleEnglisch.getString(key);
				} catch (Exception e) {
					return key;
				}
			} else {
				return key;
			}
		}
		return object;
	}

	@Override
	public Enumeration getKeys() {
		return parent.getKeys();
	}

	public static void reloadResources() {
		AdminModule adminModule = CdiUtils.getReference(AdminModule.class);
		Map<String, DbResourceBundle> bundleCache = adminModule.getAdminTenantData().getBundleCache();
		for (DbResourceBundle bundle : bundleCache.values()) {
			bundle.reload(null);
		}
	}

	static public DbResourceBundle getDbResourceBundle(Locale locale) {
		AdminModule adminModule = CdiUtils.getReference(AdminModule.class);
		Map<String, DbResourceBundle> bundleCache = adminModule.getAdminTenantData().getBundleCache();
		DbResourceBundle dbResourceBundle = bundleCache.get(locale.getDisplayLanguage());
		if (dbResourceBundle == null) {
			return new DbResourceBundle(locale);
		} else {
			return dbResourceBundle;
		}
	}

	public Properties getProperties() {
		return properties;
	}

	public byte[] getPropertyContents() {
		if (properties == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(baos, Charset.forName("UTF-8"));
		try {
			properties.store(outputStreamWriter, "Dcem Exported Text Resources for: " + locale.getDisplayLanguage());
			return baos.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	// public static ResourceBundle.Control getControl() {
	//
	// return new ResourceBundle.Control() {
	//
	// @Override
	// public List<String> getFormats(String baseName) {
	// if (baseName == null) {
	// throw new NullPointerException();
	// }
	// return Arrays.asList("db");
	// }
	//
	// @Override
	// public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean
	// reload)
	// throws IllegalAccessException, InstantiationException, IOException {
	// if ((baseName == null) || (locale == null) || (format == null) || (loader == null)) {
	// throw new NullPointerException();
	// }
	// if (format.equals("db") == false) {
	// return null;
	// }
	// TextResourceLogic textResourceLogic = CdiUtils.getReference(TextResourceLogic.class);
	// Properties properties = textResourceLogic.loadResourceFromDB(baseName, locale);
	// return new DbResourceBundle(properties);
	//
	// }
	// };
	// }

}
