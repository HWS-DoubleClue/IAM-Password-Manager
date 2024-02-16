package com.doubleclue.dcem.core.entities;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

@MappedSuperclass
public abstract class EntityAbstract  {
	

	

	@Version
	private int jpaVersion;
	
	public int getJpaVersion() {
		return jpaVersion;
	}

	public void setJpaVersion(int jpaVersion) {
		this.jpaVersion = jpaVersion;
	}
	
	public String getRowStyle() {
		return null;
	}
	
	

//	@Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((id == null) ? 0 : id.hashCode());
//        return result;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
////        Grupo other = (Grupo) obj;
//        if (id == null) {
//            if (((EntityInterface)obj).getId() != null)
//                return false;
//        } else if (!id.equals(((EntityInterface)obj).getId()))
//            return false;
//        return true;
//    }

	
}
