package cz.gattserver.grass.control.speech;

import java.io.IOException;
import java.util.function.Consumer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

public class VoskRecognizer {

	private static final Logger logger = LoggerFactory.getLogger(VoskRecognizer.class);

	private volatile boolean initialized;
	private volatile boolean enabled;

	public void initialize(Consumer<String> onResult) throws LineUnavailableException {
		LibVosk.setLogLevel(LogLevel.DEBUG);

		new Thread(new Runnable() {
			@Override
			public void run() {

				LibVosk.setLogLevel(LogLevel.DEBUG);

				AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 60000, 16, 2, 4, 48000, false);
				DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
				TargetDataLine microphone;

				try (Model model = new Model("vosk-model-small-en-us-0.15");
						Recognizer recognizer = new Recognizer(model, 120000)) {
					try {
						microphone = (TargetDataLine) AudioSystem.getLine(info);
						microphone.open(format);
						microphone.start();

						int numBytesRead;
						int CHUNK_SIZE = 1024;

						byte[] b = new byte[4096];

						while (enabled) {
							numBytesRead = microphone.read(b, 0, CHUNK_SIZE);
							if (recognizer.acceptWaveForm(b, numBytesRead)) {
								String result = recognizer.getResult();
								String chunks[] = result.split("\"");
								String payload = chunks[3];
								if (payload.trim().length() > 0) {
									logger.info(chunks[3]);
									onResult.accept(chunks[3]);
								}
							}
						}
						microphone.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}).start();
	}

	public void start(Consumer<String> onResult) {
		enabled = true;
		if (!initialized) {
			try {
				initialize(onResult);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}
	}

}
