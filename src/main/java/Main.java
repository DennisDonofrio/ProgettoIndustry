import com.influxdb.client.domain.Bucket;
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

    final int NR_PRODUCTS = 10;
    final double RANGER_MAX_DISTANCE = 15.0;
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

    boolean isDefaultSet = false;

    double rotaryLeftDefault = 0;
    double rotaryRightDefault = 0;
    double rotaryCenterDefault = 0;

    MachineState machineState;
    InfluxDB influxDB;

    void setup(){
        try{
            machineState = new MachineState();
            influxDB = new InfluxDB(TOKEN, BUCKET, ORG);

            grovePi = new GrovePi4J();
            rotaryLeft = new GroveRotarySensor(grovePi, 0);
            rotaryRight = new GroveRotarySensor(grovePi, 1);
            rotaryCenter = new GroveRotarySensor(grovePi, 2);
            ultrasonicLeft = new GroveUltrasonicRanger(grovePi, 0);
            ultrasonicRight = new GroveUltrasonicRanger(grovePi, 1);
            loadButton = new GroveButton(grovePi, 2);

            rotaryMonitorLeft = new SensorMonitor<>(rotaryLeft, 500);
            rotaryMonitorRight = new SensorMonitor<>(rotaryRight, 500);
            rotaryMonitorCenter = new SensorMonitor<>(rotaryCenter, 500);
            rangerMonitorLeft = new SensorMonitor<>(ultrasonicLeft, 500);
            rangerMonitorRight = new SensorMonitor<>(ultrasonicRight, 500);
            loadButtonMonitor = new SensorMonitor<>(loadButton, 500);

            rotaryMonitorLeft.start();
            rotaryMonitorRight.start();
            rotaryMonitorCenter.start();
            rangerMonitorLeft.start();
            rangerMonitorRight.start();
            loadButtonMonitor.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void monitorRangers(){
        if(rangerMonitorLeft.isValid() && rangerMonitorRight.isValid()){
            double rangerLeftValue = rangerMonitorLeft.getValue();
            double rangerRightValue = rangerMonitorRight.getValue();

            machineState.setRightBatchPresent(rangerRightValue > RANGER_MAX_DISTANCE);
            machineState.setLeftBatchPresent(rangerLeftValue > RANGER_MAX_DISTANCE);


        }
    }

    void monitorGates(){
        if(rotaryMonitorLeft.isValid() && rotaryMonitorRight.isValid() && rotaryMonitorCenter.isValid()){

            double rotaryLeftValue = rotaryMonitorLeft.getValue().getDegrees();
            double rotaryRightValue = rotaryMonitorRight.getValue().getDegrees();
            double rotaryCenterValue = rotaryMonitorCenter.getValue().getDegrees();


            if(!isDefaultSet){
                rotaryLeftDefault = rotaryLeftValue;
                rotaryRightDefault = rotaryRightValue;
                rotaryCenterDefault = rotaryCenterValue;
                isDefaultSet = true;
            }else{
                double centerOffset = rotaryCenterValue - rotaryCenterDefault;
                double leftOffset = Math.abs(rotaryLeftValue - rotaryLeftDefault);
                double rightOffset = Math.abs(rotaryRightValue - rotaryRightDefault);


                if(centerOffset > -20.0 && centerOffset < 20 && machineState.isSortingOpen()){
                    machineState.setSortingOpen(false);
                }
                if(centerOffset > 20.0 && !machineState.isSortingOpen()){
                    machineState.incrementLeftGateCounter();
                    machineState.setSortingOpen(true);
                    System.out.println("2: Aperto - Sinistra;  Differenza: " + (rotaryRightValue - rotaryCenterDefault));
                    System.out.println("Counter destra: " + machineState.getLeftGateCounter());
                }
                if(centerOffset < -20.0 && !machineState.isSortingOpen()){
                    machineState.incrementRightGateCounter();
                    machineState.setSortingOpen(true);
                    System.out.println("2: Aperto - Destra;  Differenza: " + (rotaryCenterValue - rotaryCenterDefault));
                    System.out.println("Counter destra: " + machineState.getRightGateCounter());
                }
                if(leftOffset < 30.0 && machineState.isLeftGateOpen()){
                    machineState.setLeftGateOpen(false);
                }
                if(leftOffset > 30.0 && !machineState.isLeftGateOpen()){
                    System.out.println("0: Aperto;  Differenza: " + Math.abs(rotaryLeftValue - rotaryLeftDefault));
                    machineState.incrementLeftDepoCounter(machineState.getLeftGateCounter());
                    machineState.setLeftGateCounter(0);
                    machineState.setLeftGateOpen(true);
                }
                if(rightOffset < 30.0 && machineState.isRightGateOpen()){
                    machineState.setRightGateOpen(false);
                }
                if(rightOffset > 30.0 && !machineState.isRightGateOpen()){
                    System.out.println("1: Aperto;  Differenza: " + Math.abs(rotaryRightValue - rotaryRightDefault));
                    machineState.incrementRightDepoCounter(machineState.getRightGateCounter());
                    machineState.setRightGateCounter(0);
                    machineState.setRightGateOpen(true);
                }
            }
        }
    }

    public void run() throws InterruptedException {
        Logger.getLogger("GrovePi").setLevel(Level.OFF);
        Logger.getLogger("RaspberryPi").setLevel(Level.OFF);

        setup();

        while (true){

            monitorGates();

            influxDB.write(machineState);

            //double rotaryMonitorValue = rotaryMonitor.isValid() ? rotaryMonitor.getValue().getDegrees() : 150;



            Thread.sleep(500);
        }
    }

    public static void main(String[] args) throws Exception{
        Main main = new Main();
        main.run();
    }
}
