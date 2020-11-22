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
		// configuration.setSampleRate(48000);

		LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
		SpeechResult result = null;
		recognizer.startRecognition(true);
		logger.info("Speech recognition initialized");
		while (running) {
			while ((result = recognizer.getResult()) != null) {
				String s = result.getHypothesis();
				if ("<unk>".equals(s))
					continue;
				// System.out.println(result.getResult().getFrameStatistics());
				logger.info("You said: " + s);
				switch (s) {
				case "v l c player next":
					executeCommand(() -> {
						VLCControl.INSTANCE.sendCommand(VLCCommand.NEXT);
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "v l c player previous":
					executeCommand(() -> {
						VLCControl.INSTANCE.sendCommand(VLCCommand.NEXT);
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "v l c player stop":
					executeCommand(() -> {
						VLCControl.INSTANCE.sendCommand(VLCCommand.PAUSE);
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "v l c player play":
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
