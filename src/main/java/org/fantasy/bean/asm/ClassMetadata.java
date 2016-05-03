package org.fantasy.bean.asm;

public interface ClassMetadata {

	public String getClassName();
	
	public boolean isInterface();
	
	public boolean isAbstract();
	
	public boolean isConcrete();
	
	public boolean isFinal();
	
	public boolean hasSuperClass();
	
	public String getSuperClassName();
	
	public String[] getInterfaceNames();

}
