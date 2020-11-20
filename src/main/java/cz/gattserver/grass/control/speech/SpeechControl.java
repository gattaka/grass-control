package cz.gattserver.grass.control.speech;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.CmdControl;
import cz.gattserver.grass.control.TrayControl;
import cz.gattserver.grass.control.vlc.VLCCommand;
import cz.gattserver.grass.control.vlc.VLCControl;
import voce.SpeechInterface;

public enum SpeechControl {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(SpeechControl.class);

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
				} catch (InterruptedException e) {
					throw new IllegalStateException(SpeechControl.class.getName() + " was interrupted", e);
				}
			}
		}).start();
	}

	public void stop() {
		running = false;
	}

	private void runControl() throws InterruptedException {
		SpeechInterface.init();
		logger.info("Speech recognition initialized");
		while (running) {
			while (SpeechInterface.getRecognizerQueueSize() > 0) {
				String s = SpeechInterface.popRecognizedString();
				logger.info("You said: " + s);
				switch (s) {
				case "grass control open hardware":
					break;
				case "grass control open articles":
					break;
				case "grass control open calculator":
					break;
				case "grass control open you tube":
					break;
				case "v l c next":
					executeCommand(() -> {
						VLCControl.INSTANCE.sendCommand(VLCCommand.NEXT);
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "v l c previous":
					executeCommand(() -> {
						VLCControl.INSTANCE.sendCommand(VLCCommand.NEXT);
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "v l c stop":
					executeCommand(() -> {
						VLCControl.INSTANCE.sendCommand(VLCCommand.PAUSE);
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "v l c play":
					executeCommand(() -> {
						VLCControl.INSTANCE.sendCommand(VLCCommand.PLAY);
						TrayControl.INSTANCE.showMessage(s);
					});
					break;
				case "grass control open grass":
					executeCommand(() -> {
						CmdControl.INSTANCE.openChrome("www.gattserver.cz");
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
		SpeechInterface.destroy();
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
