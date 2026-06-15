import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.pi4j.GrovePi4J;
import org.iot.raspberry.grovepi.sensors.analog.GroveRotarySensor;
import org.iot.raspberry.grovepi.sensors.data.GroveRotaryValue;
import org.iot.raspberry.grovepi.sensors.digital.GroveButton;
import org.iot.raspberry.grovepi.sensors.digital.GroveUltrasonicRanger;
import org.iot.raspberry.grovepi.sensors.synch.SensorMonitor;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    final double RANGER_MAX_DISTANCE = 50.0;
    final double BOX_POSITION_MAX_DISTANCE = 5.0;
    final double ROTARY_THRESHOLD = 30.0;
    final long LOOP_DELAY_MS = 10;
    final long ROTARY_MONITOR_DELAY_MS = 100;
    final long RANGER_MONITOR_DELAY_MS = 700;
    final long WARNING_THRESHOLD_MS = 6000;
    final long TIMING_UPDATE_INTERVAL_MS = 500;

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
    SensorMonitor<Boolean> loadButtonMonitor;

    double rotaryLeftDefault;
    double rotaryRightDefault;
    double rotaryCenterDefault;
    long lastTimingUpdateTime = 0;
    boolean waitingForBoxes = false;
    boolean pressButtonMessagePrinted = false;
    boolean previousLoadButtonPressed = false;
    Queue<Long> leftSortingTimes = new LinkedList<>();
    Queue<Long> rightSortingTimes = new LinkedList<>();

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
            loadButton = new GroveButton(grovePi, 2);

            rotaryMonitorLeft = new SensorMonitor<>(rotaryLeft, ROTARY_MONITOR_DELAY_MS);
            rotaryMonitorRight = new SensorMonitor<>(rotaryRight, ROTARY_MONITOR_DELAY_MS);
            rotaryMonitorCenter = new SensorMonitor<>(rotaryCenter, ROTARY_MONITOR_DELAY_MS);
            rangerMonitorLeft = new SensorMonitor<>(ultrasonicLeft, RANGER_MONITOR_DELAY_MS);
            rangerMonitorRight = new SensorMonitor<>(ultrasonicRight, RANGER_MONITOR_DELAY_MS);
            loadButtonMonitor = new SensorMonitor<>(loadButton, 100);

            rotaryMonitorLeft.start();
            rotaryMonitorRight.start();
            rotaryMonitorCenter.start();
            rangerMonitorLeft.start();
            rangerMonitorRight.start();
            loadButtonMonitor.start();

            Thread.sleep(3000);
            waitForMonitors();

            rotaryLeftDefault = rotaryMonitorLeft.getValue().getDegrees();
            rotaryRightDefault = rotaryMonitorRight.getValue().getDegrees();
            rotaryCenterDefault = rotaryMonitorCenter.getValue().getDegrees();

            previousLoadButtonPressed = loadButtonMonitor.getValue();
            printPressButtonMessage();
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
                || !loadButtonMonitor.isValid()
                || rotaryMonitorLeft.getValue() == null
                || rotaryMonitorRight.getValue() == null
                || rotaryMonitorCenter.getValue() == null
                || rangerMonitorLeft.getValue() == null
                || rangerMonitorRight.getValue() == null
                || loadButtonMonitor.getValue() == null) {
            Thread.sleep(50);
        }
    }

    void monitorLoadButton() {
        if (machineState.isMonitoringActive()) {
            return;
        }

        if (!waitingForBoxes) {
            printPressButtonMessage();
        }

        if (loadButtonMonitor.isValid() && loadButtonMonitor.getValue() != null) {
            boolean loadButtonPressed = loadButtonMonitor.getValue();
            if (loadButtonPressed && !previousLoadButtonPressed) {
                waitingForBoxes = true;
                pressButtonMessagePrinted = false;
                System.out.println("Load button pressed. Waiting for both containers within "
                        + BOX_POSITION_MAX_DISTANCE + " cm...");
            }
            previousLoadButtonPressed = loadButtonPressed;
        }

        if (waitingForBoxes) {
            startMonitoringIfBoxesReady();
        }
    }

    void printPressButtonMessage() {
        if (!pressButtonMessagePrinted) {
            System.out.println("Press the load button to start batch monitoring.");
            pressButtonMessagePrinted = true;
        }
    }

    boolean startMonitoringIfBoxesReady() {
        if (!checkBoxesReady()) {
            if (!machineState.isCheckBoxesError()) {
                System.out.println("CHECK_BOXES error: both containers must be positioned within "
                        + BOX_POSITION_MAX_DISTANCE + " cm before monitoring starts");
            }
            machineState.setCheckBoxesError(true);
            return false;
        }

        machineState.setCheckBoxesError(false);
        System.out.println("Batch monitoring started");
        waitingForBoxes = false;
        pressButtonMessagePrinted = false;
        leftSortingTimes.clear();
        rightSortingTimes.clear();
        machineState.startMonitoring(System.currentTimeMillis());
        return true;
    }

    boolean checkBoxesReady() {
        if (!rangerMonitorLeft.isValid()
                || !rangerMonitorRight.isValid()
                || rangerMonitorLeft.getValue() == null
                || rangerMonitorRight.getValue() == null) {
            return false;
        }

        return rangerMonitorLeft.getValue() <= BOX_POSITION_MAX_DISTANCE
                && rangerMonitorRight.getValue() <= BOX_POSITION_MAX_DISTANCE;
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

            if (machineState.isMonitoringActive()
                    && machineState.isBatchSeenDuringMonitoring()
                    && !leftBatchPresent
                    && !rightBatchPresent) {
                System.out.println("Batch monitoring finished");
                machineState.finishMonitoring(System.currentTimeMillis());
                waitingForBoxes = false;
                pressButtonMessagePrinted = false;
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
                int releasedApples = machineState.getLeftGateCounter();
                recordSortingToGateTimes(leftSortingTimes, releasedApples);
                machineState.incrementLeftDepoCounter(releasedApples);
                machineState.setLeftGateCounter(0);
                machineState.setLeftGateOpen(true);
            }
        } else if (sensorNumber == 1) {
            if (!open) {
                machineState.setRightGateOpen(false);
            } else if (!machineState.isRightGateOpen()) {
                int releasedApples = machineState.getRightGateCounter();
                recordSortingToGateTimes(rightSortingTimes, releasedApples);
                machineState.incrementRightDepoCounter(releasedApples);
                machineState.setRightGateCounter(0);
                machineState.setRightGateOpen(true);
            }
        } else if (sensorNumber == 2) {
            if (!open) {
                machineState.setSortingOpen(false);
            } else if (!machineState.isSortingOpen()) {
                if (difference > 0) {
                    leftSortingTimes.add(System.currentTimeMillis());
                    machineState.incrementLeftGateCounter();
                } else {
                    rightSortingTimes.add(System.currentTimeMillis());
                    machineState.incrementRightGateCounter();
                }
                machineState.setSortingOpen(true);
            }
        }
    }

    void recordSortingToGateTimes(Queue<Long> sortingTimes, int releasedApples) {
        long now = System.currentTimeMillis();
        for (int i = 0; i < releasedApples && !sortingTimes.isEmpty(); i++) {
            machineState.addSortingToGateTime(now - sortingTimes.poll());
        }
    }

    void updateTimingWarnings() {
        long now = System.currentTimeMillis();
        if (now - lastTimingUpdateTime >= TIMING_UPDATE_INTERVAL_MS) {
            machineState.updateTimingWarnings(now, WARNING_THRESHOLD_MS);
            lastTimingUpdateTime = now;
        }
    }

    public void run() throws InterruptedException {
        Logger.getLogger("GrovePi").setLevel(Level.OFF);
        Logger.getLogger("RaspberryPi").setLevel(Level.OFF);

        setup();

        while (true) {
            monitorLoadButton();

            if (machineState.isMonitoringActive()) {
                monitorGates();
                monitorRangers();
            }

            updateTimingWarnings();

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
