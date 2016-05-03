package org.fantasy.bean.asm;


import java.lang.reflect.Field;




import org.fantasy.util.ClassUtils;
import org.fantasy.util.ReflectionUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public abstract class AbstractRecursiveAnnotationVisitor extends AnnotationVisitor {

//	private static final Logger LOG = Logger.getLogger(AnnotationAttributeVisitor.class);
	protected final AnnotationAttributes attributes;
	
	public AbstractRecursiveAnnotationVisitor(AnnotationAttributes attributes) {
		super(Opcodes.ASM5);
		this.attributes = attributes;
	}

	public void visit(String attributeName, Object attributeValue) {
		attributes.put(attributeName, attributeValue);
	}
	
	public AnnotationVisitor visitAnnotation(String attributeName, String typeDescriptor) {
		String annotationType = Type.getType(typeDescriptor).getClassName();
		AnnotationAttributes nestedAttributes = new AnnotationAttributes();
		this.attributes.put(attributeName, nestedAttributes);
		return new RecursiveAnnotationAttributesVisitor(annotationType, nestedAttributes);
	}
	
	// array
	public AnnotationVisitor visitArray(String attributeName) {
		return new RecursiveAnnotationArrayVisitor(attributeName, this.attributes);
	}

	public void visitEnum(String attributeName, String typeDescriptor, String attributeValue) {
		Object newValue = getEnumValue(typeDescriptor, attributeValue);
		visit(attributeName, newValue);
	}
	
	// attributeValue => ERROR
	protected Object getEnumValue(String typeDescriptor, String attributeValue) {// asmTypeDescriptor => Lorg/butterfly/bean/Level;
		Object valueToUse = attributeValue;
		ClassLoader classLoader = ClassUtils.getClassLoader();
		try {
			Class<?> enumType = classLoader.loadClass(Type.getType(typeDescriptor).getClassName());
			Field enumConstant = ReflectionUtils.getField(enumType, attributeValue);
			if (enumConstant != null) {
				valueToUse = enumConstant.get(null);
			}
		} catch (ClassNotFoundException ex) {
		
				
		} catch (IllegalAccessException ex) {
			
		}
		
		return valueToUse;
	}


	public void visitEnd() {
	}

}
