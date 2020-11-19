package cz.gattserver.grass.control;

import cz.gattserver.grass.control.bluetooth.BTControl;

public class Main {

	public static void main(String[] args) {
		BTControl.INSTANCE.start();
	}

}
