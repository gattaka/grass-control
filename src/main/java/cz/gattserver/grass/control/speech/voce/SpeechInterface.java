/*************************************************************************
 *                                                                       *
 * Voce                                                                  *
 * Copyright (C) 2005                                                    *
 * Tyler Streeter  tylerstreeter@gmail.com                               *
 * All rights reserved.                                                  *
 * Web: voce.sourceforge.net                                             *
 *                                                                       *
 * This library is free software; you can redistribute it and/or         *
 * modify it under the terms of EITHER:                                  *
 *   (1) The GNU Lesser General Public License as published by the Free  *
 *       Software Foundation; either version 2.1 of the License, or (at  *
 *       your option) any later version. The text of the GNU Lesser      *
 *       General Public License is included with this library in the     *
 *       file license-LGPL.txt.                                          *
 *   (2) The BSD-style license that is included with this library in     *
 *       the file license-BSD.txt.                                       *
 *                                                                       *
 * This library is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the files    *
 * license-LGPL.txt and license-BSD.txt for more details.                *
 *                                                                       *
 *************************************************************************/

/// The main package that contains everything in the Voce Java API.
package cz.gattserver.grass.control.speech.voce;

/// A set of static methods that give users access to the main speech 
/// interaction components.  These methods are the only ones exposed to 
/// other programming languages through the Java Native Interface.
public class SpeechInterface {
	private static SpeechRecognizer mRecognizer = null;

	/// Initializes Voce. The 'vocePath' String specifies the path where
	/// Voce classes and config file can be found. 'initSynthesis'
	/// and 'initRecognition' enable these capabilities; if you don't
	/// need one or the other, not initializing it will save load time
	/// and memory (though the feature will be disabled, of course).
	/// 'grammarPath' is a relative or absolute path to one or more
	/// grammar files (all .gram files in 'grammarPath' will automatically
	/// be searched). 'grammarName' is the name of a specific grammar
	/// within a .gram file in the 'grammarPath'. If the 'grammarName'
	/// is empty, a simple default grammar will be used.
	public static void init() {
		Utils.setPrintDebug(false);
		Utils.log("debug", "Beginning initialization");

		// Create the speech recognizer.
		Utils.log("", "Initializing recognizer. " + "This may take some time...");
		mRecognizer = new SpeechRecognizer();

		// Enable the recognizer; this will start the recognition
		// thread.
		setRecognizerEnabled(true);

		Utils.log("", "Initialization complete");
	}

	/// Destroys Voce.
	public static void destroy() {
		Utils.log("debug", "Shutting down...");

		if (null != mRecognizer)
			mRecognizer.destroy();

		Utils.log("", "Shutdown complete");
	}

	/// Returns the number of recognized strings currently in the
	/// recognizer's queue.
	public static int getRecognizerQueueSize() {
		if (null == mRecognizer) {
			Utils.log("warning",
					"getRecognizerQueueSize " + "called before recognizer was initialized.  Returning " + "0.");
			return 0;
		}

		return mRecognizer.getQueueSize();
	}

	/// Returns and removes the oldest recognized string from the
	/// recognizer's queue.
	public static String popRecognizedString() {
		if (null == mRecognizer) {
			Utils.log("warning", "popRecognizedString " + "called before recognizer was initialized.  Returning "
					+ "an empty string.");
			return "";
		}

		return mRecognizer.popString();
	}

	/// Enables and disables the speech recognizer.
	public static void setRecognizerEnabled(boolean e) {
		if (null == mRecognizer) {
			Utils.log("warning", "setRecognizerEnabled " + "called before recognizer was initialized.  Request "
					+ "will be ignored.");
			return;
		}

		mRecognizer.setEnabled(e);
	}

	/// Returns true if the recognizer is currently enabled.
	public static boolean isRecognizerEnabled() {
		if (null == mRecognizer) {
			Utils.log("warning",
					"isRecognizerEnabled " + "called before recognizer was initialized.  Returning " + "false.");
			return false;
		}

		return mRecognizer.isEnabled();
	}
}
