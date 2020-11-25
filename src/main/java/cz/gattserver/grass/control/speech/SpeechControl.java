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
				logger.info("You said: '" + s + "'");
				logger.info("BestPronunciationResult: " + result.getResult().getBestPronunciationResult());
				logger.info("BestFinalResultNoFiller: " + result.getResult().getBestFinalResultNoFiller());
				Token bestToken = result.getResult().getBestFinalToken();
				if (bestToken != null) {
					logger.info("AcousticScore: " + bestToken.getAcousticScore());
					logger.info("LanguageScore: " + bestToken.getLanguageScore());

					// Skore je zatím jediný ukazatel, bohužel se podle něj nedá
					// spolehlivě vylučovat false-positive případy (až na
					// extrémy)
					logger.info("Score: " + bestToken.getScore());

					// False-positive score
					// Score: -1.0025287E9
					// Score: -1.47891379E9
					// Score: -2.92505152E8
					// Score: -3.05739552E8
					// Score: -3.13359136E8
					// Score: -4.61158368E8
					// Score: -4.78037696E8
					// Score: -4.82785824E8
					// Score: -5.611808E8
					// Score: -5.21283104E8
					// Score: -5.7908256E8
					// ! Score: -7.2261043E8
					// ! Score: -7.5503418E8 (ticho a zakašlání)
					//
					// Zamumlané positive
					// Score: -3.28067552E8
					// Score: -2.42420288E8
					// Score: -2.32506208E8
					// Score: -2.63913376E8
					//
					// True positive
					// Score: -5.403209E8
					// Score: -6.1160442E8
					// Score: -6.5100301E8
					// Score: -6.8809613E8
					// Score: -7.8309696E8
					// Score: -8.1800442E8

					if (bestToken.getScore() > -4e8)
						continue;

					// Predecessor jsou u gramatiky vždycky prázdné, až když se
					// vypne použití gramatiky, se začně něco zobrazovat, ale k
					// ničemu to moc není
					AlternateHypothesisManager alternateHypothesisManager = result.getResult()
							.getAlternateHypothesisManager();
					if (alternateHypothesisManager != null
							&& alternateHypothesisManager.hasAlternatePredecessors(bestToken)) {
						for (Token predecessor : alternateHypothesisManager.getAlternatePredecessors(bestToken)) {
							logger.info("Predecessor AcousticScore: " + predecessor.getAcousticScore());
							logger.info("Predecessor LanguageScore: " + predecessor.getLanguageScore());
							logger.info("Predecessor Score: " + predecessor.getScore());
						}
					}
				}
				switch (s) {
				case "grass control player next":
					executeCommand(() -> {
						VLCControl.INSTANCE.sendCommand(VLCCommand.NEXT);
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "grass control player previous":
					executeCommand(() -> {
						VLCControl.INSTANCE.sendCommand(VLCCommand.NEXT);
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "grass control player stop":
					executeCommand(() -> {
						VLCControl.INSTANCE.sendCommand(VLCCommand.PAUSE);
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "grass control player play":
					executeCommand(() -> {
						VLCControl.INSTANCE.sendCommand(VLCCommand.PLAY);
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "grass control open hardware":
					executeCommand(() -> {
						CmdControl.INSTANCE.openChrome("https://www.gattserver.cz/hw");
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "grass control open grass":
					executeCommand(() -> {
						CmdControl.INSTANCE.openChrome("https://www.gattserver.cz");
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "grass control open nexus":
					executeCommand(() -> {
						CmdControl.INSTANCE.openChrome("https://www.gattserver.cz:8843");
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				}
			}
		}
		recognizer.stopRecognition();
	}

	private void executeCommand(Command command) {
		if (!enabled) {
			String msg = "Speech recognition is disabled";
			TrayControl.INSTANCE.showMessage(msg);
			logger.info(msg);
			return;
		}
		command.execute();
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
