package com.doubleclue.dcem.core.logic;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.cache.TemplateLoader;
import freemarker.template.utility.StringUtil;

public class DcFreeMarkerStringLoader implements TemplateLoader {

	/**
	 * A {@link TemplateLoader} that uses a {@link Map} with {@link String}-s as its source of 
	 * templates.
	 *
	 * <p>In most case the regular way of loading templates from files will be fine.
	 * However, there can be situations where you don't want to or can't load a
	 * template from a file, e.g. if you have to deploy a single jar for 
	 * JavaWebStart or if they are contained within a database.
	 * A single template can be created manually
	 * e.g.
	 * <pre>
	 *   String templateStr="Hello ${user}";
	 *   Template t = new Template("name", new StringReader(templateStr),
	 *               new Configuration());
	 * </pre>
	 * <p>If, however, you want to create templates from strings which import other 
	 * templates this method doesn't work.
	 *
	 * <p>In that case you can create a StringTemplateLoader and add each template to 
	 * it:
	 * <pre>
	 *   StringTemplateLoader stringLoader = new StringTemplateLoader();
	 *   stringLoader.putTemplate("greetTemplate", "&lt;#macro greet&gt;Hello&lt;/#macro&gt;");
	 *   stringLoader.putTemplate("myTemplate", "&lt;#include \"greetTemplate\"&gt;&lt;@greet/&gt; World!");
	 * </pre>
	 * <p>Then you tell your Configuration object to use it:
	 * <pre>
	 *   cfg.setTemplateLoader(stringLoader);
	 * </pre>
	 * <p>After that you should be able to use the templates as usual. Often you will
	 * want to combine a <tt>StringTemplateLoader</tt> with another loader. You can
	 * do so using a {@link freemarker.cache.MultiTemplateLoader}.
	 */

	private final Map<String, DcTemplateSource> templates = new HashMap<>();

	/**
	 * Puts a template into the loader. A call to this method is identical to 
	 * the call to the three-arg {@link #putTemplate(String, String, long)} 
	 * passing <tt>System.currentTimeMillis()</tt> as the third argument.
	 * 
	 * <p>Note that this method is not thread safe! Don't call it after FreeMarker has started using this template
	 * loader.
	 * 
	 * @param name the name of the template.
	 * @param templateContent the source code of the template.
	 */
	public void putTemplate(String name, String templateContent) {
		putTemplate(name, templateContent, System.currentTimeMillis());
	}

	/**
	 * Puts a template into the loader. The name can contain slashes to denote
	 * logical directory structure, but must not start with a slash. If the 
	 * method is called multiple times for the same name and with different
	 * last modified time, the configuration's template cache will reload the 
	 * template according to its own refresh settings (note that if the refresh 
	 * is disabled in the template cache, the template will not be reloaded).
	 * Also, since the cache uses lastModified to trigger reloads, calling the
	 * method with different source and identical timestamp won't trigger
	 * reloading.
	 * 
	 * <p>Note that this method is not thread safe! Don't call it after FreeMarker has started using this template
	 * loader.
	 * 
	 * @param name the name of the template.
	 * @param templateContent the source code of the template.
	 * @param lastModified the time of last modification of the template in 
	 * terms of <tt>System.currentTimeMillis()</tt>
	 */
	public void putTemplate(String name, String templateContent, long lastModified) {
		templates.put(name, new DcTemplateSource(name, templateContent, lastModified));
	}

	/**
	 * Removes the template with the specified name if it was added earlier.
	 * 
	 * <p>Note that this method is not thread safe! Don't call it after FreeMarker has started using this template
	 * loader.
	 * 
	 * @param name Exactly the key with which the template was added.
	 * 
	 * @return Whether a template was found with the given key (and hence was removed now) 
	 * 
	 * @since 2.3.24
	 */
	public boolean removeTemplate(String name) {
		return templates.remove(name) != null;
	}

	@Override
	public void closeTemplateSource(Object templateSource) {
	}

	@Override
	public Object findTemplateSource(String name) {
		return templates.get(name);
	}

	@Override
	public long getLastModified(Object templateSource) {
		return ((DcTemplateSource) templateSource).lastModified;
	}

	@Override
	public Reader getReader(Object templateSource, String encoding) {
		return new StringReader(((DcTemplateSource) templateSource).templateContent);
	}

	/**
	 * Show class name and some details that are useful in template-not-found errors.
	 * 
	 * @since 2.3.21
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(Map { ");
		int cnt = 0;
		for (String name : templates.keySet()) {
			cnt++;
			if (cnt != 1) {
				sb.append(", ");
			}
			if (cnt > 10) {
				sb.append("...");
				break;
			}
			sb.append(StringUtil.jQuote(name));
			sb.append("=...");
		}
		if (cnt != 0) {
			sb.append(' ');
		}
		sb.append("})");
		return sb.toString();
	}

	public Map<String, DcTemplateSource> getTemplates() {
		return templates;
	}

	public void updataTemplateCache(long lastModified) {
		List<String> removingList = new ArrayList<>();
		for (DcTemplateSource templateSource : templates.values()) {
			if (templateSource.lastModified < lastModified) {
				removingList.add(templateSource.name);
			}
		}
		for (String name : removingList) {
			templates.remove(name);
		}
	}

	private class DcTemplateSource {
		private final String name;
		private final String templateContent;
		private final long lastModified;

		DcTemplateSource(String name, String templateContent, long lastModified) {
			if (name == null) {
				throw new IllegalArgumentException("name == null");
			}
			if (templateContent == null) {
				throw new IllegalArgumentException("source == null");
			}
			if (lastModified < -1L) {
				throw new IllegalArgumentException("lastModified < -1L");
			}
			this.name = name;
			this.templateContent = templateContent;
			this.lastModified = lastModified;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DcTemplateSource other = (DcTemplateSource) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return name;
		}
	}

}
