package net.shadowpie.sadiinso.sfc.commands.declaration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ASFCommand {
	
	String name();
	String alias() default "";
	String allowFrom() default "private/server";
	String description() default "";
	String usage() default "";
	String parentGroup() default "";
	String permissions() default "";
}
