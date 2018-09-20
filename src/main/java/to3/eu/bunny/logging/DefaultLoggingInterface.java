package to3.eu.bunny.logging;


import java.lang.annotation.Annotation;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface DefaultLoggingInterface {

	default Class <? extends DefaultLoggingInterface> getLoggingClass() {
		@SuppressWarnings ("unchecked")
		final Class <DefaultLoggingInterface> t = (Class <DefaultLoggingInterface>) this.getClass();

		Class <? extends DefaultLoggingInterface> loggingClass = DefaultLoggingInterface.class;

		if ( t.isAnnotationPresent(DLIClass.class) ) {
			final Annotation annotation = t.getAnnotation(DLIClass.class);
			final DLIClass dliClass = (DLIClass) annotation;
			loggingClass = dliClass.loggingClass();
		}

		return loggingClass;
	}

	default Logger getLogger() {
		return getLoggerByAnnotation();
		// return getLoggerByString();
	}

	default Logger getLoggerByAnnotation() {
		return LoggerFactory.getLogger(getLoggingClass());
	}

	default Logger getLogger(DefaultLoggingInterface dli) {
		return LoggerFactory.getLogger(dli.getClass());
	}

	default Logger getLoggerByString() {
		return LoggerFactory.getLogger(getLoggingClassName());
	}

	default String getLoggingClassName() {
		return this.getClass().getName();
	}

	@PostConstruct
	default void initLogger() {
		if ( getLogger().isInfoEnabled() ) {
			getLogger().info("\t\t ==> " + getLoggingClassName() + " [" + this.hashCode() + "] logging is initialized!");
		}
	}
}
