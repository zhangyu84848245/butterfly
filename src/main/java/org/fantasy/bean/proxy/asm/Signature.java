package org.fantasy.bean.proxy.asm;

import org.objectweb.asm.Type;

public class Signature implements Comparable<Signature> {

	private String name;
	private String desc;

	public Signature(String name, String desc) {
		this.name = name;
		this.desc = desc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String toString() {
		return name + desc;
	}

	public int hashCode() {
		return ( name.hashCode() * 31 ) + ( desc.hashCode() * 31 );
	}

	public boolean equals(Object other) {
		if(other == null)
			return false;

		if(this == other)
			return true;
		
		if(getClass() != other.getClass())
			return false;

		Signature that = (Signature)other;
		
		return 
				( ( name != null && name.equals(that.getName()) ) || ( name == null && that.getName() == null ) ) && 
				( ( desc != null && desc.equals(that.getDesc()) ) || ( desc == null && that.getDesc() == null) );
	}
	
	public int compareTo(Signature other) {
		int result = name.compareTo(other.getName());
		if(result == 0)
			result = desc.compareTo(other.getDesc());
		return result;
	}

	public Type getReturnType() {
		return Type.getReturnType(desc);
	}
	
	public Type[] getArgumentTypes() {
		return Type.getArgumentTypes(desc);
	}
	
}
