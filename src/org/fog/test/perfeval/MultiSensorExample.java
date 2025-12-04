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
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;
import org.fog.utils.distribution.ExponentialDistribution;

/**
 * Lab 03: Sensors with Different Tuple Emission Rates
 * 
 * This example demonstrates how to configure and use sensors with different
 * tuple emission rates in iFogSim2:
 * 1. Temperature Sensor: Deterministic distribution (5 ms)
 * 2. Heartbeat Sensor: Deterministic distribution (2 ms)
 * 3. Motion Sensor: Exponential distribution (mean 10 ms)
 * 
 * @author Lab Assignment
 */
public class MultiSensorExample {
	
	static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	static List<Sensor> sensors = new ArrayList<Sensor>();
	static List<Actuator> actuators = new ArrayList<Actuator>();
	
	private static boolean CLOUD = false;
	
	public static void main(String[] args) {
		
		Log.printLine("Starting Multi-Sensor Example with Different Emission Rates...");
		
		try {
			Log.disable();
			int numUsers = 1;
			Calendar calendar = Calendar.getInstance();
			boolean traceFlag = false;
			
			CloudSim.init(numUsers, calendar, traceFlag);
			
			String appId = "multi_sensor_app";
			
			FogBroker broker = new FogBroker("broker");
			
			Application application = createApplication(appId, broker.getId());
			application.setUserId(broker.getId());
			
			createFogDevices(broker.getId(), appId);
			
			Controller controller = null;
			
			ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();
			
			// Map processing modules to edge device
			moduleMapping.addModuleToDevice("temperature_processor", "edge-0");
			moduleMapping.addModuleToDevice("heartbeat_processor", "edge-0");
			moduleMapping.addModuleToDevice("motion_processor", "edge-0");
			moduleMapping.addModuleToDevice("alert_generator", "edge-0");
			
			controller = new Controller("master-controller", fogDevices, sensors, actuators);
			
			controller.submitApplication(application, 
					new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping));
			
			TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
			
			CloudSim.startSimulation();
			
			CloudSim.stopSimulation();
			
			Log.printLine("Multi-Sensor Example finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Errors occurred during simulation");
		}
	}
	
	/**
	 * Creates the physical topology with fog devices and sensors
	 * Demonstrates sensor attachment to specific edge devices
	 */
	private static void createFogDevices(int userId, String appId) {
		// Create Cloud at the top of hierarchy (level 0)
		FogDevice cloud = createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25);
		cloud.setParentId(-1);
		fogDevices.add(cloud);
		
		// Create proxy server (level 1)
		FogDevice proxy = createFogDevice("proxy-server", 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333);
		proxy.setParentId(cloud.getId());
		proxy.setUplinkLatency(100); // 100 ms latency to cloud
		fogDevices.add(proxy);
		
		// Create edge device (level 2) - this is where we'll attach our sensors
		FogDevice edge = createFogDevice("edge-0", 1000, 2000, 10000, 10000, 2, 0.0, 90.0, 80.0);
		edge.setParentId(proxy.getId());
		edge.setUplinkLatency(4); // 4 ms latency to proxy
		fogDevices.add(edge);
		
		// ========================================================================
		// SENSOR CREATION - Different Tuple Emission Rates
		// ========================================================================
		
		// 1. Temperature Sensor - Deterministic Distribution (5 ms)
		// This sensor emits data exactly every 5 milliseconds
		Sensor temperatureSensor = new Sensor(
			"temperature-sensor-0",           // Sensor name
			"TEMPERATURE_DATA",                // Tuple type emitted
			userId,                            // User ID
			appId,                             // Application ID
			new DeterministicDistribution(5)   // Emission rate: 5 ms (deterministic)
		);
		temperatureSensor.setGatewayDeviceId(edge.getId()); // Attach to edge device
		temperatureSensor.setLatency(1.0);                  // 1 ms latency to edge
		sensors.add(temperatureSensor);
		
		// 2. Heartbeat Sensor - Deterministic Distribution (2 ms)
		// This sensor emits data exactly every 2 milliseconds (higher frequency)
		Sensor heartbeatSensor = new Sensor(
			"heartbeat-sensor-0",              // Sensor name
			"HEARTBEAT_DATA",                  // Tuple type emitted
			userId,                            // User ID
			appId,                             // Application ID
			new DeterministicDistribution(2)   // Emission rate: 2 ms (deterministic)
		);
		heartbeatSensor.setGatewayDeviceId(edge.getId()); // Attach to same edge device
		heartbeatSensor.setLatency(1.0);                  // 1 ms latency to edge
		sensors.add(heartbeatSensor);
		
		// 3. Motion Sensor - Exponential Distribution (mean 10 ms)
		// This sensor emits data with varying intervals following exponential distribution
		// Average emission rate is 10 ms, but actual intervals vary
		Sensor motionSensor = new Sensor(
			"motion-sensor-0",                 // Sensor name
			"MOTION_DATA",                     // Tuple type emitted
			userId,                            // User ID
			appId,                             // Application ID
			new ExponentialDistribution(10)    // Emission rate: mean 10 ms (exponential)
		);
		motionSensor.setGatewayDeviceId(edge.getId()); // Attach to same edge device
		motionSensor.setLatency(1.0);                  // 1 ms latency to edge
		sensors.add(motionSensor);
		
		// ========================================================================
		// ACTUATOR CREATION
		// ========================================================================
		
		// Create an Alert Actuator connected to the edge device
		Actuator alertActuator = new Actuator(
			"alert-actuator-0",    // Actuator name
			userId,                 // User ID
			appId,                  // Application ID
			"ALERT_CONTROL"        // Actuator type
		);
		alertActuator.setGatewayDeviceId(edge.getId()); // Attach to edge device
		alertActuator.setLatency(1.0);                   // 1 ms latency
		actuators.add(alertActuator);
		
		Log.printLine("\n=== Sensor Configuration Summary ===");
		Log.printLine("1. Temperature Sensor: Deterministic 5ms -> " + (1000.0/5) + " tuples/sec");
		Log.printLine("2. Heartbeat Sensor: Deterministic 2ms -> " + (1000.0/2) + " tuples/sec");
		Log.printLine("3. Motion Sensor: Exponential mean 10ms -> ~" + (1000.0/10) + " tuples/sec (average)");
		Log.printLine("Total average throughput: ~" + ((1000.0/5) + (1000.0/2) + (1000.0/10)) + " tuples/sec");
		Log.printLine("====================================\n");
	}
	
	/**
	 * Creates a fog device with specified characteristics
	 */
	private static FogDevice createFogDevice(String nodeName, long mips,
			int ram, long upBw, long downBw, int level, double ratePerMips, 
			double busyPower, double idlePower) {
		
		List<Pe> peList = new ArrayList<Pe>();
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
			arch, os, vmm, host, timeZone, cost, costPerMem,
			costPerStorage, costPerBw);
		
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
	 * Creates the application model with processing modules
	 * Defines how data flows from sensors through processing modules to actuators
	 */
	@SuppressWarnings({"serial"})
	private static Application createApplication(String appId, int userId) {
		
		Application application = Application.createApplication(appId, userId);
		
		// Add processing modules
		application.addAppModule("temperature_processor", 10);  // 10 MIPS
		application.addAppModule("heartbeat_processor", 10);    // 10 MIPS
		application.addAppModule("motion_processor", 10);       // 10 MIPS
		application.addAppModule("alert_generator", 10);        // 10 MIPS
		
		// ========================================================================
		// APPLICATION EDGES - Define data flow paths
		// ========================================================================
		
		// Temperature sensor data flow
		application.addAppEdge("TEMPERATURE_DATA", "temperature_processor", 
			500, 500, "TEMPERATURE_DATA", Tuple.UP, AppEdge.SENSOR);
		application.addAppEdge("temperature_processor", "alert_generator", 
			100, 100, "TEMP_PROCESSED", Tuple.UP, AppEdge.MODULE);
		
		// Heartbeat sensor data flow
		application.addAppEdge("HEARTBEAT_DATA", "heartbeat_processor", 
			300, 300, "HEARTBEAT_DATA", Tuple.UP, AppEdge.SENSOR);
		application.addAppEdge("heartbeat_processor", "alert_generator", 
			100, 100, "HEART_PROCESSED", Tuple.UP, AppEdge.MODULE);
		
		// Motion sensor data flow
		application.addAppEdge("MOTION_DATA", "motion_processor", 
			1000, 1000, "MOTION_DATA", Tuple.UP, AppEdge.SENSOR);
		application.addAppEdge("motion_processor", "alert_generator", 
			200, 200, "MOTION_PROCESSED", Tuple.UP, AppEdge.MODULE);
		
		// Alert actuator control
		application.addAppEdge("alert_generator", "ALERT_CONTROL", 
			100, 100, 14, "ALERT_PARAMS", Tuple.DOWN, AppEdge.ACTUATOR);
		
		// ========================================================================
		// TUPLE MAPPING - Define selectivity (output tuples per input tuple)
		// ========================================================================
		
		application.addTupleMapping("temperature_processor", "TEMPERATURE_DATA", 
			"TEMP_PROCESSED", new FractionalSelectivity(1.0));
		application.addTupleMapping("heartbeat_processor", "HEARTBEAT_DATA", 
			"HEART_PROCESSED", new FractionalSelectivity(1.0));
		application.addTupleMapping("motion_processor", "MOTION_DATA", 
			"MOTION_PROCESSED", new FractionalSelectivity(1.0));
		
		// Alert generator produces alerts based on processed data
		// Lower selectivity means not every input produces an alert
		application.addTupleMapping("alert_generator", "TEMP_PROCESSED", 
			"ALERT_PARAMS", new FractionalSelectivity(0.1));  // 10% alert rate
		application.addTupleMapping("alert_generator", "HEART_PROCESSED", 
			"ALERT_PARAMS", new FractionalSelectivity(0.05)); // 5% alert rate
		application.addTupleMapping("alert_generator", "MOTION_PROCESSED", 
			"ALERT_PARAMS", new FractionalSelectivity(0.2));  // 20% alert rate
		
		// ========================================================================
		// APPLICATION LOOPS - For latency monitoring
		// ========================================================================
		
		final AppLoop tempLoop = new AppLoop(new ArrayList<String>() {{
			add("temperature_processor");
			add("alert_generator");
			add("ALERT_CONTROL");
		}});
		
		final AppLoop heartbeatLoop = new AppLoop(new ArrayList<String>() {{
			add("heartbeat_processor");
			add("alert_generator");
			add("ALERT_CONTROL");
		}});
		
		final AppLoop motionLoop = new AppLoop(new ArrayList<String>() {{
			add("motion_processor");
			add("alert_generator");
			add("ALERT_CONTROL");
		}});
		
		List<AppLoop> loops = new ArrayList<AppLoop>() {{
			add(tempLoop);
			add(heartbeatLoop);
			add(motionLoop);
		}};
		
		application.setLoops(loops);
		return application;
	}
}

