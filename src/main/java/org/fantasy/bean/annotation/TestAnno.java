package org.fantasy.bean.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TestAnno {

	OneAnno[] annos() default {@OneAnno(test="hello"), @OneAnno(test="world"), @OneAnno(test="abc")};
	
	OneAnno one() default @OneAnno(test="2016-02-19");
	
	public String str();
	
	public int ix() default 47;
}
