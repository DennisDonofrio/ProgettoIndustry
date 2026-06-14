import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.pi4j.GrovePi4J;
import org.iot.raspberry.grovepi.sensors.analog.GroveRotarySensor;
import org.iot.raspberry.grovepi.sensors.data.GroveRotaryValue;
import org.iot.raspberry.grovepi.sensors.digital.GroveButton;
import org.iot.raspberry.grovepi.sensors.digital.GroveUltrasonicRanger;
import org.iot.raspberry.grovepi.sensors.synch.SensorMonitor;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    final double RANGER_MAX_DISTANCE = 50.0;
    final double ROTARY_THRESHOLD = 30.0;
    final long LOOP_DELAY_MS = 10;
    final long ROTARY_MONITOR_DELAY_MS = 100;
    final long RANGER_MONITOR_DELAY_MS = 700;

    final String TOKEN = "c3P6J_F9O0CIcy8aGJr2RZNX6m2z_gRDLkf6-EbjRmdofwP1EkN_jfH1Y4SpAHg5YaGF6FdlpzRPYQAi6DtB8w==";
    final String ORG = "Supsi";
    final String BUCKET = "NanoFactory";

    GrovePi grovePi;
    GroveRotarySensor rotaryLeft;
    GroveRotarySensor rotaryRight;
    GroveRotarySensor rotaryCenter;
    GroveUltrasonicRanger ultrasonicLeft;
    GroveUltrasonicRanger ultrasonicRight;
    GroveButton loadButton;

    SensorMonitor<GroveRotaryValue> rotaryMonitorLeft;
    SensorMonitor<GroveRotaryValue> rotaryMonitorRight;
    SensorMonitor<GroveRotaryValue> rotaryMonitorCenter;
    SensorMonitor<Double> rangerMonitorLeft;
    SensorMonitor<Double> rangerMonitorRight;

    double rotaryLeftDefault;
    double rotaryRightDefault;
    double rotaryCenterDefault;

    MachineState machineState;
    MachineState machineStatePrev;
    InfluxDB influxDB;


    void setup() {
        try {
            machineState = new MachineState();
            machineStatePrev = new MachineState();
            influxDB = new InfluxDB(TOKEN, BUCKET, ORG);

            grovePi = new GrovePi4J();
            rotaryLeft = new GroveRotarySensor(grovePi, 2);
            rotaryRight = new GroveRotarySensor(grovePi, 0);
            rotaryCenter = new GroveRotarySensor(grovePi, 1);
            ultrasonicLeft = new GroveUltrasonicRanger(grovePi, 5);
            ultrasonicRight = new GroveUltrasonicRanger(grovePi, 6);
            //loadButton = new GroveButton(grovePi, 2);

            rotaryMonitorLeft = new SensorMonitor<>(rotaryLeft, ROTARY_MONITOR_DELAY_MS);
            rotaryMonitorRight = new SensorMonitor<>(rotaryRight, ROTARY_MONITOR_DELAY_MS);
            rotaryMonitorCenter = new SensorMonitor<>(rotaryCenter, ROTARY_MONITOR_DELAY_MS);
            rangerMonitorLeft = new SensorMonitor<>(ultrasonicLeft, RANGER_MONITOR_DELAY_MS);
            rangerMonitorRight = new SensorMonitor<>(ultrasonicRight, RANGER_MONITOR_DELAY_MS);

            rotaryMonitorLeft.start();
            rotaryMonitorRight.start();
            rotaryMonitorCenter.start();
            rangerMonitorLeft.start();
            rangerMonitorRight.start();

            Thread.sleep(3000);
            waitForMonitors();

            rotaryLeftDefault = rotaryMonitorLeft.getValue().getDegrees();
            rotaryRightDefault = rotaryMonitorRight.getValue().getDegrees();
            rotaryCenterDefault = rotaryMonitorCenter.getValue().getDegrees();

            System.out.println("Default rotaryLeft: " + rotaryLeftDefault);
            System.out.println("Default rotaryRight: " + rotaryRightDefault);
            System.out.println("Default Center: " + rotaryCenterDefault);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void waitForMonitors() throws InterruptedException {
        while (!rotaryMonitorLeft.isValid()
                || !rotaryMonitorRight.isValid()
                || !rotaryMonitorCenter.isValid()
                || !rangerMonitorLeft.isValid()
                || !rangerMonitorRight.isValid()
                || rotaryMonitorLeft.getValue() == null
                || rotaryMonitorRight.getValue() == null
                || rotaryMonitorCenter.getValue() == null
                || rangerMonitorLeft.getValue() == null
                || rangerMonitorRight.getValue() == null) {
            Thread.sleep(50);
        }
    }

    void monitorRangers() {
        if (rangerMonitorLeft.isValid() && rangerMonitorRight.isValid()) {
            double rangerLeftValue = rangerMonitorLeft.getValue();
            double rangerRightValue = rangerMonitorRight.getValue();
            boolean leftBatchPresent = !(rangerLeftValue > RANGER_MAX_DISTANCE);
            boolean rightBatchPresent = !(rangerRightValue > RANGER_MAX_DISTANCE);

            if (machineState.isLeftBatchPresent() != leftBatchPresent) {
                machineState.setLeftBatchPresent(leftBatchPresent);
            }

            if (machineState.isRightBatchPresent() != rightBatchPresent) {
                machineState.setRightBatchPresent(rightBatchPresent);
            }
        }
    }

    void monitorGates() {
        if (rotaryMonitorLeft.isValid() && rotaryMonitorRight.isValid() && rotaryMonitorCenter.isValid()) {
            monitorGate(rotaryMonitorLeft.getValue().getDegrees(), rotaryLeftDefault, 0);
            monitorGate(rotaryMonitorRight.getValue().getDegrees(), rotaryRightDefault, 1);
            monitorGate(rotaryMonitorCenter.getValue().getDegrees(), rotaryCenterDefault, 2);
        }
    }

    void monitorGate(double rotaryValue, double defaultValue, int sensorNumber) {
        double difference = rotaryValue - defaultValue;

        boolean open = Math.abs(difference) > ROTARY_THRESHOLD;

        if (sensorNumber == 0) {
            if (!open) {
                machineState.setLeftGateOpen(false);
            } else if (!machineState.isLeftGateOpen()) {
                System.out.println(sensorNumber + ": Aperto;  Differenza: " + difference);
                machineState.incrementLeftDepoCounter(machineState.getLeftGateCounter());
                System.out.println("Counter Depo sinistra: " + machineState.getLeftDepoCounter());
                machineState.setLeftGateCounter(0);
                machineState.setLeftGateOpen(true);
            }
        } else if (sensorNumber == 1) {
            if (!open) {
                machineState.setRightGateOpen(false);
            } else if (!machineState.isRightGateOpen()) {
                System.out.println(sensorNumber + ": Aperto;  Differenza: " + difference);
                machineState.incrementRightDepoCounter(machineState.getRightGateCounter());
                System.out.println("Counter Depo destra: " + machineState.getRightDepoCounter());
                machineState.setRightGateCounter(0);
                machineState.setRightGateOpen(true);
            }
        } else if (sensorNumber == 2) {
            if (!open) {
                machineState.setSortingOpen(false);
            } else if (!machineState.isSortingOpen()) {
                if (difference > 0) {
                    System.out.println(sensorNumber + ": Aperto - Sinistra;  Differenza: " + difference);
                    machineState.incrementLeftGateCounter();
                    System.out.println("Counter sinistra: " + machineState.getLeftGateCounter());
                } else {
                    System.out.println(sensorNumber + ": Aperto - Destra;  Differenza: " + difference);
                    machineState.incrementRightGateCounter();
                    System.out.println("Counter destra: " + machineState.getRightGateCounter());
                }
                machineState.setSortingOpen(true);
            }
        }
    }

    public void run() throws InterruptedException {
        Logger.getLogger("GrovePi").setLevel(Level.OFF);
        Logger.getLogger("RaspberryPi").setLevel(Level.OFF);

        setup();

        while (true) {
            monitorGates();
            monitorRangers();

            if (!machineState.equals(machineStatePrev)) {
                influxDB.write(machineState);
                machineStatePrev = new MachineState(machineState);
            }
            Thread.sleep(LOOP_DELAY_MS);
        }
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.run();
    }
}
