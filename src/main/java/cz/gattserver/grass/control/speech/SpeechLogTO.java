package cz.gattserver.grass.control.speech;

import java.time.LocalDateTime;

public class SpeechLogTO {

	private LocalDateTime time;
	private String command;

	public SpeechLogTO(LocalDateTime time, String command) {
		super();
		this.time = time;
		this.command = command;
	}

	public LocalDateTime getTime() {
		return time;
	}

	public String getCommand() {
		return command;
	}
}
