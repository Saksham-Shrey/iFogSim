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
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

/**
 * Simulation demonstrating heterogeneous fog nodes with different configurations
 * and two different application models.
 * 
 * Topology:
 * - Cloud: High-end computing resources
 * - Gateway: Mid-tier fog node
 * - EdgeDevice: Low-power edge node
 * 
 * Applications:
 * - App1 (TempApp): Temperature monitoring with sensor → processor → actuator
 * - App2 (AnalyticsApp): Data analytics with sensor → edge analyzer → cloud storage → actuator
 * 
 * @author iFogSim
 */
public class HeterogeneousFogNodesApp {
	
	static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	static List<Sensor> sensors = new ArrayList<Sensor>();
	static List<Actuator> actuators = new ArrayList<Actuator>();
	
	private static final double SENSOR_TRANSMISSION_TIME = 5.0; // 5 seconds
	
	public static void main(String[] args) {
		
		Log.printLine("Starting Heterogeneous Fog Nodes Application Simulation...");
		
		try {
			Log.disable();
			int numUser = 1;
			Calendar calendar = Calendar.getInstance();
			boolean traceFlag = false;
			
			CloudSim.init(numUser, calendar, traceFlag);
			
			// --- Step 1: Create fog topology with heterogeneous devices ---
			createHeterogeneousFogTopology();
			
			// --- Step 2: Create two brokers for two applications ---
			FogBroker broker1 = new FogBroker("broker_1");
			FogBroker broker2 = new FogBroker("broker_2");
			
			// --- Step 3: Create the two applications ---
			Application app1 = createApp1("TempApp", broker1.getId());
			Application app2 = createApp2("AnalyticsApp", broker2.getId());
			
			app1.setUserId(broker1.getId());
			app2.setUserId(broker2.getId());
			
			// --- Step 4: Create sensors and actuators for both applications ---
			createSensorsAndActuators(broker1.getId(), broker2.getId(), app1.getAppId(), app2.getAppId());
			
			// --- Step 5: Create module mapping ---
			ModuleMapping mapping1 = ModuleMapping.createModuleMapping();
			ModuleMapping mapping2 = ModuleMapping.createModuleMapping();
			
			// App1: run TempProcessor on the edge device
			mapping1.addModuleToDevice("TempProcessor", "edgeDevice");
			
			// App2: run EdgeAnalyzer on gateway, CloudStorage on cloud
			mapping2.addModuleToDevice("EdgeAnalyzer", "gateway");
			mapping2.addModuleToDevice("CloudStorage", "cloud");
			
			// --- Step 6: Create controller and deploy applications ---
			Controller controller = new Controller("master-controller", fogDevices, sensors, actuators);
			
			controller.submitApplication(app1, new ModulePlacementMapping(fogDevices, app1, mapping1));
			controller.submitApplication(app2, 0, new ModulePlacementMapping(fogDevices, app2, mapping2));
			
			TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
			
			// --- Step 7: Start simulation ---
			CloudSim.startSimulation();
			
			CloudSim.stopSimulation();
			
			Log.printLine("Heterogeneous Fog Nodes Application Simulation finished!");
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Error occurred during simulation");
		}
	}
	
	/**
	 * Creates heterogeneous fog topology with three different device types:
	 * 1. Cloud - High-end computing resources
	 * 2. Gateway - Mid-tier fog node
	 * 3. EdgeDevice - Low-power edge device
	 */
	private static void createHeterogeneousFogTopology() {
		// Create cloud device with high computing capacity
		FogDevice cloud = createFogDevice("cloud", 44800, 40000, 1000000, 10000, 0, 0.01, 16*103, 16*83.25);
		cloud.setParentId(-1);
		fogDevices.add(cloud);
		
		// Create gateway device with moderate computing capacity
		FogDevice gateway = createFogDevice("gateway", 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.433);
		gateway.setParentId(cloud.getId());
		gateway.setUplinkLatency(100); // 100 ms latency to cloud
		fogDevices.add(gateway);
		
		// Create edge device with limited computing capacity
		FogDevice edgeDevice = createFogDevice("edgeDevice", 1000, 512, 1000, 1000, 2, 0.0, 87.53, 82.44);
		edgeDevice.setParentId(gateway.getId());
		edgeDevice.setUplinkLatency(2); // 2 ms latency to gateway
		fogDevices.add(edgeDevice);
		
		Log.printLine("Heterogeneous fog topology created:");
		Log.printLine("  - Cloud: MIPS=44800, RAM=40000 MB, BW=1000000 Mbps");
		Log.printLine("  - Gateway: MIPS=2800, RAM=4000 MB, BW=10000 Mbps");
		Log.printLine("  - EdgeDevice: MIPS=1000, RAM=512 MB, BW=1000 Mbps");
	}
	
	/**
	 * Creates sensors and actuators for both applications
	 */
	private static void createSensorsAndActuators(int userId1, int userId2, String appId1, String appId2) {
		// App1 (TempApp): Temperature sensor and cooling actuator on edge device
		FogDevice edgeDevice = getFogDeviceByName("edgeDevice");
		
		Sensor tempSensor = new Sensor("temp-sensor", "TEMP_DATA", userId1, appId1, 
				new DeterministicDistribution(SENSOR_TRANSMISSION_TIME));
		tempSensor.setGatewayDeviceId(edgeDevice.getId());
		tempSensor.setLatency(1.0); // 1 ms latency
		sensors.add(tempSensor);
		
		Actuator coolingActuator = new Actuator("cooling-actuator", userId1, appId1, "COOLING_COMMAND");
		coolingActuator.setGatewayDeviceId(edgeDevice.getId());
		coolingActuator.setLatency(1.0); // 1 ms latency
		actuators.add(coolingActuator);
		
		// App2 (AnalyticsApp): Data sensor and display actuator on edge device
		Sensor dataSensor = new Sensor("data-sensor", "RAW_DATA", userId2, appId2, 
				new DeterministicDistribution(SENSOR_TRANSMISSION_TIME));
		dataSensor.setGatewayDeviceId(edgeDevice.getId());
		dataSensor.setLatency(1.0); // 1 ms latency
		sensors.add(dataSensor);
		
		Actuator displayActuator = new Actuator("display-actuator", userId2, appId2, "DISPLAY_DATA");
		displayActuator.setGatewayDeviceId(edgeDevice.getId());
		displayActuator.setLatency(1.0); // 1 ms latency
		actuators.add(displayActuator);
		
		Log.printLine("Sensors and actuators created for both applications");
	}
	
	/**
	 * Helper method to get fog device by name
	 */
	private static FogDevice getFogDeviceByName(String name) {
		for (FogDevice device : fogDevices) {
			if (device.getName().equals(name)) {
				return device;
			}
		}
		return null;
	}
	
	/**
	 * Creates a fog device with specified parameters
	 * 
	 * @param nodeName name of the device
	 * @param mips MIPS (Million Instructions Per Second)
	 * @param ram RAM in MB
	 * @param upBw uplink bandwidth in Mbps
	 * @param downBw downlink bandwidth in Mbps
	 * @param level hierarchy level (0=cloud, 1=gateway, 2=edge)
	 * @param ratePerMips cost rate per MIPS
	 * @param busyPower power consumption when busy (Watts)
	 * @param idlePower power consumption when idle (Watts)
	 * @return FogDevice instance
	 */
	private static FogDevice createFogDevice(String nodeName, long mips, int ram, 
			long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {
		
		List<Pe> peList = new ArrayList<Pe>();
		peList.add(new Pe(0, new PeProvisionerOverbooking(mips)));
		
		int hostId = FogUtils.generateEntityId();
		long storage = 1000000; // 1 GB storage
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
		
		List<Host> hostList = new ArrayList<Host>();
		hostList.add(host);
		
		String arch = "x86";
		String os = "Linux";
		String vmm = "Xen";
		double timeZone = 10.0;
		double cost = 3.0;
		double costPerMem = 0.05;
		double costPerStorage = 0.001;
		double costPerBw = 0.0;
		LinkedList<Storage> storageList = new LinkedList<Storage>();
		
		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
				arch, os, vmm, host, timeZone, cost, costPerMem, costPerStorage, costPerBw);
		
		FogDevice fogDevice = null;
		try {
			fogDevice = new FogDevice(nodeName, characteristics, 
					new AppModuleAllocationPolicy(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		fogDevice.setLevel(level);
		return fogDevice;
	}
	
	/**
	 * Creates Application 1: Temperature Monitoring Application
	 * Flow: Sensor → TempProcessor → Actuator
	 * 
	 * @param appId application identifier
	 * @param userId user identifier
	 * @return Application instance
	 */
	@SuppressWarnings({"serial"})
	private static Application createApp1(String appId, int userId) {
		
		Application application = Application.createApplication(appId, userId);
		
		// Add application modules
		application.addAppModule("TempProcessor", 10); // CPU length of processing
		
		// Define application edges (data flow)
		// Sensor → TempProcessor: Temperature data from sensor
		application.addAppEdge("TEMP_DATA", "TempProcessor", 1000, 500, "TEMP_DATA", Tuple.UP, AppEdge.SENSOR);
		
		// TempProcessor → Actuator: Cooling command
		application.addAppEdge("TempProcessor", "COOLING_COMMAND", 500, 500, "COOLING_COMMAND", Tuple.DOWN, AppEdge.ACTUATOR);
		
		// Define tuple mappings (selectivity)
		application.addTupleMapping("TempProcessor", "TEMP_DATA", "COOLING_COMMAND", new FractionalSelectivity(1.0));
		
		// Define application loops for latency monitoring
		final AppLoop loop1 = new AppLoop(new ArrayList<String>() {{
			add("TEMP_DATA");
			add("TempProcessor");
			add("COOLING_COMMAND");
		}});
		List<AppLoop> loops = new ArrayList<AppLoop>() {{
			add(loop1);
		}};
		application.setLoops(loops);
		
		Log.printLine("Application 1 (TempApp) created: Sensor → TempProcessor → Actuator");
		
		return application;
	}
	
	/**
	 * Creates Application 2: Analytics Application
	 * Flow: Sensor → EdgeAnalyzer → CloudStorage → Actuator
	 * 
	 * @param appId application identifier
	 * @param userId user identifier
	 * @return Application instance
	 */
	@SuppressWarnings({"serial"})
	private static Application createApp2(String appId, int userId) {
		
		Application application = Application.createApplication(appId, userId);
		
		// Add application modules
		application.addAppModule("EdgeAnalyzer", 10); // Edge processing module
		application.addAppModule("CloudStorage", 10); // Cloud storage module
		
		// Define application edges (data flow)
		// Sensor → EdgeAnalyzer: Raw data from sensor
		application.addAppEdge("RAW_DATA", "EdgeAnalyzer", 2000, 1000, "RAW_DATA", Tuple.UP, AppEdge.SENSOR);
		
		// EdgeAnalyzer → CloudStorage: Analyzed data
		application.addAppEdge("EdgeAnalyzer", "CloudStorage", 1000, 1000, "ANALYZED_DATA", Tuple.UP, AppEdge.MODULE);
		
		// CloudStorage → EdgeAnalyzer: Stored confirmation (optional feedback)
		application.addAppEdge("CloudStorage", "EdgeAnalyzer", 100, 100, "STORAGE_CONFIRM", Tuple.DOWN, AppEdge.MODULE);
		
		// EdgeAnalyzer → Actuator: Display data
		application.addAppEdge("EdgeAnalyzer", "DISPLAY_DATA", 500, 500, "DISPLAY_DATA", Tuple.DOWN, AppEdge.ACTUATOR);
		
		// Define tuple mappings (selectivity)
		application.addTupleMapping("EdgeAnalyzer", "RAW_DATA", "ANALYZED_DATA", new FractionalSelectivity(1.0));
		application.addTupleMapping("CloudStorage", "ANALYZED_DATA", "STORAGE_CONFIRM", new FractionalSelectivity(1.0));
		application.addTupleMapping("EdgeAnalyzer", "STORAGE_CONFIRM", "DISPLAY_DATA", new FractionalSelectivity(1.0));
		
		// Define application loops for latency monitoring
		final AppLoop loop1 = new AppLoop(new ArrayList<String>() {{
			add("RAW_DATA");
			add("EdgeAnalyzer");
			add("CloudStorage");
			add("EdgeAnalyzer");
			add("DISPLAY_DATA");
		}});
		List<AppLoop> loops = new ArrayList<AppLoop>() {{
			add(loop1);
		}};
		application.setLoops(loops);
		
		Log.printLine("Application 2 (AnalyticsApp) created: Sensor → EdgeAnalyzer → CloudStorage → Actuator");
		
		return application;
	}
}

