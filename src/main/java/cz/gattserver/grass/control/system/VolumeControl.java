package cz.gattserver.grass.control.system;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.vlc.VLCControl;

public enum VolumeControl {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(VLCControl.class);

	private static List<FloatControl> volCtrls = new ArrayList<>();

	public static void probe() {
		Mixer.Info[] mixers = AudioSystem.getMixerInfo();
		logger.info("There are " + mixers.length + " mixer info objects");
		for (Mixer.Info mixerInfo : mixers) {
			logger.info("mixer name: " + mixerInfo.getName());
			Mixer mixer = AudioSystem.getMixer(mixerInfo);
			// target, not source
			Line.Info[] lineInfos = mixer.getTargetLineInfo();
			for (Line.Info lineInfo : lineInfos) {
				logger.info("  Line.Info: " + lineInfo);
				Line line = null;
				boolean opened = true;
				try {
					line = mixer.getLine(lineInfo);
					opened = line.isOpen() || line instanceof Clip;
					if (!opened)
						line.open();
					FloatControl volCtrl = (FloatControl) line.getControl(FloatControl.Type.VOLUME);
					volCtrls.add(volCtrl);
					logger.info("\tvolCtrl.getValue() = " + volCtrl.getValue());
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException iaEx) {
					logger.info("    " + iaEx);
				} finally {
					if (line != null && !opened) {
						line.close();
					}
				}
			}
		}
	}

	public static void increaseVolume() {
		for (FloatControl volCtrl : volCtrls) {
			volCtrl.setValue(volCtrl.getValue() + .1f);
			logger.info("volCtrl " + volCtrl.getValue());
		}

	}

	public static void dereaseVolume() {
		for (FloatControl volCtrl : volCtrls) {
			volCtrl.setValue(volCtrl.getValue() - .1f);
			logger.info("volCtrl " + volCtrl.getValue());
		}
	}

}
