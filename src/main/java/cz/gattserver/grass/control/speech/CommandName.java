package cz.gattserver.grass.control.speech;

public enum CommandName {

	PLAYER_NEXT("player next"),
	VOLUME_UP("volume up"),
	VOLUME_DOWN("volume down"),
	
	PLAYER_PREVIOUS("player previous"),
	PLAYER_STOP("player stop"),
	PLAYER_PLAY("player play"),
	PLAYER_START_SHUFFLE("player start shuffle"),
	PLAYER_STOP_SHUFFLE("player stop shuffle"),
	PLAYER_STATUS("player status"),
	
	OPEN_MUSIC("open music"),
	OPEN_HW("open hardware"),
	OPEN_GRASS("open grass"),
	OPEN_NEXUS("open nexus"),
	OPEN_SYSTEM_MONITOR("open system monitor"),
	OPEN_SPEECH_HISTORY("open speech history");
	
	// private static final String GRASS_CONTROL = "grass control";
	// private static final String PREFIX = GRASS_CONTROL + " ";
	private static final String PREFIX = "";

	private String phrase;
	private String[] chunks;

	private CommandName(String phrase) {
		this.phrase = PREFIX + phrase;
		this.chunks = this.phrase.split(" ");
	}

	public String getPhrase() {
		return phrase;
	}
	
	public String[] getChunks() {
		return chunks;
	}

}
