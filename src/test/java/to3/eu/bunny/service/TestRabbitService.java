package to3.eu.bunny.service;

import static org.junit.Assert.fail;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;

import to3.eu.bunny.service.impl.AbstractRabbitService;
import to3.eu.bunny.transfer.RabbitMessage;
import to3.eu.bunny.transfer.TestMessage;

@Service
@Profile("testing")
public class TestRabbitService extends AbstractRabbitService {

	
	@Override
	@RabbitListener(queues = "${bunny.ampqTaskQueueName}", containerFactory = "prefetchTenRabbitListenerContainerFactory")
	public void receiveTask(final RabbitMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
		System.err.println(message);
	}
	
	@Override
	public boolean processReceivedTask(RabbitMessage message) {
		if (!(message instanceof TestMessage)) {
			fail();
			getLogger().error("Received Message is not of expected type TestMessage, it's -> ",
					message.getClass());
			return false;
		}
		getLogger().info("received type {}, contains {}", message.getClass(), message);
		System.out.println("received type " + message.getClass() + ", contains " + message);
		final TestMessage tm = (TestMessage) message;
		final boolean rejectOnce = tm.isReject();
		if (rejectOnce)
			getLogger().error("Rejecting Message!");
		tm.setReject(false); // just one time
		return rejectOnce;
	}

}
