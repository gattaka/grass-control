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

	// /**
	// * Volume down
	// */
	// VOLDOWN("voldown 2"),
	//
	// /**
	// * Volume up
	// */
	// VOLUP("volup 2"),

	/**
	 * Random on
	 */
	RANDOM_ON("random on"),

	/**
	 * Random off
	 */
	RANDOM_OFF("random off"),

	/**
	 * Status
	 */
	STATUS("status"),

	/**
	 * Add
	 */
	ADD("add"),

	/**
	 * Clear
	 */
	CLEAR("clear");

	private String signalName;

	private VLCCommand(String signalName) {
		this.signalName = signalName;
	}

	public String getSignalName() {
		return signalName;
	}
}