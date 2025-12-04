# LAB-05: Lower-level Fog Devices with Nearby Gateway Nodes

## Objective
Connect lower-level fog devices with nearby gateway nodes in iFogSim and understand the hierarchy of fog computing infrastructure.

---

## Part A: Hierarchy of Fog Nodes in iFogSim

### Fog Computing Hierarchy (Cloud → Gateway → Edge)

In iFogSim, fog computing infrastructure follows a **hierarchical architecture** with three primary levels:

#### **Level 0: Cloud Node**
- **Position**: Top of the hierarchy
- **Characteristics**: 
  - Highest computational power and storage capacity
  - Centralized resources
  - Highest latency from edge devices
  - Unlimited resources but slowest response time
- **Parent ID**: Set to `-1` (no parent)

#### **Level 1: Gateway Nodes (Proxy/Fog Gateways)**
- **Position**: Middle layer between cloud and edge
- **Characteristics**:
  - Moderate computational resources
  - Acts as aggregation point for multiple edge devices
  - Reduces load on cloud by processing locally when possible
  - Lower latency to edge devices compared to cloud
- **Parent ID**: Cloud node's ID

#### **Level 2: Edge Devices (IoT Devices, Smart Cameras, Mobile Devices)**
- **Position**: Bottom of the hierarchy, closest to end users/sensors
- **Characteristics**:
  - Limited computational resources
  - Lowest latency to sensors and actuators
  - First point of data processing
  - Resource-constrained devices
- **Parent ID**: Nearest gateway node's ID

### Importance of Connecting Lower-Level Fog Devices with Nearby Gateways

Connecting lower-level fog devices to nearby gateway nodes is crucial for several reasons:

1. **Latency Reduction**
   - Direct connection to nearby gateways minimizes communication delay
   - Critical for real-time applications (autonomous vehicles, healthcare monitoring, industrial automation)
   - Avoids routing data all the way to the cloud for every operation

2. **Bandwidth Optimization**
   - Reduces network congestion by processing data locally
   - Prevents unnecessary data transmission to distant cloud servers
   - Aggregates data from multiple edge devices before forwarding to cloud

3. **Energy Efficiency**
   - Shorter communication distances consume less energy
   - Important for battery-powered IoT devices
   - Reduces transmission power requirements

4. **Reliability and Fault Tolerance**
   - Local processing continues even if cloud connection is lost
   - Gateway can cache and forward data when connectivity is restored
   - Distributed architecture improves system resilience

5. **Scalability**
   - Hierarchical structure allows system to scale horizontally
   - New edge devices can be added to existing gateways
   - Load distribution across multiple gateways

6. **Privacy and Security**
   - Sensitive data can be processed locally without sending to cloud
   - Reduces exposure to network-based attacks
   - Enables compliance with data locality regulations

---

## Part B: Steps to Configure Parent-Child Relationships

### Configuration Steps for Fog Node Hierarchy

#### **Step 1: Create the Cloud Node**
```java
FogDevice cloud = createFogDevice("cloud", mips, ram, upBw, downBw, level, ratePerMips, busyPower, idlePower);
cloud.setParentId(-1);  // Cloud has no parent
cloud.setLevel(0);      // Cloud is at level 0
fogDevices.add(cloud);
```

#### **Step 2: Create Gateway Nodes**
```java
FogDevice gateway = createFogDevice("gateway-1", mips, ram, upBw, downBw, level, ratePerMips, busyPower, idlePower);
gateway.setParentId(cloud.getId());  // Set cloud as parent
gateway.setLevel(1);                 // Gateway is at level 1
gateway.setUplinkLatency(100);       // Set latency to parent (cloud)
fogDevices.add(gateway);
```

#### **Step 3: Create Edge Devices and Connect to Gateways**
```java
FogDevice edgeDevice = createFogDevice("edge-1", mips, ram, upBw, downBw, level, ratePerMips, busyPower, idlePower);
edgeDevice.setParentId(gateway.getId());  // Set nearest gateway as parent
edgeDevice.setLevel(2);                   // Edge device is at level 2
edgeDevice.setUplinkLatency(10);          // Set latency to parent (gateway)
fogDevices.add(edgeDevice);
```

#### **Step 4: Establish Parent-Child Latency Mapping**
The parent device maintains a map of its children and their respective latencies:
```java
// This is typically done automatically by the Controller
parent.getChildToLatencyMap().put(child.getId(), latency);
parent.getChildrenIds().add(child.getId());
```

### Key Methods for Configuration

| Method | Purpose | Example |
|--------|---------|---------|
| `setParentId(int id)` | Assigns parent device ID | `edgeDevice.setParentId(gateway.getId())` |
| `setUplinkLatency(double latency)` | Sets communication latency to parent | `gateway.setUplinkLatency(100)` |
| `setLevel(int level)` | Sets hierarchical level | `cloud.setLevel(0)` |
| `getId()` | Gets device ID | `int cloudId = cloud.getId()` |

---

## Part C: Java Code Implementation

### Scenario Configuration
- **Cloud Node** (level 0)
- **Gateway1 and Gateway2** (level 1)
- **Edge Devices**:
  - Edge1 and Edge2 → connect to Gateway1
  - Edge3 → connects to Gateway2

### Complete Java Code Snippet

```java
package org.fog.test.lab05;

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
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;

/**
 * LAB-05: Lower-level Fog Devices with Nearby Gateway Nodes
 * Demonstrates hierarchical fog computing architecture
 */
public class Lab05_ProximityAwareConnectivity {
    
    static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
    
    public static void main(String[] args) {
        Log.printLine("Starting LAB-05: Proximity-Aware Fog Connectivity...");
        
        try {
            // Initialize CloudSim
            int numUsers = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;
            CloudSim.init(numUsers, calendar, traceFlag);
            
            // Create the hierarchical fog infrastructure
            createFogInfrastructure();
            
            // Display the topology
            printTopology();
            
            Log.printLine("\nLAB-05: Fog infrastructure created successfully!");
            
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Error occurred during LAB-05 execution");
        }
    }
    
    /**
     * Creates the hierarchical fog infrastructure:
     * Cloud → Gateway1, Gateway2 → Edge1, Edge2, Edge3
     */
    private static void createFogInfrastructure() {
        // ===== LEVEL 0: CLOUD NODE =====
        FogDevice cloud = createFogDevice(
            "cloud",           // name
            44800,             // MIPS (high computational power)
            40000,             // RAM (40 GB)
            100,               // uplink bandwidth
            10000,             // downlink bandwidth
            0,                 // level 0 (cloud)
            0.01,              // rate per MIPS
            16 * 103,          // busy power
            16 * 83.25         // idle power
        );
        cloud.setParentId(-1);  // Cloud has no parent
        fogDevices.add(cloud);
        
        Log.printLine("Created Cloud Node (Level 0): " + cloud.getName() + 
                     " [ID: " + cloud.getId() + "]");
        
        // ===== LEVEL 1: GATEWAY NODES =====
        
        // Gateway 1
        FogDevice gateway1 = createFogDevice(
            "gateway-1",       // name
            2800,              // MIPS (moderate computational power)
            4000,              // RAM (4 GB)
            10000,             // uplink bandwidth
            10000,             // downlink bandwidth
            1,                 // level 1 (gateway)
            0.0,               // rate per MIPS
            107.339,           // busy power
            83.4333            // idle power
        );
        gateway1.setParentId(cloud.getId());  // Cloud is parent
        gateway1.setUplinkLatency(100);       // 100 ms latency to cloud
        fogDevices.add(gateway1);
        
        Log.printLine("Created Gateway1 (Level 1): " + gateway1.getName() + 
                     " [ID: " + gateway1.getId() + "] → Parent: " + cloud.getName() + 
                     " [Latency: 100 ms]");
        
        // Gateway 2
        FogDevice gateway2 = createFogDevice(
            "gateway-2",       // name
            2800,              // MIPS
            4000,              // RAM (4 GB)
            10000,             // uplink bandwidth
            10000,             // downlink bandwidth
            1,                 // level 1 (gateway)
            0.0,               // rate per MIPS
            107.339,           // busy power
            83.4333            // idle power
        );
        gateway2.setParentId(cloud.getId());  // Cloud is parent
        gateway2.setUplinkLatency(100);       // 100 ms latency to cloud
        fogDevices.add(gateway2);
        
        Log.printLine("Created Gateway2 (Level 1): " + gateway2.getName() + 
                     " [ID: " + gateway2.getId() + "] → Parent: " + cloud.getName() + 
                     " [Latency: 100 ms]");
        
        // ===== LEVEL 2: EDGE DEVICES =====
        
        // Edge Device 1 (connects to Gateway1)
        FogDevice edge1 = createFogDevice(
            "edge-1",          // name
            500,               // MIPS (limited computational power)
            1000,              // RAM (1 GB)
            10000,             // uplink bandwidth
            10000,             // downlink bandwidth
            2,                 // level 2 (edge)
            0,                 // rate per MIPS
            87.53,             // busy power
            82.44              // idle power
        );
        edge1.setParentId(gateway1.getId());  // Gateway1 is parent (nearest gateway)
        edge1.setUplinkLatency(10);           // 10 ms latency to gateway
        fogDevices.add(edge1);
        
        Log.printLine("Created Edge1 (Level 2): " + edge1.getName() + 
                     " [ID: " + edge1.getId() + "] → Parent: " + gateway1.getName() + 
                     " [Latency: 10 ms]");
        
        // Edge Device 2 (connects to Gateway1)
        FogDevice edge2 = createFogDevice(
            "edge-2",          // name
            500,               // MIPS
            1000,              // RAM (1 GB)
            10000,             // uplink bandwidth
            10000,             // downlink bandwidth
            2,                 // level 2 (edge)
            0,                 // rate per MIPS
            87.53,             // busy power
            82.44              // idle power
        );
        edge2.setParentId(gateway1.getId());  // Gateway1 is parent (nearest gateway)
        edge2.setUplinkLatency(10);           // 10 ms latency to gateway
        fogDevices.add(edge2);
        
        Log.printLine("Created Edge2 (Level 2): " + edge2.getName() + 
                     " [ID: " + edge2.getId() + "] → Parent: " + gateway1.getName() + 
                     " [Latency: 10 ms]");
        
        // Edge Device 3 (connects to Gateway2)
        FogDevice edge3 = createFogDevice(
            "edge-3",          // name
            500,               // MIPS
            1000,              // RAM (1 GB)
            10000,             // uplink bandwidth
            10000,             // downlink bandwidth
            2,                 // level 2 (edge)
            0,                 // rate per MIPS
            87.53,             // busy power
            82.44              // idle power
        );
        edge3.setParentId(gateway2.getId());  // Gateway2 is parent (nearest gateway)
        edge3.setUplinkLatency(10);           // 10 ms latency to gateway
        fogDevices.add(edge3);
        
        Log.printLine("Created Edge3 (Level 2): " + edge3.getName() + 
                     " [ID: " + edge3.getId() + "] → Parent: " + gateway2.getName() + 
                     " [Latency: 10 ms]");
        
        // Establish parent-child latency mappings
        connectWithLatencies();
    }
    
    /**
     * Creates a FogDevice with specified characteristics
     */
    private static FogDevice createFogDevice(String nodeName, long mips, int ram, 
            long upBw, long downBw, int level, double ratePerMips, 
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
            costPerStorage, costPerBw
        );
        
        FogDevice fogDevice = null;
        try {
            fogDevice = new FogDevice(nodeName, characteristics, 
                new AppModuleAllocationPolicy(hostList), storageList, 
                10, upBw, downBw, 0, ratePerMips);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        fogDevice.setLevel(level);
        return fogDevice;
    }
    
    /**
     * Establishes parent-child relationships with latency mappings
     */
    private static void connectWithLatencies() {
        for (FogDevice fogDevice : fogDevices) {
            if (fogDevice.getParentId() >= 0) {
                FogDevice parent = null;
                // Find parent device
                for (FogDevice device : fogDevices) {
                    if (device.getId() == fogDevice.getParentId()) {
                        parent = device;
                        break;
                    }
                }
                
                if (parent != null) {
                    double latency = fogDevice.getUplinkLatency();
                    parent.getChildToLatencyMap().put(fogDevice.getId(), latency);
                    parent.getChildrenIds().add(fogDevice.getId());
                }
            }
        }
    }
    
    /**
     * Prints the hierarchical topology
     */
    private static void printTopology() {
        Log.printLine("\n========================================");
        Log.printLine("HIERARCHICAL FOG TOPOLOGY");
        Log.printLine("========================================");
        
        for (FogDevice device : fogDevices) {
            if (device.getLevel() == 0) {
                Log.printLine("\n[LEVEL 0 - CLOUD]");
                printDeviceInfo(device);
                printChildren(device, 1);
            }
        }
        
        Log.printLine("\n========================================");
    }
    
    private static void printDeviceInfo(FogDevice device) {
        String indent = "  ".repeat(device.getLevel());
        Log.printLine(indent + "├─ " + device.getName() + 
                     " [ID: " + device.getId() + 
                     ", Level: " + device.getLevel() + 
                     ", MIPS: " + device.getHost().getTotalMips() + "]");
    }
    
    private static void printChildren(FogDevice parent, int level) {
        for (FogDevice device : fogDevices) {
            if (device.getParentId() == parent.getId()) {
                Log.printLine("  ".repeat(level) + "│");
                printDeviceInfo(device);
                if (level == 1) {
                    Log.printLine("  ".repeat(level) + "│  (Latency: " + 
                                 device.getUplinkLatency() + " ms to " + 
                                 parent.getName() + ")");
                }
                printChildren(device, level + 1);
            }
        }
    }
}
```

### Expected Output
```
Created Cloud Node (Level 0): cloud [ID: xxx]
Created Gateway1 (Level 1): gateway-1 [ID: yyy] → Parent: cloud [Latency: 100 ms]
Created Gateway2 (Level 1): gateway-2 [ID: zzz] → Parent: cloud [Latency: 100 ms]
Created Edge1 (Level 2): edge-1 [ID: aaa] → Parent: gateway-1 [Latency: 10 ms]
Created Edge2 (Level 2): edge-2 [ID: bbb] → Parent: gateway-1 [Latency: 10 ms]
Created Edge3 (Level 2): edge-3 [ID: ccc] → Parent: gateway-2 [Latency: 10 ms]

========================================
HIERARCHICAL FOG TOPOLOGY
========================================

[LEVEL 0 - CLOUD]
  ├─ cloud [ID: xxx, Level: 0, MIPS: 44800.0]
  │
  ├─ gateway-1 [ID: yyy, Level: 1, MIPS: 2800.0]
  │  (Latency: 100 ms to cloud)
    │
    ├─ edge-1 [ID: aaa, Level: 2, MIPS: 500.0]
    ├─ edge-2 [ID: bbb, Level: 2, MIPS: 500.0]
  │
  ├─ gateway-2 [ID: zzz, Level: 1, MIPS: 2800.0]
  │  (Latency: 100 ms to cloud)
    │
    ├─ edge-3 [ID: ccc, Level: 2, MIPS: 500.0]
========================================
```

---

## Part D: Impact of Proximity-Aware Connectivity

### 1. Latency Impact

**Definition**: Latency is the time delay between sending a request and receiving a response.

#### How Proximity-Aware Connectivity Reduces Latency:

| Scenario | Path | Total Latency | Explanation |
|----------|------|---------------|-------------|
| **Without Gateway** (Direct to Cloud) | Edge → Cloud | ~150-200 ms | Long-distance communication to centralized cloud |
| **With Nearby Gateway** | Edge → Gateway | ~10 ms | Short-distance communication to local gateway |
| **Gateway Processing** | Edge → Gateway → Cloud | 10 + 100 = 110 ms | Gateway can process locally or forward if needed |

**Key Benefits**:
- **Local Processing**: Gateway can handle requests without cloud round-trip (90%+ latency reduction)
- **Reduced Hops**: Fewer network hops mean less queuing and transmission delay
- **Geographic Proximity**: Physical distance reduction translates to lower propagation delay

**Real-World Example**:
```
Smart Camera Detection System:
- Edge device detects motion → Gateway processes → Response in 10 ms
- If sent to cloud → 100+ ms delay (unusable for real-time tracking)
```

**Latency Comparison**:
```
Edge to Cloud (without gateway):
  Edge → ISP → Internet → Cloud = 150+ ms

Edge to Nearby Gateway:
  Edge → Local Network → Gateway = 5-15 ms
  
Improvement: 90-95% latency reduction
```

### 2. Energy Consumption Impact

**Definition**: Energy consumption refers to the power used by devices for computation and communication.

#### How Proximity-Aware Connectivity Reduces Energy:

**Communication Energy Model**:
```
Energy = Power × Time
Energy ∝ Distance² × Data_Size

Where:
- Long-distance transmission requires higher transmission power
- More retransmissions due to packet loss in long-distance links
- Edge devices are typically battery-powered (energy-critical)
```

**Energy Comparison**:

| Operation | Energy Cost | Notes |
|-----------|-------------|-------|
| Send to Cloud | **HIGH** (10x-100x) | Long-distance, high power transmission |
| Send to Gateway | **LOW** (1x baseline) | Short-distance, low power transmission |
| Gateway Processing | **MEDIUM** (2x-5x) | Gateway has more power resources |

**Specific Energy Savings**:

1. **Transmission Energy**:
   - Short-range communication (WiFi/Bluetooth to gateway): 0.1-1 mW
   - Long-range communication (cellular to cloud): 100-1000 mW
   - **Savings**: 100-1000x reduction

2. **Idle Power Reduction**:
   - Faster response means device returns to sleep mode sooner
   - Less waiting = less idle power consumption

3. **Data Aggregation**:
   - Gateway aggregates data from multiple edge devices
   - Reduces redundant transmissions
   - Example: 10 sensors send raw data → Gateway sends 1 processed result

**Battery Life Impact**:
```
IoT Sensor with 1000 mAh battery:

Scenario 1 (Cloud-only):
- 100 transmissions/day × 100 mW × 1 second = 2.78 mWh/transmission
- Battery life: ~360 transmissions or 3-4 days

Scenario 2 (Nearby Gateway):
- 100 transmissions/day × 1 mW × 0.1 second = 0.028 mWh/transmission
- Battery life: ~35,714 transmissions or 357 days (1 year)

Energy Savings: 99% reduction, 100x longer battery life
```

### 3. Task Offloading Performance Impact

**Definition**: Task offloading is the process of transferring computational tasks from resource-constrained devices to more powerful devices.

#### How Proximity-Aware Connectivity Improves Task Offloading:

**Offloading Decision Factors**:
```
Offloading_Benefit = (Local_Execution_Time + Local_Energy) - 
                     (Offload_Time + Transmission_Energy + Processing_Time)

Where:
Offload_Time = Upload_Time + Queue_Time + Processing_Time + Download_Time
```

#### Performance Metrics:

| Metric | Cloud Offloading | Gateway Offloading | Improvement |
|--------|------------------|--------------------| ------------|
| **Network Latency** | 100-200 ms | 10-20 ms | **80-90% reduction** |
| **Total Response Time** | 150-300 ms | 20-50 ms | **75-85% faster** |
| **Transmission Energy** | 100-1000 mW | 1-10 mW | **90-99% savings** |
| **Success Rate** | 85-95% | 95-99% | **More reliable** |
| **Scalability** | Limited | High | **Better** |

#### Offloading Scenarios:

**1. Image Processing Task**:
```
Task: Process 1 MB image from smart camera

Cloud Offloading:
- Upload (1 MB @ 10 Mbps): 800 ms
- Processing: 50 ms
- Download (10 KB @ 10 Mbps): 8 ms
- Total: 858 ms
- Energy: ~800 mJ

Gateway Offloading:
- Upload (1 MB @ 100 Mbps): 80 ms
- Processing: 50 ms
- Download (10 KB @ 100 Mbps): 0.8 ms
- Total: 130.8 ms
- Energy: ~80 mJ

Performance: 6.5x faster, 10x less energy
```

**2. Real-Time Decision Making**:
```
IoT Device needs to offload task:

Gateway Available (Proximity-Aware):
✓ Offload to gateway (10 ms latency)
✓ Fast processing and response
✓ Real-time requirements met
✓ Low energy consumption

Only Cloud Available:
✗ High latency (100+ ms)
✗ May miss real-time deadline
✗ High energy consumption
✗ Network congestion possible
→ Device must process locally (limited resources)
```

#### Offloading Strategies Enabled by Proximity:

**1. Partial Offloading**:
- Pre-process data at edge
- Offload intermediate results to gateway
- Gateway performs heavy computation
- Final aggregation at cloud

**2. Dynamic Offloading**:
- Monitor gateway load and latency
- Switch between local processing and offloading based on conditions
- Nearby gateway makes switching feasible (low overhead)

**3. Collaborative Offloading**:
- Multiple edge devices share gateway resources
- Gateway schedules tasks efficiently
- Load balancing across nearby gateways

#### Performance Under Different Loads:

```
Low Load (< 30% gateway utilization):
- Offloading to gateway: Near-optimal performance
- 10-15 ms response time
- High success rate

Medium Load (30-70% gateway utilization):
- Offloading still beneficial
- 20-40 ms response time
- Slight queuing delay

High Load (> 70% gateway utilization):
- Gateway may reject tasks
- Edge device falls back to local processing or cloud
- Proximity allows quick detection and adaptation
```

### Comparative Analysis Summary

| Factor | Direct to Cloud | Proximity-Aware (Gateway) | Improvement |
|--------|----------------|---------------------------|-------------|
| **Average Latency** | 150 ms | 15 ms | **90% reduction** |
| **Energy/Transaction** | 100 mWh | 1 mWh | **99% reduction** |
| **Task Success Rate** | 85% | 98% | **15% improvement** |
| **Network Load** | High | Low | **80% reduction** |
| **Scalability** | Limited | High | **10x more devices** |
| **Reliability** | Moderate | High | **Better fault tolerance** |

### Real-World Application Examples

**1. Smart City Traffic Management**:
- Edge devices (cameras, sensors) detect traffic
- Nearby gateway processes data in real-time
- Immediate traffic light adjustments (< 100 ms)
- Cloud receives aggregated analytics (minutes delay acceptable)

**2. Industrial IoT**:
- Machine sensors monitor equipment
- Gateway detects anomalies in milliseconds
- Prevents machine failures (latency-critical)
- Cloud performs long-term predictive analysis

**3. Healthcare Monitoring**:
- Wearable devices send health data
- Gateway analyzes critical vital signs locally
- Immediate alerts for emergencies (< 50 ms)
- Cloud stores historical data for physician review

### Conclusion

Proximity-aware connectivity through nearby gateways provides:
- **90% latency reduction** for real-time applications
- **99% energy savings** for battery-powered devices
- **6-10x faster** task offloading performance
- **Better scalability** and fault tolerance
- **Optimal balance** between cloud power and edge proximity

This hierarchical architecture is essential for modern IoT and fog computing applications where latency, energy, and performance are critical constraints.

---

## References

1. Bonomi, F., et al. (2012). "Fog computing and its role in the internet of things"
2. iFogSim Documentation: https://github.com/Cloudslab/iFogSim
3. Gupta, H., et al. (2017). "iFogSim: A toolkit for modeling and simulation of resource management techniques in the Internet of Things, Edge and Fog computing environments"

---

## Lab Completion Checklist

- [x] Understand fog computing hierarchy (Cloud → Gateway → Edge)
- [x] Learn importance of proximity-aware connectivity
- [x] Configure parent-child relationships between fog nodes
- [x] Implement latency settings using `setUplinkLatency()`
- [x] Create complete Java code for the scenario
- [x] Analyze impact on latency, energy, and task offloading
- [x] Understand real-world applications

---

**End of LAB-05**

