package to3.eu.bunny.transfer;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

/**
 * 
 * @author Tom Kornelson
 * @Date 27.07.2018
 * @email t.kornelson@to3.eu
 * 
 * To have one root interface for objects which will get sent through rabbit.
 * 
 * This enables you the whole wide world of transfering Objects! Let them implement this Interface
 *
 */

@JsonTypeInfo(
	      use = JsonTypeInfo.Id.CLASS, 
	      include = As.PROPERTY, 
	      property = "class")
public interface RabbitMessage extends Serializable {

}
