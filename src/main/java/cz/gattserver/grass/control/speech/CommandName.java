package cz.gattserver.grass.control.speech;

public enum CommandName {

	/**
	 * Zvýšení hlasitosti
	 */
	VOLUME_UP("volume up", 1, 2),

	/**
	 * Snížení hlasitosti
	 */
	VOLUME_DOWN("volume down", 1, 3),

	/**
	 * Další skladba
	 */
	PLAYER_NEXT("player next", 4, 2),

	/**
	 * Předchozí skladba
	 */
	PLAYER_PREVIOUS("player previous", 4, 3),

	/**
	 * Pauza/zrušení pauzy
	 */
	PLAYER_STOP("player stop", 4, 2),

	/**
	 * Zrušení pauzy
	 */
	PLAYER_PLAY("player play", 4, 2),

	/**
	 * Zapnutí náhodného pořadí
	 */
	PLAYER_START_SHUFFLE("player start shuffle", 4, 2, 3),

	/**
	 * Vypnutí náhodného pořadí
	 */
	PLAYER_STOP_SHUFFLE("player stop shuffle", 4, 2, 3),

	/**
	 * Status přehrávání
	 */
	PLAYER_STATUS("player status", 4, 2),

	/**
	 * Otevření menu skladeb
	 */
	OPEN_MUSIC("open music", 3, 2),

	/**
	 * Otevření správce HW
	 */
	OPEN_HW("open hardware", 3, 3),

	/**
	 * Otevření Grass
	 */
	OPEN_GRASS("open grass", 3, 2),

	/**
	 * Otevření systémového monitoru
	 */
	OPEN_SYSTEM_MONITOR("open system monitor", 3, 3, 3),

	/**
	 * Otevření historie příkazů
	 */
	OPEN_SPEECH_HISTORY("open speech history", 3, 3, 3);

	private String phrase;
	private String[] chunks;
	private int[] tolerances;

	private CommandName(String phrase, int... tolerances) {
		this.phrase = phrase;
		this.chunks = this.phrase.split(" ");
		if (tolerances.length != chunks.length)
			throw new AssertionError("tolerances.length != chunks.length");
		this.tolerances = tolerances;
	}

	public String getPhrase() {
		return phrase;
	}

	public String[] getChunks() {
		return chunks;
	}

	public int[] getTolerances() {
		return tolerances;
	}

}
