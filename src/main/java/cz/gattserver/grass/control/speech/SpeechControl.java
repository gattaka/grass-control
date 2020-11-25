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
				Token bestToken = result.getResult().getBestFinalToken();
				Float score = bestToken == null ? null : bestToken.getScore();

				// Vypadá to, že čím lepší frázování (oddělení slov při
				// zadávání, tím lepší skore)
				logger.info("You said: '" + s + "' (score " + score + ")");

				// Ani přes skore není možné odchytit správně (obrovské množství
				// false-positive, které svým rozsahem výskytu přesahují
				// true-positve)
				// v l c next
				// v l c previous
				// v l c stop
				// v l c play

				switch (s) {
				case "grass control player next":
					// false -2.40568032E8
					executeCommand(s, -2.4E8, -5.11E8, score, () -> VLCControl.INSTANCE.sendCommand(VLCCommand.NEXT));
					break;
				case "grass control player previous":
					// false
					executeCommand(s, -2.71929344E8, -4.76348384E8, score,
							() -> VLCControl.INSTANCE.sendCommand(VLCCommand.NEXT));
					break;
				case "grass control player stop":
					executeCommand(s, -2.3E8, -4.1E8, score, () -> VLCControl.INSTANCE.sendCommand(VLCCommand.PAUSE));
					break;
				case "grass control player play":
					executeCommand(s, -2.4E8, -3.67315744E8, score,
							() -> VLCControl.INSTANCE.sendCommand(VLCCommand.PLAY));
					break;
				case "grass control open hardware":
					// false -3.29825952E8 až -5.08478624E8
					executeCommand(s, -2.4E8, -3.29E8, score,
							() -> CmdControl.INSTANCE.openChrome("https://www.gattserver.cz/hw"));
					break;
				case "grass control open grass":
					// false -3.01723776E8 až -7.1960922E8
					executeCommand(s, -2.96E8, -3.0E8, score,
							() -> CmdControl.INSTANCE.openChrome("https://www.gattserver.cz"));
					break;
				case "grass control open nexus":
					// false
					executeCommand(s, -2.59455376E8, -4.09875456E8, score,
							() -> CmdControl.INSTANCE.openChrome("https://www.gattserver.cz:8843"));
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
			logger.info("Score in range");
			command.execute();
		} else {
			logger.info("Score out of range");
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
