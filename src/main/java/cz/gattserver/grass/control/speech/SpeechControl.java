package cz.gattserver.grass.control.speech;

import cz.gattserver.grass.control.speech.voce.SpeechInterface;

public enum SpeechControl {

	INSTANCE;

	private static final String ENDPHRASE = "grass end the recognition";

	private volatile boolean running = false;

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
		System.out.println("Initialized... say '" + ENDPHRASE + "' to quit.");

		// Normally, applications would do application-specific things
		// here. For this sample, we'll just sleep for a little bit.
		Thread.sleep(200);

		while (running && SpeechInterface.getRecognizerQueueSize() > 0) {
			String s = SpeechInterface.popRecognizedString();
			System.out.println("You said: " + s);
		}

		SpeechInterface.destroy();
	}

}
