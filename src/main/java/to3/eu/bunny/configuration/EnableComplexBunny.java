package to3.eu.bunny.configuration;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Import;

import to3.eu.bunny.config.BunnyConfig;
import to3.eu.bunny.config.ComplexBunnyConfiguration;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Import(value = { BunnyConfig.class, ComplexBunnyConfiguration.class })
@EnableRabbit
public @interface EnableComplexBunny {

}
