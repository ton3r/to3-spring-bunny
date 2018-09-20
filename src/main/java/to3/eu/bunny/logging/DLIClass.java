package to3.eu.bunny.logging;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE) // on class level
@Inherited
public @interface DLIClass {

	Class <? extends DefaultLoggingInterface> loggingClass() default DefaultLoggingInterface.class;
}
