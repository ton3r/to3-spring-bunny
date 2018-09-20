package to3.eu.bunny.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import to3.eu.bunny.logging.DefaultLoggingInterface;
import to3.eu.bunny.transfer.RabbitMessage;


public interface RabbitService extends DefaultLoggingInterface {

	void putTaskToQueue(final RabbitMessage entity, final String routeKeySuffix) throws JsonProcessingException;

	void putTaskToRetryQueue(final RabbitMessage entity, final String routeKeyChannel) throws JsonProcessingException;

	void putTaskToRetryQueue(final RabbitMessage entity, final String routeKeyChannel, final int priority) throws JsonProcessingException;

	void putTaskToQueue(final RabbitMessage entity, final String routeKeyChannel, final int priority) throws JsonProcessingException;

}
