package to3.eu.bunny.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import to3.eu.bunny.logging.DefaultLoggingInterface;

@Configuration
public class BunnyConfiguration implements RabbitListenerConfigurer, DefaultLoggingInterface {
	private static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
	private static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
	private static final String X_MESSAGE_TTL = "x-message-ttl";
	private static final String DEADLETTER = "-deadletter";
	@Autowired
	private BunnyConfig bunnyConfig;

	@Bean
	TopicExchange exchange() {
		getLogger().info("Register topic exchange {}", bunnyConfig.getAmpqTaskTopicExchangeName());
		return new TopicExchange(bunnyConfig.getAmpqTaskTopicExchangeName());
	}

	/**
	 * a queue for sending plain object which will get serialized in json
	 * 
	 * @return
	 */
	@Bean
	@Profile("default")
	Queue queue() {
		getLogger().info("Register queue {}", bunnyConfig.getAmpqTaskQueueName());
		Map<String, Object> args = new HashMap<>();
		args.put(X_MESSAGE_TTL, 600000);
		args.put(X_DEAD_LETTER_EXCHANGE, bunnyConfig.getAmpqTaskTopicExchangeName());
		args.put(X_DEAD_LETTER_ROUTING_KEY, bunnyConfig.getAmpqTaskRouteKey() + DEADLETTER);
		return new Queue(bunnyConfig.getAmpqTaskQueueName(), true, false, false, args);
	}

	@Bean
	@Profile("default")
	Binding binding() {
		getLogger().info("Binding queue {} and exchange {}", bunnyConfig.getAmpqTaskQueueName(),
				bunnyConfig.getAmpqTaskTopicExchangeName());
		return BindingBuilder.bind(queue()).to(exchange()).with(bunnyConfig.getAmpqTaskRouteKey());
	}

	@Bean
	@Profile("default")
	Queue deadLetterQueue() {
		getLogger().info("Register DLQ {}", bunnyConfig.getAmpqTaskQueueName() + DEADLETTER);
		Map<String, Object> args = new HashMap<>();
		args.put(X_MESSAGE_TTL, 60000);
		args.put(X_DEAD_LETTER_EXCHANGE, bunnyConfig.getAmpqTaskTopicExchangeName());
		args.put(X_DEAD_LETTER_ROUTING_KEY, bunnyConfig.getAmpqTaskRouteKey());
		return new Queue(bunnyConfig.getAmpqTaskQueueName() + DEADLETTER, true, false, false, args);
	}

	@Bean
	@Profile("default")
	Binding deadLetterBinding() {
		getLogger().info("DLQ Binding queue {} and exchange {}{}", bunnyConfig.getAmpqTaskQueueName() , DEADLETTER,
				bunnyConfig.getAmpqTaskTopicExchangeName());
		return BindingBuilder.bind(deadLetterQueue()).to(exchange())
				.with(bunnyConfig.getAmpqTaskRouteKey() + DEADLETTER);
	}

	// for automated json parsing
	@Bean
	public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
		getLogger().info("Creating Rabbit Template");
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
		return rabbitTemplate;
	}

	@Bean
	public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
		getLogger().info("Creating Jackson2JsonMessageConverter");
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public MappingJackson2MessageConverter consumerJackson2MessageConverter() {
		getLogger().info("Creating MappingJackson2MessageConverter");
		return new MappingJackson2MessageConverter();
	}

	@Bean
	public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
		getLogger().info("Creating DefaultMessageHandlerMethodFactory");
		DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
		factory.setMessageConverter(consumerJackson2MessageConverter());
		return factory;
	}

	@Override
	public void configureRabbitListeners(final RabbitListenerEndpointRegistrar registrar) {
		getLogger().info("Configure RabbitListenerEndpointRegistrar");
		registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
	}

	@Bean
	public RabbitListenerContainerFactory<SimpleMessageListenerContainer> prefetchTenRabbitListenerContainerFactory(
			ConnectionFactory rabbitConnectionFactory) {
		getLogger().info("Creating SimpleRabbitListenerContainerFactory with prefetchCount {} and manual Acknowledge",
				bunnyConfig.getAmpqTaskMaxPrefetch());
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(rabbitConnectionFactory);
		factory.setPrefetchCount(bunnyConfig.getAmpqTaskMaxPrefetch());
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		factory.setMessageConverter(producerJackson2MessageConverter());
		return factory;
	}

	// For Testing, queues get autodeleted

	@Bean
	@Profile("testing")
	Queue workerQueueTesting() {
		getLogger().info("Register queue {}", bunnyConfig.getAmpqTaskQueueName());
		Map<String, Object> args = new HashMap<>();
		args.put(X_MESSAGE_TTL, 2500);
		args.put(X_DEAD_LETTER_EXCHANGE, bunnyConfig.getAmpqTaskTopicExchangeName());
		args.put(X_DEAD_LETTER_ROUTING_KEY, bunnyConfig.getAmpqTaskRouteKey() + DEADLETTER);
		return new Queue(bunnyConfig.getAmpqTaskQueueName(), true, false, true, args);
	}

	@Bean
	@Profile("testing")
	Queue deadLetterQueueTesting() {
		getLogger().info("Register DLQ {}{}", bunnyConfig.getAmpqTaskQueueName() , DEADLETTER);
		Map<String, Object> args = new HashMap<>();
		args.put(X_MESSAGE_TTL, 1000);
		args.put(X_DEAD_LETTER_EXCHANGE, bunnyConfig.getAmpqTaskTopicExchangeName());
		args.put(X_DEAD_LETTER_ROUTING_KEY, bunnyConfig.getAmpqTaskRouteKey());
		return new Queue(bunnyConfig.getAmpqTaskQueueName() + DEADLETTER, true, false, true, args);
	}

	@Bean
	@Profile("testing")
	Binding workerBindingTesting() {
		getLogger().info("Binding queue {} and exchange {}", bunnyConfig.getAmpqTaskQueueName(),
				bunnyConfig.getAmpqTaskTopicExchangeName());
		return BindingBuilder.bind(workerQueueTesting()).to(exchange()).with(bunnyConfig.getAmpqTaskRouteKey());
	}

	@Bean
	@Profile("testing")
	Binding deadLetterBindingTesting() {
		getLogger().info("DLQ Binding queue {} and exchange {}{}", bunnyConfig.getAmpqTaskQueueName() , DEADLETTER,
				bunnyConfig.getAmpqTaskTopicExchangeName());
		return BindingBuilder.bind(deadLetterQueueTesting()).to(exchange())
				.with(bunnyConfig.getAmpqTaskRouteKey() + DEADLETTER);
	}
}
