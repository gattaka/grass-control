package cz.gattserver.grass.control.speech;

import cz.gattserver.grass.control.speech.voce.SpeechInterface;

public class RecognitionTest {
	public static void main(String[] argv) {

		final String ENDPHRASE = "grass end the recognition";

		

		System.out.println("Initialized... say '" + ENDPHRASE + "' to quit.");

		boolean quit = false;
		while (!quit) {
			
		}

		voce.SpeechInterface.destroy();
		System.exit(0);
	}
}
