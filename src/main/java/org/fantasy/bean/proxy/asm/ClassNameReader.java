package org.fantasy.bean.proxy.asm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class ClassNameReader {

	
	private ClassNameReader() {
		
	}
	
	public static String getClassName(ClassReader cr) {
		return getClassInfo(cr).get(0);
	}
	
	
	public static List<String> getClassInfo(ClassReader cr) {
		final List<String> array = new ArrayList<String>();
		try {
			
			cr.accept(new ClassVisitor(Opcodes.ASM5, null) {
				public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
					array.add( name.replace('/', '.') );
	        		
	                if(superName != null){
	                	array.add( superName.replace('/', '.') );
	                }
	                
	                for(int i = 0; i < interfaces.length; i++  ){
	                   array.add( interfaces[i].replace('/', '.') );
	                }
	                
	                throw new RuntimeException();
				}
			}, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		} catch(Throwable ex) {

		}
		
		return array;
	}
}
