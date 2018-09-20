/**
 * 
 */
package to3.eu.bunny.service;

import static org.junit.Assert.fail;

import java.util.Date;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;

import to3.eu.bunny.configuration.EnableComplexBunny;
import to3.eu.bunny.configuration.TestConfiguration;
import to3.eu.bunny.logging.DefaultLoggingInterface;
import to3.eu.bunny.transfer.TestMessage;

/**
 * @author Tom Kornelson
 * @Date 01.08.2018
 * @email t.kornelson@to3.eu
 * 
 * 
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestConfiguration.class)
@ComponentScan("to3.eu.bunny")
@EnableComplexBunny
@EnableAutoConfiguration
@ActiveProfiles("testing")
public class RabbitServiceTest implements DefaultLoggingInterface {
	@Autowired
	private TestRabbitService rabbitService;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		getLogger().info("Rabbit Service -> {}", rabbitService);
	}

	/**
	 * Test method for
	 * {@link to3.eu.bunny.service.impl.AbstractRabbitService#putTaskToQueue(to3.eu.bunny.transfer.RabbitMessage, java.lang.String)}.
	 */
	@Test
	public final void testPutTaskToQueue() {
		TestMessage aMessage = new TestMessage("testPutTaskToQueue", new Date(), new Random().nextInt(50), false);
		try {
			rabbitService.putTaskToQueue(aMessage, "testaccept");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		getLogger().info("Accepting Test completed!");
	}

	@Test
	public final void testPutRejectTaskToQueue() {
		try {
			TestMessage aMessage = new TestMessage("testPutTaskToQueue", new Date(), new Random().nextInt(50), true);
			rabbitService.putTaskToQueue(aMessage, "testreject");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		getLogger().info("Rejecting Test completed!");
	}

}
