package org.fog.test.perfeval;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.HealthcarePlacementPolicy;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

/**
 * IoT Healthcare Application Simulation with Custom Placement Policy
 * 
 * Application Modules:
 * - Sensor Data Processing (Edge)
 * - Feature Extraction (Gateway)
 * - Deep Analytics (Cloud)
 * 
 * @author Saksham Shrey
 */
public class HealthcareApplication {
    
    static List<FogDevice> fogDevices = new ArrayList<>();
    static List<Sensor> sensors = new ArrayList<>();
    static List<Actuator> actuators = new ArrayList<>();
    
    static int numOfPatients = 10;
    
    public static void main(String[] args) {
        Log.printLine("Starting Healthcare Application...");
        
        try {
            Log.disable();
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            
            CloudSim.init(num_user, calendar, trace_flag);
            
            String appId = "healthcare";
            
            FogBroker broker = new FogBroker("broker");
            
            Application application = createApplication(appId, broker.getId());
            application.setUserId(broker.getId());
            
            createFogDevices(broker.getId(), appId);
            
            Controller controller = new Controller("master-controller", fogDevices, sensors, actuators);
            
            // Use our custom healthcare placement policy
            HealthcarePlacementPolicy placementPolicy = new HealthcarePlacementPolicy(fogDevices, application);
            
            controller.submitApplication(application, 0, placementPolicy);
            
            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
            
            CloudSim.startSimulation();
            CloudSim.stopSimulation();
            
            Log.printLine("Healthcare Application finished!");
            
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }
    
    /**
     * Creates the fog infrastructure topology
     */
    private static void createFogDevices(int userId, String appId) {
        // Cloud (level 0) - Unlimited resources
        FogDevice cloud = createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25);
        cloud.setParentId(-1);
        fogDevices.add(cloud);
        
        // Gateway (level 2) - Moderate resources
        FogDevice gateway = createFogDevice("gateway", 2800, 4000, 10000, 10000, 2, 0.0, 107.339, 83.4333);
        gateway.setParentId(cloud.getId());
        gateway.setUplinkLatency(100);
        fogDevices.add(gateway);
        
        // Edge devices (level 3) - Limited resources, one per patient
        for (int i = 0; i < numOfPatients; i++) {
            String deviceId = "patient-" + i;
            FogDevice wearable = createFogDevice("edge-" + deviceId, 500, 1000, 10000, 10000, 3, 0, 87.53, 82.44);
            wearable.setParentId(gateway.getId());
            wearable.setUplinkLatency(2);
            fogDevices.add(wearable);
            
            // Add sensors (heart rate, blood pressure, temperature)
            Sensor hrSensor = new Sensor("hr-sensor-" + deviceId, "HEART_RATE", userId, appId, 
                                        new DeterministicDistribution(5));
            hrSensor.setGatewayDeviceId(wearable.getId());
            hrSensor.setLatency(1.0);
            sensors.add(hrSensor);
            
            // Add actuator (alert device)
            Actuator alertDevice = new Actuator("alert-" + deviceId, userId, appId, "ALERT");
            alertDevice.setGatewayDeviceId(wearable.getId());
            alertDevice.setLatency(1.0);
            actuators.add(alertDevice);
        }
    }
    
    /**
     * Creates a fog device with specified parameters
     */
    private static FogDevice createFogDevice(String nodeName, long mips, int ram, long upBw, long downBw, 
                                            int level, double ratePerMips, double busyPower, double idlePower) {
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerOverbooking(mips)));
        
        int hostId = FogUtils.generateEntityId();
        long storage = 1000000;
        int bw = 10000;
        
        PowerHost host = new PowerHost(
            hostId,
            new RamProvisionerSimple(ram),
            new BwProvisionerOverbooking(bw),
            storage,
            peList,
            new StreamOperatorScheduler(peList),
            new FogLinearPowerModel(busyPower, idlePower)
        );
        
        List<Host> hostList = new ArrayList<>();
        hostList.add(host);
        
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;
        LinkedList<Storage> storageList = new LinkedList<>();
        
        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
            arch, os, vmm, host, time_zone, cost, costPerMem, costPerStorage, costPerBw);
        
        FogDevice fogdevice = null;
        try {
            fogdevice = new FogDevice(nodeName, characteristics, 
                new AppModuleAllocationPolicy(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        fogdevice.setLevel(level);
        return fogdevice;
    }
    
    /**
     * Creates the healthcare application model
     */
    @SuppressWarnings({"serial"})
    private static Application createApplication(String appId, int userId) {
        Application application = Application.createApplication(appId, userId);
        
        // Add application modules
        application.addAppModule("sensor_data_processing", 10);  // Lightweight
        application.addAppModule("feature_extraction", 10);      // Medium
        application.addAppModule("deep_analytics", 10);          // Heavy
        
        // Add edges (data flow)
        application.addAppEdge("HEART_RATE", "sensor_data_processing", 1000, 500, 
                              "RAW_DATA", Tuple.UP, AppEdge.SENSOR);
        
        application.addAppEdge("sensor_data_processing", "feature_extraction", 500, 500, 
                              "PROCESSED_DATA", Tuple.UP, AppEdge.MODULE);
        
        application.addAppEdge("feature_extraction", "deep_analytics", 100, 1000, 
                              "FEATURES", Tuple.UP, AppEdge.MODULE);
        
        application.addAppEdge("deep_analytics", "ALERT", 100, 28, 100, 
                              "ALERT_SIGNAL", Tuple.DOWN, AppEdge.ACTUATOR);
        
        // Add tuple mappings (selectivity)
        application.addTupleMapping("sensor_data_processing", "RAW_DATA", "PROCESSED_DATA", 
                                   new FractionalSelectivity(1.0));
        
        application.addTupleMapping("feature_extraction", "PROCESSED_DATA", "FEATURES", 
                                   new FractionalSelectivity(0.5));
        
        application.addTupleMapping("deep_analytics", "FEATURES", "ALERT_SIGNAL", 
                                   new FractionalSelectivity(0.1));
        
        // Add application loop for monitoring
        final AppLoop loop1 = new AppLoop(new ArrayList<String>() {{
            add("sensor_data_processing");
            add("feature_extraction");
            add("deep_analytics");
        }});
        
        List<AppLoop> loops = new ArrayList<AppLoop>() {{ add(loop1); }};
        application.setLoops(loops);
        
        return application;
    }
}


