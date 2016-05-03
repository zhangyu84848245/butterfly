package org.fantasy.bean.asm;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.fantasy.util.ObjectUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

public class RecursiveAnnotationArrayVisitor extends AbstractRecursiveAnnotationVisitor {

	private final String attributeName;
	private final List<AnnotationAttributes> allNestedAttributes = new ArrayList<AnnotationAttributes>();

	public RecursiveAnnotationArrayVisitor(String attributeName, AnnotationAttributes attributes) {
		super(attributes);
		this.attributeName = attributeName;
	}


	public void visit(String attributeNameIsNull, Object attributeValue) {
		Object newValue = attributeValue;
		Object existingValue = this.attributes.get(this.attributeName);
		if(existingValue != null) {
			newValue = ObjectUtils.addObjectToArray((Object[]) existingValue, newValue);
		} else {
			Class<?> arrayClass = newValue.getClass();
			if(Enum.class.isAssignableFrom(arrayClass)) {
				while(arrayClass.getSuperclass() != null && !arrayClass.isEnum()) {
					arrayClass = arrayClass.getSuperclass();
				}
			}
			Object[] newArray = (Object[]) Array.newInstance(arrayClass, 1);
			newArray[0] = newValue;
			newValue = newArray;
		}
		this.attributes.put(this.attributeName, newValue);
	}
	
	// [@Test(target=@Target({ElementType.TYPE}))]
	public AnnotationVisitor visitAnnotation(String attributeNameIsNull, String typeDescriptor) {
		String annotationType = Type.getType(typeDescriptor).getClassName();
		AnnotationAttributes nestedAttributes = new AnnotationAttributes();
		this.allNestedAttributes.add(nestedAttributes);
		return new RecursiveAnnotationAttributesVisitor(annotationType, nestedAttributes);
	}

	public void visitEnd() {
		if (!this.allNestedAttributes.isEmpty()) {
			this.attributes.put(this.attributeName, this.allNestedAttributes.toArray(new AnnotationAttributes[this.allNestedAttributes.size()]));
		}
	}

}
