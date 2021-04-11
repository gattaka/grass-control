package cz.gattserver.grass.control.speech;

import java.io.IOException;

import edu.cmu.sphinx.api.AbstractSpeechRecognizer;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.Microphone;
import edu.cmu.sphinx.frontend.util.StreamDataSource;

/**
 * High-level class for live speech recognition.
 */
public class CustomLiveSpeechRecognizer extends AbstractSpeechRecognizer {

	private final Microphone microphone;

	/**
	 * Constructs new live recognition object.
	 *
	 * @param configuration
	 *            common configuration
	 * @throws IOException
	 *             if model IO went wrong
	 */
	public CustomLiveSpeechRecognizer(Configuration configuration) throws IOException {
		super(configuration);
		microphone = new Microphone(16000, 16, true, false);
		context.getInstance(StreamDataSource.class).setInputStream(microphone.getStream());
	}

	/**
	 * Starts recognition process.
	 *
	 * @param clear
	 *            clear cached microphone data
	 * @see LiveSpeechRecognizer#stopRecognition()
	 */
	public void startRecognition(boolean clear) {
		recognizer.allocate();
		microphone.startRecording();
	}

	/**
	 * Stops recognition process.
	 *
	 * Recognition process is paused until the next call to startRecognition.
	 *
	 * @see LiveSpeechRecognizer#startRecognition(boolean)
	 */
	public void stopRecognition() {
		microphone.stopRecording();
		recognizer.deallocate();
		try {
			microphone.getStream().close();
		} catch (IOException e) {
			// nic
		}
	}
}
