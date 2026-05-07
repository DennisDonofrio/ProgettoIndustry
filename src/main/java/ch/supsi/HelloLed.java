package ch.supsi;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.pi4j.GrovePi4J;
import org.iot.raspberry.grovepi.sensors.digital.GroveLed;
import org.iot.raspberry.grovepi.sensors.digital.GroveUltrasonicRanger;

/**
 *
 * @author Giuseppe Landolfi
 * @author Radostin Tsetanov
 *
 * UltraSonic Ranger --> D6 Led --> D3
 *
 */
public class HelloLed {

    private static boolean acquisitionOn = true;

    public static void main(String[] args) throws Exception {
        Logger.getLogger("GrovePi").setLevel(Level.WARNING);
        Logger.getLogger("RaspberryPi").setLevel(Level.WARNING);
        GrovePi grovePi = new GrovePi4J();
        GroveUltrasonicRanger ranger = new GroveUltrasonicRanger(grovePi, 6);
        GroveLed led = new GroveLed(grovePi, 3);

        while (true) {
            if (acquisitionOn) {
                double distance = ranger.get();
                if (distance > 0) {
                    System.out.println("Distanza = " + distance + " cm");

                    if (distance <= 5) {
                        led.set(true);
                    } else {
                        led.set(false);
                    }
                }
            }

            Thread.sleep(500);
        }
    }

}
