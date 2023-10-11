package com.doubleclue.dcem.core.entities;

import com.doubleclue.dcem.core.gui.SupportedLanguage;
import java.time.LocalDateTime;
import java.util.LinkedList;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2023-10-11T10:54:25.089+0200")
@StaticMetamodel(DcemTemplate.class)
public class DcemTemplate_ {
	public static volatile SingularAttribute<DcemTemplate, Integer> id;
	public static volatile SingularAttribute<DcemTemplate, String> name;
	public static volatile SingularAttribute<DcemTemplate, SupportedLanguage> language;
	public static volatile SingularAttribute<DcemTemplate, Integer> version;
	public static volatile SingularAttribute<DcemTemplate, Boolean> inUse;
	public static volatile SingularAttribute<DcemTemplate, Boolean> defaultTemplate;
	public static volatile SingularAttribute<DcemTemplate, byte[]> macDigest;
	public static volatile SingularAttribute<DcemTemplate, String> content;
	public static volatile SingularAttribute<DcemTemplate, LinkedList> tokens;
	public static volatile SingularAttribute<DcemTemplate, LocalDateTime> lastModified;
	public static volatile SingularAttribute<DcemTemplate, Boolean> active;
	public static volatile SingularAttribute<DcemTemplate, Integer> jpaVersion;
}
