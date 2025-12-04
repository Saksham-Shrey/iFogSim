# LAB-06: Placement Policies in iFogSim

## a) What is a Placement Policy in iFogSim?

### Definition

A **placement policy** in iFogSim is a strategy or algorithm that determines where application modules should be deployed in a fog computing infrastructure. It decides the mapping between application modules (computational units) and physical fog devices (edge nodes, gateways, cloud servers) in the fog computing hierarchy.

In iFogSim, placement policies are implemented as classes that extend the `ModulePlacement` abstract class or implement specific placement logic interfaces. The core placement policies include:

1. **ModulePlacementOnlyCloud**: Places all modules in the cloud
2. **ModulePlacementEdgewards**: Attempts to place modules as close to edge devices as possible
3. **ModulePlacementMapping**: Uses user-defined custom mappings via `ModuleMapping`

### Why Placement is Critical in Fog/Edge Computing

Module placement is a **critical decision** in fog/edge computing for several fundamental reasons:

#### 1. **Latency Sensitivity**
- IoT applications often have strict real-time requirements (e.g., autonomous vehicles, healthcare monitoring)
- Placing modules closer to data sources (edge) reduces network latency
- Cloud-only placement introduces significant communication delays (100-200ms typical)
- Edge placement can reduce latency to 1-10ms

#### 2. **Bandwidth Constraints**
- Edge devices generate massive amounts of data (video streams, sensor readings)
- Sending all data to the cloud consumes significant bandwidth
- Strategic placement enables data filtering and aggregation at the edge
- Reduces network congestion and backhaul traffic

#### 3. **Energy Efficiency**
- Data transmission is energy-intensive
- Edge processing reduces wireless communication overhead
- Cloud data centers consume substantial power
- Optimal placement balances computation vs. communication energy

#### 4. **Resource Heterogeneity**
- Fog infrastructure has diverse computational capabilities:
  - **Edge devices**: Limited CPU, memory, battery-powered
  - **Gateways**: Moderate resources, always powered
  - **Cloud**: Virtually unlimited resources, high latency
- Placement must match module requirements to device capabilities

#### 5. **Scalability**
- Poor placement leads to bottlenecks (e.g., all processing at cloud)
- Distributed placement enables horizontal scaling
- Edge processing allows the system to handle more IoT devices

#### 6. **Privacy and Security**
- Processing sensitive data at the edge enhances privacy
- Reduces data exposure during network transmission
- Compliance with data locality regulations (GDPR, HIPAA)

#### 7. **Network Reliability**
- Edge processing continues during network outages
- Reduces dependency on cloud connectivity
- Critical for mission-critical applications

---

## b) Comparison of Placement Strategies

### 1. Cloud-Only Placement

**Description**: All application modules are deployed exclusively in the cloud data center.

**Implementation**: `ModulePlacementOnlyCloud` class

**Characteristics**:
- Centralized processing model
- All sensor data transmitted to cloud
- Results sent back to edge actuators
- Traditional cloud computing approach

**Advantages**:
- Unlimited computational resources
- Easy to manage and update
- No resource constraints on modules
- Simplified deployment

**Disadvantages**:
- High latency (100-200ms typical)
- High network bandwidth consumption
- Poor scalability with many IoT devices
- Single point of failure
- Energy-intensive data transmission

**Use Cases**:
- Batch processing applications
- Big data analytics (non-time-critical)
- Applications with minimal data volume
- Scenarios with high-bandwidth, low-latency networks

---

### 2. Edge-Ward Placement

**Description**: Modules are pushed toward edge devices and gateways as much as possible, with the goal of placing computation closer to data sources.

**Implementation**: `ModulePlacementEdgewards` class

**Characteristics**:
- Hierarchical placement strategy
- Modules placed from leaf (edge) to root (cloud)
- Considers resource availability (CPU, RAM, bandwidth)
- Attempts placement at lowest level first
- If resources insufficient, shifts module "northward" (toward cloud)

**Algorithm Overview**:
1. Start from edge devices (leaf nodes)
2. For each module, check if current device has sufficient resources
3. If yes, place module on current device
4. If no, move up the hierarchy and try parent device
5. Continue until module is placed or cloud is reached

**Advantages**:
- Minimized latency (1-10ms for edge processing)
- Reduced network bandwidth consumption
- Better energy efficiency
- Improved privacy (data processed locally)
- Scalable architecture

**Disadvantages**:
- Complex placement logic
- Resource constraints on edge devices
- May lead to suboptimal placements if resources are limited
- Increased management complexity

**Use Cases**:
- Real-time IoT applications
- Video analytics and surveillance
- Healthcare monitoring
- Smart manufacturing
- Autonomous vehicles

---

### 3. Custom Placement

**Description**: User-defined placement policy based on application-specific requirements such as resource availability, latency constraints, energy budgets, or business logic.

**Implementation**: `ModulePlacementMapping` class with `ModuleMapping`, or custom classes extending `ModulePlacement`

**Characteristics**:
- Application-aware placement
- Can incorporate domain knowledge
- Flexible and customizable
- Can consider multiple optimization objectives

**Decision Factors**:
- **Resource availability**: CPU, RAM, storage, battery
- **Latency requirements**: Per-module deadline constraints
- **Energy consumption**: Battery life, power budgets
- **Data privacy**: Regulatory compliance
- **Cost**: Cloud usage fees, bandwidth costs
- **Application topology**: Module dependencies and data flow

**Advantages**:
- Optimized for specific application needs
- Can balance multiple objectives (latency, energy, cost)
- Incorporates domain expertise
- Fine-grained control over placement

**Disadvantages**:
- Requires deep understanding of application and infrastructure
- Implementation complexity
- May not generalize to different scenarios
- Maintenance overhead

**Use Cases**:
- Mission-critical applications with strict SLAs
- Multi-objective optimization scenarios
- Applications with complex module dependencies
- Hybrid cloud-edge deployments

---

### Comparison Table

| Aspect | Cloud-Only | Edge-Ward | Custom |
|--------|-----------|-----------|--------|
| **Latency** | High (100-200ms) | Low (1-10ms) | Variable (optimized) |
| **Bandwidth Usage** | Very High | Low | Optimized |
| **Energy Consumption** | High (transmission) | Low (local processing) | Balanced |
| **Resource Requirements** | Minimal (edge) | Moderate (edge) | Application-specific |
| **Scalability** | Limited | High | High |
| **Complexity** | Simple | Moderate | High |
| **Privacy** | Low | High | Configurable |
| **Reliability** | Centralized risk | Distributed | Configurable |

---

## c) Healthcare Application Placement Policy Design

### Application Overview

**IoT Healthcare Application with Three Modules**:

1. **Sensor Data Processing** (Lightweight)
   - **Function**: Raw data collection, filtering, noise reduction
   - **Characteristics**: Low CPU, low memory, high frequency
   - **Data flow**: Receives from sensors (heart rate, blood pressure, temperature)
   - **Processing**: Basic signal processing, outlier detection

2. **Feature Extraction** (Medium Load)
   - **Function**: Pattern recognition, data aggregation, feature computation
   - **Characteristics**: Moderate CPU, moderate memory
   - **Data flow**: Receives from Sensor Data Processing
   - **Processing**: Statistical analysis, trend detection, windowing

3. **Deep Analytics** (Heavy Load)
   - **Function**: Machine learning inference, diagnosis, predictions
   - **Characteristics**: High CPU, high memory, GPU acceleration
   - **Data flow**: Receives from Feature Extraction
   - **Processing**: Neural network inference, complex pattern analysis

---

### Placement Strategy Design

#### **Tier 1: Edge Nodes (Wearables, IoT Devices)**
- **Place**: Sensor Data Processing
- **Rationale**:
  - Minimizes data transmission (filters noise before sending)
  - Low latency for immediate alerts
  - Reduces bandwidth (e.g., 1000 samples → 10 events)
  - Battery-efficient (transmitting less data)
  - Privacy-preserving (raw biometric data stays local)

#### **Tier 2: Gateway Nodes (Home/Hospital Gateways)**
- **Place**: Feature Extraction
- **Rationale**:
  - Aggregates data from multiple edge devices
  - Sufficient resources for moderate computation
  - Reduced latency compared to cloud (5-20ms)
  - Can serve multiple patients/devices
  - Enables local alerting for critical conditions

#### **Tier 3: Cloud Data Center**
- **Place**: Deep Analytics
- **Rationale**:
  - Requires significant computational resources
  - Machine learning models are large and complex
  - Accesses historical data and large databases
  - Updates models with new training data
  - Non-time-critical (minutes acceptable)
  - Benefits from GPU acceleration

---

### Data Flow and Processing Pipeline

```
[Sensors (ECG, BP, Temp, etc.)]
           |
           | Raw data (high frequency, high volume)
           v
    [Edge Node: Sensor Data Processing]
           |
           | Filtered data, events (reduced volume)
           v
    [Gateway: Feature Extraction]
           |
           | Features, aggregated metrics (low volume)
           v
    [Cloud: Deep Analytics]
           |
           | Diagnosis, predictions, alerts
           v
    [Healthcare Provider Dashboard]
```

---

### Latency Analysis

| Module | Location | Latency | Justification |
|--------|----------|---------|---------------|
| Sensor Data Processing | Edge | <1ms | Immediate processing |
| Feature Extraction | Gateway | 5-20ms | Local network |
| Deep Analytics | Cloud | 100-500ms | Non-critical, complex |
| **Total End-to-End** | - | **~520ms** | Acceptable for monitoring |

---

### Resource Requirements

| Module | CPU (MIPS) | RAM (MB) | Bandwidth (KB/s) |
|--------|-----------|----------|------------------|
| Sensor Data Processing | 500 | 128 | 50 (input), 5 (output) |
| Feature Extraction | 2000 | 512 | 5 (input), 1 (output) |
| Deep Analytics | 10000 | 4096 | 1 (input), 0.1 (output) |

---

## d) Java Implementation: Custom Healthcare Placement Policy

### HealthcarePlacementPolicy.java

```java
package org.fog.placement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.FogDevice;

/**
 * Custom placement policy for IoT Healthcare Application
 * 
 * Placement Strategy:
 * - Sensor Data Processing → Edge Nodes (level 3)
 * - Feature Extraction → Gateway Nodes (level 2)
 * - Deep Analytics → Cloud (level 0)
 * 
 * @author Saksham Shrey
 */
public class HealthcarePlacementPolicy extends ModulePlacement {
    
    // Module names
    private static final String SENSOR_DATA_PROCESSING = "sensor_data_processing";
    private static final String FEATURE_EXTRACTION = "feature_extraction";
    private static final String DEEP_ANALYTICS = "deep_analytics";
    
    // Device level definitions
    private static final int CLOUD_LEVEL = 0;
    private static final int PROXY_LEVEL = 1;
    private static final int GATEWAY_LEVEL = 2;
    private static final int EDGE_LEVEL = 3;
    
    /**
     * Constructor for Healthcare Placement Policy
     * 
     * @param fogDevices List of all fog devices in the infrastructure
     * @param application The healthcare application to be deployed
     */
    public HealthcarePlacementPolicy(List<FogDevice> fogDevices, Application application) {
        this.setFogDevices(fogDevices);
        this.setApplication(application);
        this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
        this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
        this.setModuleInstanceCountMap(new HashMap<Integer, Map<String, Integer>>());
        
        // Initialize module instance count map for all devices
        for (FogDevice device : fogDevices) {
            getModuleInstanceCountMap().put(device.getId(), new HashMap<String, Integer>());
        }
        
        // Perform the module placement
        mapModules();
    }
    
    /**
     * Main placement logic implementing the healthcare-specific strategy
     */
    @Override
    protected void mapModules() {
        System.out.println("========================================");
        System.out.println("Healthcare Placement Policy - Starting Module Placement");
        System.out.println("========================================");
        
        // Place each module according to the healthcare placement strategy
        for (AppModule module : getApplication().getModules()) {
            placeModule(module);
        }
        
        System.out.println("========================================");
        System.out.println("Healthcare Placement Policy - Placement Complete");
        System.out.println("========================================");
    }
    
    /**
     * Place a single module based on its type and resource requirements
     * 
     * @param module The application module to be placed
     */
    private void placeModule(AppModule module) {
        String moduleName = module.getName();
        
        switch (moduleName) {
            case SENSOR_DATA_PROCESSING:
                placeSensorDataProcessing(module);
                break;
            case FEATURE_EXTRACTION:
                placeFeatureExtraction(module);
                break;
            case DEEP_ANALYTICS:
                placeDeepAnalytics(module);
                break;
            default:
                // For any other modules, use default edge-ward placement
                placeModuleEdgeward(module);
                break;
        }
    }
    
    /**
     * Place Sensor Data Processing module on Edge devices (level 3)
     * This is lightweight processing that should happen closest to sensors
     * 
     * @param module The sensor data processing module
     */
    private void placeSensorDataProcessing(AppModule module) {
        System.out.println("\n[PLACEMENT] Module: " + module.getName());
        System.out.println("  Strategy: Place on EDGE devices (level " + EDGE_LEVEL + ")");
        System.out.println("  Rationale: Lightweight, low latency, privacy-preserving");
        
        List<FogDevice> edgeDevices = getDevicesByLevel(EDGE_LEVEL);
        
        if (edgeDevices.isEmpty()) {
            System.err.println("  WARNING: No edge devices found, trying gateway level");
            edgeDevices = getDevicesByLevel(GATEWAY_LEVEL);
        }
        
        // Place module on all edge devices (one instance per device)
        for (FogDevice device : edgeDevices) {
            if (createModuleInstanceOnDevice(module, device)) {
                System.out.println("  ✓ Placed on: " + device.getName() + 
                                 " (MIPS: " + device.getHost().getTotalMips() + ")");
                getModuleInstanceCountMap().get(device.getId()).put(module.getName(), 1);
            }
        }
    }
    
    /**
     * Place Feature Extraction module on Gateway devices (level 2)
     * This is moderate processing that aggregates data from multiple edge devices
     * 
     * @param module The feature extraction module
     */
    private void placeFeatureExtraction(AppModule module) {
        System.out.println("\n[PLACEMENT] Module: " + module.getName());
        System.out.println("  Strategy: Place on GATEWAY devices (level " + GATEWAY_LEVEL + ")");
        System.out.println("  Rationale: Moderate load, aggregates multiple edge streams");
        
        List<FogDevice> gatewayDevices = getDevicesByLevel(GATEWAY_LEVEL);
        
        if (gatewayDevices.isEmpty()) {
            System.err.println("  WARNING: No gateway devices found, trying proxy level");
            gatewayDevices = getDevicesByLevel(PROXY_LEVEL);
        }
        
        // Place module on all gateway devices
        for (FogDevice device : gatewayDevices) {
            if (hasSufficientResources(device, module)) {
                if (createModuleInstanceOnDevice(module, device)) {
                    System.out.println("  ✓ Placed on: " + device.getName() + 
                                     " (MIPS: " + device.getHost().getTotalMips() + ")");
                    getModuleInstanceCountMap().get(device.getId()).put(module.getName(), 1);
                }
            } else {
                System.err.println("  ✗ Insufficient resources on: " + device.getName());
            }
        }
    }
    
    /**
     * Place Deep Analytics module on Cloud (level 0)
     * This is heavy processing requiring significant computational resources
     * 
     * @param module The deep analytics module
     */
    private void placeDeepAnalytics(AppModule module) {
        System.out.println("\n[PLACEMENT] Module: " + module.getName());
        System.out.println("  Strategy: Place on CLOUD (level " + CLOUD_LEVEL + ")");
        System.out.println("  Rationale: Heavy load, ML inference, unlimited resources");
        
        FogDevice cloud = getCloudDevice();
        
        if (cloud != null) {
            if (createModuleInstanceOnDevice(module, cloud)) {
                System.out.println("  ✓ Placed on: " + cloud.getName() + 
                                 " (MIPS: " + cloud.getHost().getTotalMips() + ")");
                
                // Calculate required instances based on edge devices
                int instanceCount = calculateRequiredInstances(module);
                getModuleInstanceCountMap().get(cloud.getId()).put(module.getName(), instanceCount);
                System.out.println("  Instances: " + instanceCount);
            }
        } else {
            System.err.println("  ERROR: Cloud device not found!");
        }
    }
    
    /**
     * Default edge-ward placement for unrecognized modules
     * Tries to place modules as close to edge as possible
     * 
     * @param module The module to be placed
     */
    private void placeModuleEdgeward(AppModule module) {
        System.out.println("\n[PLACEMENT] Module: " + module.getName());
        System.out.println("  Strategy: Default EDGE-WARD placement");
        
        // Try placement from edge to cloud
        for (int level = EDGE_LEVEL; level >= CLOUD_LEVEL; level--) {
            List<FogDevice> devices = getDevicesByLevel(level);
            
            for (FogDevice device : devices) {
                if (hasSufficientResources(device, module)) {
                    if (createModuleInstanceOnDevice(module, device)) {
                        System.out.println("  ✓ Placed on: " + device.getName() + 
                                         " (level " + level + ")");
                        getModuleInstanceCountMap().get(device.getId()).put(module.getName(), 1);
                        return;
                    }
                }
            }
        }
        
        System.err.println("  ERROR: Could not place module anywhere!");
    }
    
    /**
     * Get all fog devices at a specific hierarchy level
     * 
     * @param level The hierarchy level (0=cloud, 1=proxy, 2=gateway, 3=edge)
     * @return List of fog devices at the specified level
     */
    private List<FogDevice> getDevicesByLevel(int level) {
        List<FogDevice> devices = new ArrayList<>();
        
        for (FogDevice device : getFogDevices()) {
            if (device.getLevel() == level) {
                devices.add(device);
            }
        }
        
        return devices;
    }
    
    /**
     * Get the cloud device (level 0)
     * 
     * @return The cloud fog device, or null if not found
     */
    private FogDevice getCloudDevice() {
        for (FogDevice device : getFogDevices()) {
            if (device.getName().equalsIgnoreCase("cloud") || device.getLevel() == CLOUD_LEVEL) {
                return device;
            }
        }
        return null;
    }
    
    /**
     * Check if a fog device has sufficient resources to host a module
     * 
     * @param device The fog device
     * @param module The application module
     * @return true if device has sufficient resources, false otherwise
     */
    private boolean hasSufficientResources(FogDevice device, AppModule module) {
        // Check CPU availability
        double requiredMips = module.getMips();
        double availableMips = device.getHost().getTotalMips();
        
        // Check RAM availability
        int requiredRam = module.getRam();
        int availableRam = device.getHost().getRam();
        
        return (requiredMips <= availableMips) && (requiredRam <= availableRam);
    }
    
    /**
     * Calculate the required number of module instances based on workload
     * For cloud modules, this estimates based on the number of edge devices
     * 
     * @param module The module for which to calculate instances
     * @return The number of required instances
     */
    private int calculateRequiredInstances(AppModule module) {
        // For cloud-based analytics, estimate based on edge device count
        int edgeDeviceCount = getDevicesByLevel(EDGE_LEVEL).size();
        int gatewayDeviceCount = getDevicesByLevel(GATEWAY_LEVEL).size();
        
        // Simple heuristic: 1 instance per 5 edge devices or 1 per gateway
        int instanceCount = Math.max(1, Math.max(
            edgeDeviceCount / 5,
            gatewayDeviceCount
        ));
        
        return instanceCount;
    }
}
```

---

### HealthcareApplication.java (Test/Demo Application)

```java
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
```

---

## e) Impact of Placement Policies on Performance Metrics

### 1. Latency (End-to-End Response Time)

#### Cloud-Only Placement
- **Latency**: 200-500ms per request
- **Components**:
  - Sensor → Cloud: 100-150ms (WAN latency)
  - Processing: 10-50ms
  - Cloud → Actuator: 100-150ms
- **Issues**:
  - Unacceptable for real-time applications (autonomous vehicles, emergency response)
  - Jitter due to network congestion
  - Multi-hop delays accumulate

#### Edge-Ward Placement
- **Latency**: 5-50ms per request
- **Components**:
  - Sensor → Edge: 1-2ms (local)
  - Edge processing: 1-5ms
  - Edge → Gateway: 2-5ms
  - Gateway → Actuator: 1-2ms
- **Benefits**:
  - 10-100x latency reduction
  - Suitable for time-critical IoT applications
  - Predictable, low jitter
  - Local loops avoid WAN traversal

#### Custom Placement (Healthcare Example)
- **Latency**: Optimized per module
  - Edge (sensor processing): 1-2ms
  - Gateway (feature extraction): 5-10ms
  - Cloud (analytics): 100-200ms (acceptable for non-critical tasks)
- **Total latency for critical path**: ~15ms (edge+gateway)
- **Total latency for full analytics**: ~215ms

**Key Insight**: Placement policy can reduce latency by 10-100x for time-sensitive operations while using cloud for heavy, non-time-critical tasks.

---

### 2. Network Load (Uplink/Downlink Usage)

#### Cloud-Only Placement
- **Uplink usage**: VERY HIGH
  - All sensor data transmitted to cloud
  - Example: 1000 IoT devices × 10 KB/s = 10 MB/s continuous uplink
  - Video streams: 1000 cameras × 1 Mbps = 1 Gbps uplink
- **Downlink usage**: HIGH
  - Results sent back to all actuators
  - Unnecessary data movement
- **Issues**:
  - Network congestion
  - High bandwidth costs
  - Scalability bottleneck

#### Edge-Ward Placement
- **Uplink usage**: LOW (90-99% reduction)
  - Data filtered and aggregated at edge
  - Only events/alerts sent to cloud
  - Example: 1000 IoT devices → 10-100 KB/s aggregate uplink
- **Downlink usage**: LOW
  - Most control loops closed locally
  - Only configuration updates from cloud
- **Benefits**:
  - 10-100x bandwidth reduction
  - Reduced network costs
  - Better scalability

#### Custom Placement (Healthcare Example)
- **Edge → Gateway**: 50 KB/s (filtered data)
- **Gateway → Cloud**: 5 KB/s (features only)
- **Data reduction**: 1000 KB/s (raw sensors) → 5 KB/s (to cloud) = 99.5% reduction
- **Cost savings**: Significant reduction in cloud ingress/egress fees

**Key Insight**: Edge placement reduces network load by 10-100x, critical for bandwidth-constrained environments (cellular, satellite).

---

### 3. Energy Consumption (Cloud vs. Edge Execution)

#### Cloud-Only Placement
- **Energy components**:
  - **Wireless transmission**: 100-1000 mW per device (dominant cost for IoT)
  - **Cloud computation**: Amortized across many users (efficient per operation)
  - **Edge idle power**: Minimal (no processing)
- **Total energy**: HIGH due to continuous data transmission
- **Battery life**: 1-7 days for battery-powered IoT devices

#### Edge-Ward Placement
- **Energy components**:
  - **Wireless transmission**: 1-10 mW per device (only events sent)
  - **Edge computation**: 10-100 mW (local processing)
  - **Cloud computation**: Minimal (only for heavy tasks)
- **Total energy**: LOW due to reduced transmission
- **Battery life**: 30-365 days for battery-powered IoT devices

#### Energy Comparison (Example: Smart Camera)
| Operation | Energy Cost | Cloud-Only | Edge-Ward |
|-----------|-------------|------------|-----------|
| Capture frame (1 MB) | - | 1 mJ | 1 mJ |
| Transmit 1 MB (WiFi) | 3 J/MB | 3000 mJ | 0 mJ |
| Process frame (edge) | 0.1 J | 0 mJ | 100 mJ |
| Transmit event (1 KB) | 3 mJ/KB | 0 mJ | 3 mJ |
| **Total per frame** | - | **3001 mJ** | **104 mJ** |
| **Energy savings** | - | - | **97% reduction** |

#### Custom Placement (Healthcare Example)
- **Edge processing**: 50 mW (sensor data filtering)
- **Gateway processing**: 200 mW (feature extraction, shared across patients)
- **Wireless transmission**: 5 mW average (events only)
- **Battery life estimate**: 60-180 days (vs. 3-7 days for cloud-only)

**Key Insight**: Edge placement can extend battery life by 10-100x, critical for wearable devices and remote sensors.

---

### 4. Scalability (Handling Large IoT Deployments)

#### Cloud-Only Placement
- **Scalability limitations**:
  - Cloud resources are elastic, but network is bottleneck
  - WAN bandwidth fixed (e.g., 10 Gbps uplink)
  - Network latency increases with congestion
  - Single point of failure (cloud connectivity)
- **Max device capacity**: 1,000-10,000 devices (limited by network)
- **Scaling approach**: Vertical (bigger cloud) + network upgrades (expensive)

#### Edge-Ward Placement
- **Scalability advantages**:
  - Distributed processing (no central bottleneck)
  - Network load constant per edge-cloud path
  - Linear scaling with infrastructure
  - Fault isolation (edge failures don't affect others)
- **Max device capacity**: 100,000-1,000,000 devices (limited by fog nodes)
- **Scaling approach**: Horizontal (add more edge/gateway nodes) - cost-effective

#### Custom Placement (Healthcare Example)
- **Scaling strategy**:
  - Edge nodes: One per patient (10-100 patients per gateway)
  - Gateway nodes: One per hospital ward (10-50 wards per hospital)
  - Cloud: Single instance serves entire hospital system
- **Capacity**: 10,000+ patients per cloud instance
- **Cost efficiency**: $10-50 per patient (vs. $100-500 for cloud-only)

#### Scalability Comparison Table

| Metric | Cloud-Only | Edge-Ward | Custom |
|--------|-----------|-----------|--------|
| **Max devices** | 1K-10K | 100K-1M | 10K-100K |
| **Network bottleneck** | Severe | Minimal | Moderate |
| **Cost per device** | $50-200 | $10-50 | $10-100 |
| **Failure impact** | Global outage | Local outage | Tiered impact |
| **Management complexity** | Low | High | Moderate |

**Key Insight**: Edge-ward placement enables 10-100x better scalability through distributed processing and reduced network dependency.

---

### Comprehensive Performance Comparison

| Performance Aspect | Cloud-Only | Edge-Ward | Custom (Healthcare) |
|-------------------|------------|-----------|---------------------|
| **Latency** | 200-500ms ❌ | 5-50ms ✅ | 15ms (critical) ✅ |
| **Network Load** | 10 MB/s ❌ | 100 KB/s ✅ | 5 KB/s ✅ |
| **Energy (per device)** | 3000 mJ ❌ | 100 mJ ✅ | 50-200 mJ ✅ |
| **Battery Life** | 1-7 days ❌ | 30-365 days ✅ | 60-180 days ✅ |
| **Max Devices** | 1K-10K ❌ | 100K-1M ✅ | 10K-100K ✅ |
| **Cost per Device** | $50-200 ❌ | $10-50 ✅ | $10-100 ✅ |
| **Privacy** | Low ❌ | High ✅ | Configurable ✅ |
| **Reliability** | Single point ❌ | Distributed ✅ | Hybrid ✅ |
| **Implementation** | Simple ✅ | Complex ❌ | Moderate ⚠️ |

---

## Conclusion

Placement policies are a **critical design decision** in fog/edge computing that fundamentally impact application performance, cost, and scalability. The choice of placement strategy depends on:

1. **Application requirements**: Latency, throughput, reliability
2. **Resource constraints**: Device capabilities, energy budgets
3. **Network conditions**: Bandwidth, connectivity, cost
4. **Deployment scale**: Number of devices, geographic distribution

**Recommendations**:
- **Real-time IoT**: Use Edge-Ward or Custom placement
- **Batch analytics**: Cloud-Only placement acceptable
- **Healthcare, autonomous systems**: Custom placement with tiered processing
- **Smart cities, large-scale deployments**: Edge-Ward with load balancing

The **custom healthcare placement policy** demonstrates how application-specific knowledge can be leveraged to optimize multiple objectives simultaneously—achieving low latency for critical operations, minimizing bandwidth costs, extending battery life, and maintaining scalability.

---

## References

1. iFogSim2 Documentation and Source Code
2. Gupta, H., et al. "iFogSim: A toolkit for modeling and simulation of resource management techniques in the Internet of Things, Edge and Fog computing environments." Software: Practice and Experience (2017).
3. Bonomi, F., et al. "Fog computing and its role in the internet of things." MCC workshop on Mobile cloud computing (2012).
4. Module Placement Classes in iFogSim:
   - `org.fog.placement.ModulePlacement`
   - `org.fog.placement.ModulePlacementOnlyCloud`
   - `org.fog.placement.ModulePlacementEdgewards`
   - `org.fog.placement.ModulePlacementMapping`

---

**Author**: Saksham Shrey  
**Course**: LAB-06: Edge Computing  
**Date**: December 2025  

