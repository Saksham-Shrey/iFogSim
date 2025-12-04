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
 * Cloud Datacenter with 2x Data Forwarding
 * For every sensor reading, twice the amount of data is sent to actuator
 */
public class SimpleCloudDatacenter_DoubleData {
	static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	static List<Sensor> sensors = new ArrayList<Sensor>();
	static List<Actuator> actuators = new ArrayList<Actuator>();
	
	public static void main(String[] args) {
		Log.printLine("Starting Cloud Datacenter with 2x Data Forwarding...");

		try {
			Log.disable();
			int numUser = 1;
			Calendar calendar = Calendar.getInstance();
			boolean traceFlag = false;

			CloudSim.init(numUser, calendar, traceFlag);

			String appId = "cloud_app_double_data";
			
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

			Log.printLine("Cloud Datacenter with 2x Data Forwarding finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happened");
		}
	}

	private static void createFogDevices(int userId, String appId) {
		FogDevice cloud = createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25);
		cloud.setParentId(-1);
		fogDevices.add(cloud);
		
		Sensor tempSensor = new Sensor("temp-sensor", "TEMPERATURE", userId, appId, 
			new DeterministicDistribution(10));
		sensors.add(tempSensor);
		tempSensor.setGatewayDeviceId(cloud.getId());
		tempSensor.setLatency(2.0);
		
		Actuator cooler = new Actuator("cooler", userId, appId, "COOLING_CONTROL");
		actuators.add(cooler);
		cooler.setGatewayDeviceId(cloud.getId());
		cooler.setLatency(1.0);
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
	 * Creates application with 2.0 selectivity
	 * For every sensor reading, 2 commands are sent to actuator
	 */
	@SuppressWarnings({"serial"})
	private static Application createApplication(String appId, int userId) {
		Application application = Application.createApplication(appId, userId);
		
		application.addAppModule("processor", 10);
		
		application.addAppEdge("TEMPERATURE", "processor", 1000, 500, "TEMPERATURE", 
			Tuple.UP, AppEdge.SENSOR);
		application.addAppEdge("processor", "COOLING_CONTROL", 500, 500, "COOLING_COMMAND", 
			Tuple.DOWN, AppEdge.ACTUATOR);
		
		// KEY CHANGE: Selectivity is 2.0 (200%)
		// For every temperature reading, 2 cooling commands are sent
		application.addTupleMapping("processor", "TEMPERATURE", "COOLING_COMMAND", 
			new FractionalSelectivity(2.0));
		
		final AppLoop loop1 = new AppLoop(new ArrayList<String>(){
			{
				add("TEMPERATURE");
				add("processor");
				add("COOLING_CONTROL");
			}
		});
		List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);}};
		application.setLoops(loops);
		
		return application;
	}
}

