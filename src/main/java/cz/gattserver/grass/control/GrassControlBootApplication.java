package cz.gattserver.grass.control;

import cz.gattserver.grass.control.bluetooth.BluetoothControl;
import cz.gattserver.grass.control.speech.SpeechControl;
import cz.gattserver.grass.control.ui.MusicIndex;
import cz.gattserver.grass.control.ui.common.TrayControl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class GrassControlBootApplication {

    public static void main(String[] args) throws IOException {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(GrassControlBootApplication.class);
        // řeší absenci SystemTray.isSupported()
        builder.headless(false);
        ConfigurableApplicationContext context = builder.run(args);

        // SpringApplication.run(GrassControlBootApplication.class, args);

        TrayControl.INSTANCE.create();
        BluetoothControl.INSTANCE.start();
        SpeechControl.INSTANCE.start();

        new Thread(() -> MusicIndex.buildIndex()).start();
    }

}
