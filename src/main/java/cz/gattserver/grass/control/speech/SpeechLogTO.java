package cz.gattserver.grass.control.speech;

import java.util.Date;

public class SpeechLogTO {

	private Date time;
	private String command;
	private float score;
	private boolean inRange;

	public SpeechLogTO(Date time, String command, float score, boolean inRange) {
		super();
		this.time = time;
		this.command = command;
		this.score = score;
		this.inRange = inRange;
	}

	public Date getTime() {
		return time;
	}

	public String getCommand() {
		return command;
	}

	public float getScore() {
		return score;
	}

	public boolean isInRange() {
		return inRange;
	}
}
