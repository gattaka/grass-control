package cz.gattserver.grass.control.speech;

public class LastAcceptedTO {

	final private String text;
	final private Long time;

	public LastAcceptedTO(String text, Long time) {
		this.text = text;
		this.time = time;
	}

	public String getText() {
		return text;
	}

	public Long getTime() {
		return time;
	}

}
