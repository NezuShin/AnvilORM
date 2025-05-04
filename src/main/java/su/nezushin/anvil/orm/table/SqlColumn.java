package su.nezushin.anvil.orm.table;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import su.nezushin.anvil.orm.SqlFlag;
import su.nezushin.anvil.orm.SqlType;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SqlColumn {
	
	public final static String defaultName = "-default";
	
	public SqlFlag[] flags() default {};
	
	public SqlType type();
	
	public String name() default "-default";
	
}
