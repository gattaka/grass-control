package cz.gattserver.grass.control.vlc;

public enum VLCCommand {

	/**
	 * Next track
	 */
	NEXT("next"),

	/**
	 * Previous track
	 */
	PREV("prev"),

	/**
	 * Stop
	 */
	STOP("stop"),

	/**
	 * Play
	 */
	PLAY("play"),

	/**
	 * Pause
	 */
	PAUSE("pause"),

	/**
	 * Quit
	 */
	QUIT("quit"),

	/**
	 * Volume down
	 */
	VOLDOWN("voldown 1"),

	/**
	 * Volume up
	 */
	VOLUP("volup 1");

	private String signalName;

	private VLCCommand(String signalName) {
		this.signalName = signalName;
	}

	public String getSignalName() {
		return signalName;
	}
}