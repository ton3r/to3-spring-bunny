package to3.eu.bunny.service.impl;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import to3.eu.bunny.config.BunnyConfig;
import to3.eu.bunny.service.RabbitService;
import to3.eu.bunny.transfer.RabbitMessage;

/**
 * 
 * @author Tom Kornelson
 * @Date 01.08.2018
 * @email t.kornelson@to3.eu
 * 
 *        This abstract Service provides the minimum to send/receive to the
 *        RabbitMQ
 * 
 *        You have to implement the abstract methods to work on the received
 *        messages. The abstract methods will get called from the impremented
 *        receiveXYZ Method
 *
 */
@Service
@PropertySource("classpath:bunny.properties")
@ConfigurationProperties(prefix = "bunny")
public abstract class AbstractRabbitService implements RabbitService {
	@Autowired
	protected ObjectMapper jacksonObjectMapper;
	@Autowired
	protected RabbitTemplate rabbitTemplate;

	@Autowired
	protected BunnyConfig bunnyConfig;


	@Value("${bunny.ampqTaskQueueName}")
	private String myListeningQueue;

	@PostConstruct
	public void initServices() {
		getLogger().info("I am listening to queue {}", myListeningQueue);
	}

	@Override
	public void putTaskToQueue(final RabbitMessage entity, final String routeKeyChannel, final int priority)
			throws JsonProcessingException {
		final String routingKey = bunnyConfig.getAmpqTaskRouteKey();
		if (getLogger().isTraceEnabled()) {
			final String json = jacksonObjectMapper.writeValueAsString(entity);
			getLogger().trace("putting {} to rabbit route {}|-|{}  [FOO_ROUTE-BAR_KEY]",
					json.replaceAll("secureKey", "XXXXXX").replaceAll("passwd", "xxxxxxx"),
					bunnyConfig.getAmpqTaskTopicExchangeName(), routingKey);
		}

		rabbitTemplate.convertAndSend(bunnyConfig.getAmpqTaskTopicExchangeName(), routingKey, entity, m -> {
			m.getMessageProperties().setPriority(priority);
			return m;
		});
	}

	@Override
	public void putTaskToQueue(final RabbitMessage entity, final String routeKeyChannel)
			throws JsonProcessingException {
		putTaskToQueue(entity, routeKeyChannel, 0);
	}

	@Override
	@SuppressWarnings(value = "squid:S2068")
	public void putTaskToRetryQueue(final RabbitMessage entity, final String routeKeyChannel, final int priority)
			throws JsonProcessingException {
		final String routingKey = bunnyConfig.getAmpqRetryRouteKey();
		if (getLogger().isTraceEnabled()) {
			final String json = jacksonObjectMapper.writeValueAsString(entity);
			getLogger().trace("putting {} to rabbit route {}|-|{}  [FOO_ROUTE-BAR_KEY]",
					json.replaceAll("secureKey", "XXXXXX").replaceAll("passwd", "xxxxxxx"),
					bunnyConfig.getAmpqRetryTopicExchangeName(), routingKey);
		}
		rabbitTemplate.convertAndSend(bunnyConfig.getAmpqRetryTopicExchangeName(), routingKey, entity, m -> {
			m.getMessageProperties().setPriority(priority);
			return m;
		});

	}

	@Override
	@SuppressWarnings(value = "squid:S2068")
	public void putTaskToRetryQueue(final RabbitMessage entity, final String routeKeyChannel)
			throws JsonProcessingException {
		putTaskToRetryQueue(entity, routeKeyChannel, 0);
	}

	@RabbitListener(queues = "${bunny.ampqTaskQueueName}", containerFactory = "prefetchTenRabbitListenerContainerFactory")
	public void receiveTask(final RabbitMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag)
			throws IOException {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("Received {} -> Detail: {}", message.getClass().getName(), message);
		}
		final boolean processed = processReceivedTask(message);
		if (processed) {
			channel.basicAck(tag, false);
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("Receive acknowledged");
			}
		} else {
			channel.basicNack(tag, false, false);
			if (getLogger().isDebugEnabled()) {
				getLogger().debug("Receive not acknowledged");
			}
		}

	}

	/**
	 * 
	 * 
	 * @param message
	 *            - the received object. You have to check the instance and take
	 *            your own operations on/with it
	 * @return based on your return value the receiver method
	 *         {@link AbstractRabbitService#receiveTask(RabbitMessage, Channel, long)}
	 *         will ack or nack the message to the underlying queue
	 */
	public abstract boolean processReceivedTask(final RabbitMessage message);

}
