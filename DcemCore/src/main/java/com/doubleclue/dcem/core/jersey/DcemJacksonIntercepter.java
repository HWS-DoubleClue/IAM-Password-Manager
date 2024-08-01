package com.doubleclue.dcem.core.jersey;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

public class DcemJacksonIntercepter extends  JacksonAnnotationIntrospector {

	private static final long serialVersionUID = 1L;
	Set<String> ignoreSet;
	
	public DcemJacksonIntercepter() {
		super();
	}

	public DcemJacksonIntercepter(HashSet<String> ignoreSet) {
		super();
		this.ignoreSet = ignoreSet;
	}
	
	@Override
	public boolean hasIgnoreMarker(AnnotatedMember annotatedMember) {
		if (ignoreSet.contains(annotatedMember.getFullName())) {
			return true;
		}
        return super.hasIgnoreMarker(annotatedMember);
    }
}
