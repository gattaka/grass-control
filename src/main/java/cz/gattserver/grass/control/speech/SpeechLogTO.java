package cz.gattserver.grass.control.speech;

import java.util.Date;

public class SpeechLogTO {

	private Date time;
	private String command;

	public SpeechLogTO(Date time, String command) {
		super();
		this.time = time;
		this.command = command;
	}

	public Date getTime() {
		return time;
	}

	public String getCommand() {
		return command;
	}
}
