package cz.gattserver.grass.control.speech;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.CmdControl;
import cz.gattserver.grass.control.TrayControl;
import cz.gattserver.grass.control.vlc.VLCCommand;
import cz.gattserver.grass.control.vlc.VLCControl;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.decoder.search.AlternateHypothesisManager;
import edu.cmu.sphinx.decoder.search.Token;

public enum SpeechControl {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(SpeechControl.class);

	private static final String ACOUSTIC_MODEL = "resource:/edu/cmu/sphinx/models/en-us/en-us";
	private static final String DICTIONARY_PATH = "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
	private static final String GRAMMAR_PATH = "resource:/gram/";
	private static final String LANGUAGE_MODEL = "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin";

	private volatile boolean running = false;
	private volatile boolean enabled = true;

	public void start() {
		if (running)
			throw new IllegalStateException(SpeechControl.class.getName() + " is already running");
		running = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					runControl();
				} catch (InterruptedException | IOException e) {
					throw new IllegalStateException(SpeechControl.class.getName() + " was interrupted", e);
				}
			}
		}).start();
	}

	public void stop() {
		running = false;
	}

	private void runControl() throws InterruptedException, IOException {

		Configuration configuration = new Configuration();
		configuration.setAcousticModelPath(ACOUSTIC_MODEL);
		configuration.setDictionaryPath(DICTIONARY_PATH);
		configuration.setLanguageModelPath(LANGUAGE_MODEL);
		configuration.setGrammarPath(GRAMMAR_PATH);
		configuration.setUseGrammar(true);
		configuration.setGrammarName("grammar");
		// configuration.setSampleRate(48000); // přestane detekovat všechno

		LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
		SpeechResult result = null;
		recognizer.startRecognition(true);
		logger.info("Speech recognition initialized");
		while (running) {
			while ((result = recognizer.getResult()) != null) {
				String s = result.getHypothesis();
				if ("<unk>".equals(s) || "".equals(s))
					continue;
				// prázdné
				// System.out.println(result.getResult().getFrameStatistics());
				Token bestToken = result.getResult().getBestFinalToken();
				Float score = bestToken == null ? null : bestToken.getScore();

				// Vypadá to, že čím lepší frázování (oddělení slov při
				// zadávání, tím lepší skore)
				logger.info("You said: '" + s + "' (score " + score + ")");

				switch (s) {
				case "grass control player next":
					// false
					// -2.40568032E8

					// true (bez hudby)
					// -2.75173376E8 až -3.66884512E8
					// true (s hudbou)
					// -2.54164672E8 až -3.53690176E8
					executeCommand(s, -2.54164672E8, -3.66884512E8, score, () -> {
						VLCControl.INSTANCE.sendCommand(VLCCommand.NEXT);
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "grass control player previous":
					// false

					// true (bez hudby)
					// -2.71929344E8 až -4.02070592E8
					// true (s hudbou)
					// -2.97899136E8 až -4.76348384E8
					executeCommand(s, -2.71929344E8, -4.76348384E8, score, () -> {
						VLCControl.INSTANCE.sendCommand(VLCCommand.NEXT);
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "grass control player stop":
					// false

					// true (bez hudby)
					// -2.45552176E8 až -2.9530528E8
					// true (s hudbou)
					// -2.31685504E8 až -3.81001824E8
					executeCommand(s, -2.31685504E8, -3.81001824E8, score, () -> {
						VLCControl.INSTANCE.sendCommand(VLCCommand.PAUSE);
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "grass control player play":
					// false

					// true (bez hudby)
					// -2.6881104E8 až -3.26824352E8
					// true (s hudbou)
					// -2.76270016E8 až -3.67315744E8
					executeCommand(s, -2.6881104E8, -3.67315744E8, score, () -> {
						VLCControl.INSTANCE.sendCommand(VLCCommand.PLAY);
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "grass control open hardware":
					// false
					// -5.08478624E8

					// true (bez hudby)
					// -2.50869712E8 až -2.91747104E8
					// true (s hudbou)
					// -2.49946336E8 až -3.6449936E8
					executeCommand(s, -2.49946336E8, -3.6449936E8, score, () -> {
						CmdControl.INSTANCE.openChrome("https://www.gattserver.cz/hw");
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "grass control open grass":
					// false
					// -7.1960922E8

					// true (bez hudby)
					// -2.51387168E8 až -3.76090496E8
					// true (s hudbou)
					// -2.51323984E8 až -3.85843776E8
					executeCommand(s, -2.51323984E8, -3.76090496E8, score, () -> {
						CmdControl.INSTANCE.openChrome("https://www.gattserver.cz");
					});
					break;
				case "grass control open nexus":
					// false

					// true (bez hudby)
					// -2.59455376E8 až -4.09875456E8
					// true (s hudbou)
					// -2.87446784E8 až -3.9382896E8
					executeCommand(s, -2.59455376E8, -4.09875456E8, score, () -> {
						CmdControl.INSTANCE.openChrome("https://www.gattserver.cz:8843");
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				}
			}
		}
		recognizer.stopRecognition();
	}

	private void executeCommand(String text, double fromScore, double toScore, Float score, Command command) {
		if (!enabled) {
			String msg = "Speech recognition is disabled";
			TrayControl.INSTANCE.showMessage(msg);
			logger.info(msg);
			return;
		}
		if (score <= fromScore && score >= toScore) {
			TrayControl.INSTANCE.showMessage(text + " (score " + score + ")");
			command.execute();
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
