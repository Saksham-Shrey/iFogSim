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
 * Cloud Datacenter with Multiple Actuators
 * Temperature Sensor controls both Fan and Alarm actuators
 */
public class SimpleCloudDatacenter_MultiActuator {
	static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	static List<Sensor> sensors = new ArrayList<Sensor>();
	static List<Actuator> actuators = new ArrayList<Actuator>();
	
	public static void main(String[] args) {
		Log.printLine("Starting Cloud Datacenter with Multiple Actuators...");

		try {
			Log.disable();
			int numUser = 1;
			Calendar calendar = Calendar.getInstance();
			boolean traceFlag = false;

			CloudSim.init(numUser, calendar, traceFlag);

			String appId = "cloud_app_multi_actuator";
			
			FogBroker broker = new FogBroker("broker");
			
			Application application = createApplication(appId, broker.getId());
			application.setUserId(broker.getId());
			
			createFogDevices(broker.getId(), appId);
			
			ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();
			moduleMapping.addModuleToDevice("processor", "cloud");
			
			Controller controller = new Controller("master-controller", fogDevices, sensors, actuators);
			
			controller.submitApplication(application, 0, 
					new ModulePlacementMapping(fogDevices, application, moduleMapping));

			TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			Log.printLine("Cloud Datacenter with Multiple Actuators finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happened");
		}
	}

	/**
	 * Creates cloud datacenter with 1 temperature sensor and 2 actuators (fan and alarm)
	 */
	private static void createFogDevices(int userId, String appId) {
		FogDevice cloud = createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25);
		cloud.setParentId(-1);
		fogDevices.add(cloud);
		
		// Create temperature sensor
		Sensor tempSensor = new Sensor("temp-sensor", "TEMPERATURE", userId, appId, 
			new DeterministicDistribution(10));
		sensors.add(tempSensor);
		tempSensor.setGatewayDeviceId(cloud.getId());
		tempSensor.setLatency(2.0);
		
		// Create Fan actuator
		Actuator fan = new Actuator("fan", userId, appId, "FAN_CONTROL");
		actuators.add(fan);
		fan.setGatewayDeviceId(cloud.getId());
		fan.setLatency(1.0);
		
		// Create Alarm actuator
		Actuator alarm = new Actuator("alarm", userId, appId, "ALARM_CONTROL");
		actuators.add(alarm);
		alarm.setGatewayDeviceId(cloud.getId());
		alarm.setLatency(1.5);
	}

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
	 * Creates application where temperature sensor controls both fan and alarm
	 * Sensor -> Processor -> Fan (always)
	 *                    -> Alarm (50% of time, when temperature is critical)
	 */
	@SuppressWarnings({"serial"})
	private static Application createApplication(String appId, int userId) {
		Application application = Application.createApplication(appId, userId);
		
		application.addAppModule("processor", 10);
		
		// Add edge from sensor to processor
		application.addAppEdge("TEMPERATURE", "processor", 1000, 500, "TEMPERATURE", 
			Tuple.UP, AppEdge.SENSOR);
		
		// Add edge from processor to FAN actuator
		application.addAppEdge("processor", "FAN_CONTROL", 500, 500, "FAN_COMMAND", 
			Tuple.DOWN, AppEdge.ACTUATOR);
		
		// Add edge from processor to ALARM actuator
		application.addAppEdge("processor", "ALARM_CONTROL", 300, 300, "ALARM_COMMAND", 
			Tuple.DOWN, AppEdge.ACTUATOR);
		
		// Define tuple mappings
		// Fan always responds to temperature readings (1.0 selectivity)
		application.addTupleMapping("processor", "TEMPERATURE", "FAN_COMMAND", 
			new FractionalSelectivity(1.0));
		
		// Alarm only triggers for critical temperatures (0.5 selectivity)
		application.addTupleMapping("processor", "TEMPERATURE", "ALARM_COMMAND", 
			new FractionalSelectivity(0.5));
		
		// Define monitoring loops
		final AppLoop loop1 = new AppLoop(new ArrayList<String>(){
			{
				add("TEMPERATURE");
				add("processor");
				add("FAN_CONTROL");
			}
		});
		
		final AppLoop loop2 = new AppLoop(new ArrayList<String>(){
			{
				add("TEMPERATURE");
				add("processor");
				add("ALARM_CONTROL");
			}
		});
		
		List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1); add(loop2);}};
		application.setLoops(loops);
		
		return application;
	}
}

