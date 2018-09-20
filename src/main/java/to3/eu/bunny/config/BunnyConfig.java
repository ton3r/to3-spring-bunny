package to3.eu.bunny.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:bunny.properties")
@ConfigurationProperties(prefix = "bunny")
public class BunnyConfig {
	private String ampqTaskTopicExchangeName;
	private String ampqTaskQueueName;
	private String ampqTaskRouteKey;

	private int ampqTaskMaxPrefetch;

	private String ampqRetryTopicExchangeName;
	private String ampqRetryQueueName;
	private String ampqRetryRouteKey;

	public int getAmpqTaskMaxPrefetch() {
		return ampqTaskMaxPrefetch;
	}

	public void setAmpqTaskMaxPrefetch(int ampqTaskMaxPrefetch) {
		this.ampqTaskMaxPrefetch = ampqTaskMaxPrefetch;
	}

	public String getAmpqRetryTopicExchangeName() {
		return ampqRetryTopicExchangeName;
	}

	public void setAmpqRetryTopicExchangeName(String ampqRetryTopicExchangeName) {
		this.ampqRetryTopicExchangeName = ampqRetryTopicExchangeName;
	}

	public String getAmpqRetryQueueName() {
		return ampqRetryQueueName;
	}

	public void setAmpqRetryQueueName(String ampqRetryQueueName) {
		this.ampqRetryQueueName = ampqRetryQueueName;
	}

	public String getAmpqRetryRouteKey() {
		return ampqRetryRouteKey;
	}

	public void setAmpqRetryRouteKey(String ampqRetryRouteKey) {
		this.ampqRetryRouteKey = ampqRetryRouteKey;
	}

	public String getAmpqTaskTopicExchangeName() {
		return ampqTaskTopicExchangeName;
	}

	public void setAmpqTaskTopicExchangeName(String ampqTaskTopicExchangeName) {
		this.ampqTaskTopicExchangeName = ampqTaskTopicExchangeName;
	}

	public String getAmpqTaskQueueName() {
		return ampqTaskQueueName;
	}

	public void setAmpqTaskQueueName(String ampqTaskQueueName) {
		this.ampqTaskQueueName = ampqTaskQueueName;
	}

	public String getAmpqTaskRouteKey() {
		return ampqTaskRouteKey;
	}

	public void setAmpqTaskRouteKey(String ampqTaskRouteKey) {
		this.ampqTaskRouteKey = ampqTaskRouteKey;
	}

}