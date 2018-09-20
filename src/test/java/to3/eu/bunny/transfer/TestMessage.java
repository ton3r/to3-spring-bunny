package to3.eu.bunny.transfer;

import java.util.Date;

public class TestMessage implements RabbitMessage {
	private static final long serialVersionUID = -5621164854122570972L;
	private String message;
	private Date date;
	private int randomInt;
	private boolean reject;


	public TestMessage(String message, Date date, int randomInt, boolean reject) {
		super();
		this.message = message;
		this.date = date;
		this.randomInt = randomInt;
		this.reject = reject;
	}

	public boolean isReject() {
		return reject;
	}

	public void setReject(boolean reject) {
		this.reject = reject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getRandomInt() {
		return randomInt;
	}

	public void setRandomInt(int randomInt) {
		this.randomInt = randomInt;
	}

}
