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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import to3.eu.bunny.logging.DefaultLoggingInterface;

@Configuration
@PropertySource("classpath:bunny.properties")
public class ComplexBunnyConfiguration implements RabbitListenerConfigurer, DefaultLoggingInterface {

	public static final String X_MAX_PRIORITY = "x-max-priority";
	public static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
	public static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
	public static final String X_MESSAGE_TTL = "x-message-ttl";

	@Value("${bunny.ampqRetryTopicExchangeName}")
	private String bunny_retry_exchange = "BUNNY.RETRY.EXCHANGE";
	@Value("${bunny.ampqRetryQueueName}")
	private String bunny_retry_queue = "BUNNY.RETRY.QUEUE";
	@Value("${bunny.ampqRetryRouteKey}")
	private String bunny_retry_queue_route_key = "BUNNY.INBOUND.QUEUE";

	@Value("${bunny.ampqTaskTopicExchangeName}")
	private String bunny_inbound_exchange = "BUNNY.INBOUND.EXCHANGE";
	@Value("${bunny.ampqTaskQueueName}")
	private String bunny_inbound_queue = "BUNNY.INBOUND.QUEUE";
	@Value("${bunny.ampqTaskRouteKey}")
	private String bunny_inbound_queue_route_key = "BUNNY.INBOUND.QUEUE";
	
	@Value("${bunny.ampqRetryTTL}")
	private long bunny_retry_ttl = 60000L;
	
	@Value("${bunny.ampqInboundTTL}")
	private long bunny_inbound_ttl = 600000L;
	
	
	@Value("${bunny.ampqTaskMaxPrefetch}")
	private int max_prefetch = 7;

	@Bean
	TopicExchange retryExchange() {
		getLogger().info("Register topic retryExchange {}", bunny_retry_exchange);
		return new TopicExchange(bunny_retry_exchange);
	}

	@Bean
	@Profile("default")
	Queue retryQueue() {
		getLogger().info("Register retryQueue {}", bunny_retry_queue);
		Map<String, Object> args = new HashMap<>();
		args.put(X_MESSAGE_TTL, bunny_retry_ttl);
		args.put(X_DEAD_LETTER_EXCHANGE, bunny_inbound_exchange);
		args.put(X_DEAD_LETTER_ROUTING_KEY, bunny_inbound_queue);
		args.put(X_MAX_PRIORITY, 10);
		return new Queue(bunny_retry_queue, true, false, false, args);
	}

	@Bean
	@Profile("default")
	Binding bindingRetry() {
		getLogger().info("Binding retry queue {} and retry exchange {}", bunny_retry_queue, bunny_retry_exchange);
		return BindingBuilder.bind(retryQueue()).to(retryExchange()).with(bunny_retry_queue);
	}

	@Bean
	TopicExchange inboundExchange() {
		getLogger().info("Register topic exchange {}", bunny_inbound_exchange);
		return new TopicExchange(bunny_inbound_exchange);
	}

	@Bean
	@Profile("default")
	Queue inboundQueue() {
		getLogger().info("Register inboundQueue {}", bunny_inbound_queue);
		Map<String, Object> args = new HashMap<>();
		args.put(X_MESSAGE_TTL, bunny_inbound_ttl);
		args.put(X_DEAD_LETTER_EXCHANGE, bunny_retry_exchange);
		args.put(X_DEAD_LETTER_ROUTING_KEY, bunny_retry_queue);
		args.put(X_MAX_PRIORITY, 10);
		return new Queue(bunny_inbound_queue, true, false, false, args);
	}

	@Bean
	@Profile("default")
	Binding bindingInbound() {
		getLogger().info("Binding inbound queue {} and exchange {}", bunny_inbound_queue, bunny_inbound_exchange);
		return BindingBuilder.bind(inboundQueue()).to(inboundExchange()).with(bunny_inbound_queue);
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
				max_prefetch);
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(rabbitConnectionFactory);
		factory.setPrefetchCount(max_prefetch);
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		factory.setMessageConverter(producerJackson2MessageConverter());
		return factory;
	}

	// For Testing, queues get autodeleted

	@Bean
	@Profile("testing")
	Queue workerQueueTesting() {
		getLogger().info("Register queue {}", bunny_inbound_queue);
		Map<String, Object> args = new HashMap<>();
		args.put(X_MESSAGE_TTL, 2500);
		args.put(X_DEAD_LETTER_EXCHANGE, bunny_retry_exchange);
		args.put(X_DEAD_LETTER_ROUTING_KEY, bunny_retry_queue);
		args.put(X_MAX_PRIORITY, 10);
		return new Queue(bunny_inbound_queue, true, false, true, args);
	}

	@Bean
	@Profile("testing")
	Queue deadLetterQueueTesting() {
		getLogger().info("Register DLQ {}", bunny_retry_queue);
		Map<String, Object> args = new HashMap<>();
		args.put(X_MESSAGE_TTL, 1000);
		args.put(X_DEAD_LETTER_EXCHANGE, bunny_inbound_exchange);
		args.put(X_DEAD_LETTER_ROUTING_KEY, bunny_inbound_queue);
		args.put(X_MAX_PRIORITY, 10);
		return new Queue(bunny_retry_queue, true, false, true, args);
	}

	@Bean
	@Profile("testing")
	Binding workerBindingTesting() {
		getLogger().info("Binding queue {} and exchange {}",bunny_inbound_queue, bunny_inbound_exchange);
		return BindingBuilder.bind(workerQueueTesting()).to(inboundExchange()).with(bunny_inbound_queue);
	}

	@Bean
	@Profile("testing")
	Binding deadLetterBindingTesting() {
		getLogger().info("DLQ Binding queue {} and exchange {}", bunny_retry_queue,
				bunny_retry_exchange);
		return BindingBuilder.bind(deadLetterQueueTesting()).to(retryExchange()).with(bunny_retry_queue);
	}
}
