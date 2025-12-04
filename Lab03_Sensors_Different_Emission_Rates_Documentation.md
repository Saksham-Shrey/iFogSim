# Lab 03: Sensors with Different Tuple Emission Rates in iFogSim

**Course:** Edge Computing  
**Topic:** Designing and Implementing Sensors with Different Tuple Emission Rates  
**Framework:** iFogSim2

---

## a) Role of Sensors in iFogSim and Impact of Tuple Emission Rate on Performance

### Role of Sensors in iFogSim

Sensors in iFogSim are **data generation entities** that simulate real-world IoT devices. They serve several critical functions:

1. **Data Source Simulation**: Sensors represent physical IoT devices (temperature sensors, cameras, motion detectors, etc.) that generate data tuples in the fog computing environment.

2. **Tuple Generation**: Sensors create and emit tuples (data packets) at specified rates, which are then processed by application modules deployed on fog nodes.

3. **Gateway Attachment**: Each sensor is attached to a specific fog device (gateway), simulating the physical connection between IoT devices and edge infrastructure.

4. **Application Input**: Sensors serve as entry points for application workflows, initiating the data processing pipeline from edge to cloud.

5. **Workload Generation**: They control the workload intensity by determining how frequently data is generated and sent into the system.

### Impact of Tuple Emission Rate on Application Performance

The tuple emission rate significantly affects multiple performance metrics:

#### 1. **Latency**

- **Higher emission rates** (shorter intervals) → More tuples competing for processing resources → Potential queue buildup → **Increased latency**
- **Lower emission rates** (longer intervals) → Less congestion → Tuples processed more quickly → **Reduced latency**
- **Variable rates** (exponential distribution) → Latency varies, with occasional bursts causing temporary spikes

#### 2. **Throughput**

- **Higher emission rates** → More data processed per unit time → **Higher throughput** (until saturation)
- At saturation point, throughput plateaus as resources are fully utilized
- **Network bandwidth** becomes a limiting factor with high emission rates
- Deterministic rates provide **predictable throughput**; exponential rates create **variable throughput**

#### 3. **Energy Consumption**

- **Higher emission rates** → More data transmission and processing → **Increased energy consumption**
- Edge devices remain in active state longer → Higher busy power consumption
- Frequent tuple emission prevents devices from entering low-power idle states
- **Trade-off**: Higher data freshness vs. energy efficiency

#### 4. **Network Congestion**

- Multiple sensors with high emission rates → Network congestion → Packet drops and retransmissions
- Bandwidth saturation affects all sensors sharing the same network link

#### 5. **Resource Utilization**

- CPU, memory, and bandwidth utilization increase proportionally with emission rates
- Over-provisioned emission rates can lead to resource starvation for other applications

#### 6. **Data Freshness vs. System Load**

- Frequent emissions ensure fresh data but increase system load
- Application requirements dictate optimal emission rates (e.g., critical health monitoring needs faster rates than environmental sensing)

---

## b) Steps to Configure Sensors with Different Distribution Types

### Deterministic Distribution Configuration

**Deterministic Distribution** emits tuples at **fixed, regular intervals**. The inter-transmission time is constant.

**Step-by-Step Configuration:**

1. **Import Required Classes**

```java
import org.fog.entities.Sensor;
import org.fog.utils.distribution.DeterministicDistribution;
```

2. **Create DeterministicDistribution Object**

```java
// Create distribution with 5 ms inter-transmission time
DeterministicDistribution distribution = new DeterministicDistribution(5);
```

3. **Create Sensor with Deterministic Distribution**

```java
Sensor sensor = new Sensor(
    "sensor-name",              // Unique sensor identifier
    "TUPLE_TYPE",               // Type of tuples emitted (e.g., "TEMPERATURE_DATA")
    userId,                     // User ID from broker
    appId,                      // Application ID
    new DeterministicDistribution(5)  // Emit every 5 ms
);
```

4. **Attach Sensor to Edge Device**

```java
sensor.setGatewayDeviceId(edgeDevice.getId());  // Connect to specific fog device
sensor.setLatency(1.0);                          // Set latency (ms) between sensor and gateway
```

5. **Add to Sensor List**

```java
sensors.add(sensor);  // Add to global sensor list for controller
```

**Characteristics:**

- Predictable, periodic data generation
- Inter-transmission time = exactly `meanMs` milliseconds
- Suitable for: Regular monitoring applications (periodic temperature readings, scheduled data collection)

---

### Exponential Distribution Configuration

**Exponential Distribution** emits tuples at **random intervals** following an exponential distribution with a specified mean. This models Poisson arrival processes common in real-world scenarios.

**Step-by-Step Configuration:**

1. **Import Required Classes**

```java
import org.fog.entities.Sensor;
import org.fog.utils.distribution.ExponentialDistribution;
```

2. **Create ExponentialDistribution Object**

```java
// Create distribution with mean 10 ms inter-transmission time
ExponentialDistribution distribution = new ExponentialDistribution(10);
```

3. **Create Sensor with Exponential Distribution**

```java
Sensor sensor = new Sensor(
    "sensor-name",              // Unique sensor identifier
    "TUPLE_TYPE",               // Type of tuples emitted (e.g., "MOTION_DATA")
    userId,                     // User ID from broker
    appId,                      // Application ID
    new ExponentialDistribution(10)  // Mean interval = 10 ms
);
```

4. **Attach Sensor to Edge Device**

```java
sensor.setGatewayDeviceId(edgeDevice.getId());  // Connect to specific fog device
sensor.setLatency(1.0);                          // Set latency (ms) between sensor and gateway
```

5. **Add to Sensor List**

```java
sensors.add(sensor);  // Add to global sensor list for controller
```

**Characteristics:**

- Random, variable inter-transmission times
- Average inter-transmission time = `meanMs` milliseconds
- Can produce bursts (short intervals) and gaps (long intervals)
- Suitable for: Event-driven applications (motion detection, anomaly detection, sporadic sensor readings)

**Mathematical Property:**

- Probability density: f(x) = (1/λ) \* e^(-x/λ) where λ is the mean
- Memoryless property: Past emissions don't affect future emission timing

---

## c) Java Code Snippet for Three Sensor Types

Below is the complete Java code demonstrating the configuration of three different sensors:

### Temperature Sensor (Deterministic, 5 ms)

```java
// Temperature Sensor - Emits data exactly every 5 milliseconds
Sensor temperatureSensor = new Sensor(
    "temperature-sensor-0",              // Sensor name
    "TEMPERATURE_DATA",                  // Tuple type emitted
    userId,                              // User ID from broker
    appId,                               // Application ID
    new DeterministicDistribution(5)     // Deterministic: 5 ms interval
);
temperatureSensor.setGatewayDeviceId(edgeDevice.getId()); // Attach to edge device
temperatureSensor.setLatency(1.0);                        // 1 ms sensor-to-gateway latency
sensors.add(temperatureSensor);                           // Add to sensor list
```

**Characteristics:**

- **Emission Rate**: Exactly every 5 ms
- **Throughput**: 200 tuples/second (1000 ms / 5 ms)
- **Behavior**: Highly predictable, periodic data generation
- **Use Case**: Regular temperature monitoring in smart buildings

---

### Heartbeat Sensor (Deterministic, 2 ms)

```java
// Heartbeat Sensor - Emits data exactly every 2 milliseconds (high frequency)
Sensor heartbeatSensor = new Sensor(
    "heartbeat-sensor-0",                // Sensor name
    "HEARTBEAT_DATA",                    // Tuple type emitted
    userId,                              // User ID from broker
    appId,                               // Application ID
    new DeterministicDistribution(2)     // Deterministic: 2 ms interval
);
heartbeatSensor.setGatewayDeviceId(edgeDevice.getId()); // Attach to same edge device
heartbeatSensor.setLatency(1.0);                        // 1 ms sensor-to-gateway latency
sensors.add(heartbeatSensor);                           // Add to sensor list
```

**Characteristics:**

- **Emission Rate**: Exactly every 2 ms
- **Throughput**: 500 tuples/second (1000 ms / 2 ms)
- **Behavior**: High-frequency, time-critical data generation
- **Use Case**: Real-time health monitoring, ECG data collection

---

### Motion Sensor (Exponential Distribution, Mean 10 ms)

```java
// Motion Sensor - Emits data with random intervals following exponential distribution
Sensor motionSensor = new Sensor(
    "motion-sensor-0",                   // Sensor name
    "MOTION_DATA",                       // Tuple type emitted
    userId,                              // User ID from broker
    appId,                               // Application ID
    new ExponentialDistribution(10)      // Exponential: mean 10 ms interval
);
motionSensor.setGatewayDeviceId(edgeDevice.getId()); // Attach to same edge device
motionSensor.setLatency(1.0);                        // 1 ms sensor-to-gateway latency
sensors.add(motionSensor);                           // Add to sensor list
```

**Characteristics:**

- **Emission Rate**: Variable intervals, average 10 ms
- **Average Throughput**: ~100 tuples/second (1000 ms / 10 ms)
- **Behavior**: Unpredictable, bursty data generation with occasional gaps
- **Use Case**: Motion detection, event-based monitoring, sporadic activity sensing

---

### Creating and Attaching an Actuator

```java
// Alert Actuator - Connected to the edge device for control actions
Actuator alertActuator = new Actuator(
    "alert-actuator-0",     // Actuator name
    userId,                  // User ID from broker
    appId,                   // Application ID
    "ALERT_CONTROL"         // Actuator type (matches application edge definition)
);
alertActuator.setGatewayDeviceId(edgeDevice.getId()); // Attach to edge device
alertActuator.setLatency(1.0);                         // 1 ms actuator-to-gateway latency
actuators.add(alertActuator);                          // Add to actuator list
```

**Purpose**: The actuator receives processed results and triggers control actions (e.g., sending alerts, activating devices).

---

### Edge Device Creation Example

```java
// Create an edge fog device where sensors and actuators will be attached
FogDevice edgeDevice = createFogDevice(
    "edge-0",    // Device name
    1000,        // MIPS (processing power)
    2000,        // RAM (MB)
    10000,       // Uplink bandwidth (Mbps)
    10000,       // Downlink bandwidth (Mbps)
    2,           // Hierarchy level (2 = edge tier)
    0.0,         // Rate per MIPS
    90.0,        // Busy power (watts)
    80.0         // Idle power (watts)
);
edgeDevice.setParentId(proxyServer.getId()); // Connect to parent proxy server
edgeDevice.setUplinkLatency(4);              // 4 ms latency to parent
fogDevices.add(edgeDevice);                  // Add to fog device list
```

---

## d) Impact of Varying Tuple Emission Rates on Latency, Throughput, and Energy Consumption

### Comparative Analysis of the Three Sensors

| **Metric**         | **Temperature (5ms)** | **Heartbeat (2ms)** | **Motion (Exp 10ms)** |
| ------------------ | --------------------- | ------------------- | --------------------- |
| **Emission Type**  | Deterministic         | Deterministic       | Exponential           |
| **Interval**       | Fixed 5 ms            | Fixed 2 ms          | Variable, mean 10 ms  |
| **Throughput**     | 200 tuples/sec        | 500 tuples/sec      | ~100 tuples/sec (avg) |
| **Predictability** | High                  | High                | Low (bursty)          |

---

### 1. Latency Analysis

#### End-to-End Latency Components:

- **Transmission delay**: Sensor → Gateway
- **Queueing delay**: Waiting for processing at fog nodes
- **Processing delay**: Computation time at modules
- **Propagation delay**: Network transmission between fog tiers

#### Impact of Different Emission Rates:

**Temperature Sensor (5 ms, Deterministic):**

- **Moderate latency**: 200 tuples/sec creates moderate load
- **Predictable queueing**: Regular arrivals lead to consistent queue lengths
- **Expected latency**: Low to moderate, depending on processing capacity
- **Suitable for**: Applications tolerating 10-50 ms latency

**Heartbeat Sensor (2 ms, Deterministic):**

- **Higher latency risk**: 500 tuples/sec creates significant load
- **Potential queue buildup**: High-frequency arrivals may saturate processing capacity
- **Resource contention**: Competes with other sensors for CPU, memory, network
- **Expected latency**: Higher than temperature sensor, especially under heavy load
- **Critical consideration**: May require dedicated processing resources or higher MIPS allocation

**Motion Sensor (10 ms, Exponential):**

- **Variable latency**: Bursty arrivals cause latency spikes during burst periods
- **Average latency**: Lower than deterministic sensors due to lower average rate
- **Burst impact**: Short bursts (multiple tuples in quick succession) → temporary queue buildup → latency spikes
- **Gap impact**: Long gaps between tuples → queues drain → low latency periods
- **Expected latency**: Average is low, but variance is high

#### Latency Comparison:

```
Average Latency: Motion < Temperature < Heartbeat
Latency Variance: (Temperature ≈ Heartbeat) < Motion
Peak Latency: Motion (bursts) > Heartbeat > Temperature
```

---

### 2. Throughput Analysis

#### System-Level Throughput:

**Combined Throughput** (all three sensors):

```
Total average throughput = 200 + 500 + 100 = 800 tuples/second
```

#### Throughput Characteristics:

**Temperature Sensor:**

- **Steady throughput**: Constant 200 tuples/sec
- **Predictable load**: Enables efficient resource allocation
- **No burst behavior**: Smooth, continuous data flow

**Heartbeat Sensor:**

- **High throughput**: Constant 500 tuples/sec (highest among three)
- **Dominant workload**: Contributes 62.5% of total system throughput
- **Resource intensive**: Requires sufficient bandwidth and processing capacity
- **Risk**: May cause throughput degradation for other sensors if resources are limited

**Motion Sensor:**

- **Variable throughput**: Instantaneous rate fluctuates widely
- **Average throughput**: ~100 tuples/sec (lowest among three)
- **Burst periods**: Short-term throughput can exceed 500 tuples/sec
- **Idle periods**: Throughput drops to near zero during gaps
- **System impact**: Less predictable; requires adaptive resource management

#### Throughput Bottlenecks:

1. **Network Bandwidth**: If combined tuple size × throughput exceeds network capacity → packet drops
2. **Processing Capacity**: If total MIPS demand exceeds available capacity → queueing and delays
3. **Module Selectivity**: High selectivity (output/input ratio) multiplies data volume downstream

#### Throughput Saturation:

When total throughput approaches system capacity:

- **Queueing increases** → Latency rises
- **Buffer overflows** → Packet drops
- **Throughput plateaus** → Cannot increase further despite higher emission rates

---

### 3. Energy Consumption Analysis

Energy consumption in fog computing comprises:

1. **Transmission energy**: Sensor → Gateway communication
2. **Processing energy**: Computation at fog nodes
3. **Network energy**: Data transmission between fog tiers
4. **Idle energy**: Baseline power when devices are on but inactive

#### Energy Model:

```
Total Energy = (Busy Power × Busy Time) + (Idle Power × Idle Time)
```

For edge device in example:

- **Busy Power**: 90 watts
- **Idle Power**: 80 watts

#### Energy Consumption by Sensor:

**Temperature Sensor (5 ms, 200 tuples/sec):**

- **Processing time per tuple**: ~10 MIPS / processing capacity
- **Busy time per second**: (200 tuples/sec) × (processing time per tuple)
- **Moderate energy consumption**: Proportional to moderate throughput
- **Predictable energy draw**: Steady emission rate → consistent power profile

**Heartbeat Sensor (2 ms, 500 tuples/sec):**

- **Highest energy consumption**: 2.5× more tuples than temperature sensor
- **Extended busy periods**: Device spends more time in high-power state
- **Reduced idle time**: Less opportunity to enter low-power modes
- **Energy cost**: Significantly higher than other sensors
- **Trade-off**: Data freshness and responsiveness vs. energy efficiency

**Motion Sensor (10 ms exponential, ~100 tuples/sec avg):**

- **Variable energy consumption**: Spikes during bursts, low during gaps
- **Average energy**: Lower than deterministic sensors due to lower average rate
- **Burst energy spikes**: Short periods of high power consumption
- **Gap energy savings**: Device can enter idle state during long gaps
- **Energy efficiency**: Best among three sensors on average, but less predictable

#### Energy Consumption Ranking:

```
Average Energy: Motion < Temperature < Heartbeat
Energy Variance: (Temperature ≈ Heartbeat) < Motion
Peak Energy: Heartbeat > (Motion bursts) > Temperature
```

#### Energy Optimization Strategies:

1. **Dynamic Voltage and Frequency Scaling (DVFS)**: Adjust processing power based on workload
2. **Sleep Modes**: During motion sensor gaps, transition edge device to low-power state
3. **Batch Processing**: Accumulate tuples and process in batches (increases latency but saves energy)
4. **Adaptive Emission Rates**: Reduce heartbeat sensor rate during non-critical periods
5. **Edge Intelligence**: Filter redundant data at edge to reduce upstream transmission energy

---

### 4. Combined System Impact

#### Resource Contention:

- **Heartbeat sensor** dominates resource usage (CPU, network, energy)
- **Temperature sensor** contributes moderate, steady load
- **Motion sensor** causes occasional contention during bursts

#### System Design Considerations:

1. **Prioritization**: Assign higher priority to time-critical heartbeat data
2. **Resource Allocation**: Dedicate more MIPS to heartbeat processing module
3. **Buffer Sizing**: Size queues to handle motion sensor bursts without drops
4. **Network Planning**: Ensure bandwidth accommodates peak combined throughput (~800+ tuples/sec during bursts)

#### QoS Trade-offs:

| **Scenario**                      | **Latency** | **Throughput** | **Energy** |
| --------------------------------- | ----------- | -------------- | ---------- |
| High emission rates (all sensors) | High        | High           | High       |
| Low emission rates (all sensors)  | Low         | Low            | Low        |
| Mixed rates (current setup)       | Variable    | Moderate       | Moderate   |
| Burst handling (motion spikes)    | Spikes      | Spikes         | Spikes     |

---

### 5. Practical Recommendations

**For Latency-Critical Applications (e.g., Healthcare Monitoring):**

- Use deterministic distributions for predictable latency
- Allocate sufficient resources to prevent queueing
- Deploy processing modules at edge (level 2-3) to minimize transmission delays
- Consider heartbeat sensor as highest priority

**For Energy-Constrained Applications (e.g., Battery-Powered IoT):**

- Use exponential distributions to allow idle periods
- Implement adaptive emission rates based on battery level
- Process data locally to reduce network transmission energy
- Favor lower emission rates (e.g., motion sensor model)

**For High-Throughput Applications (e.g., Video Surveillance):**

- Ensure network bandwidth exceeds peak combined throughput
- Use edge devices with high MIPS capacity
- Implement load balancing across multiple edge devices
- Monitor and prevent bottlenecks

**For Mixed Workloads (Current Scenario):**

- Implement QoS policies to prioritize critical sensors (heartbeat)
- Use adaptive resource allocation based on current load
- Monitor latency and throughput metrics to detect saturation
- Balance energy consumption with application requirements

---

## Summary

This lab demonstrates that **tuple emission rate is a critical design parameter** in fog computing applications. Key takeaways:

1. **Deterministic distributions** provide predictability but create steady resource demands
2. **Exponential distributions** model real-world variability, creating bursts and gaps
3. **Higher emission rates** improve data freshness but increase latency, reduce throughput efficiency, and consume more energy
4. **System capacity** must accommodate peak combined throughput from all sensors
5. **Trade-offs** between latency, throughput, and energy require careful application-specific tuning

The implementation in `MultiSensorExample.java` provides a complete, runnable example demonstrating these concepts in iFogSim2.

---

## Files Included

1. **ExponentialDistribution.java**: New distribution class for exponential tuple emission
2. **Distribution.java**: Updated to include EXPONENTIAL distribution type
3. **MultiSensorExample.java**: Complete working example with three sensors and one actuator
4. **Lab03_Sensors_Different_Emission_Rates_Documentation.md**: This document (comprehensive answers)

---

## How to Run the Example

1. **Ensure iFogSim2 is properly set up** with all dependencies in the `jars/` folder
2. **Compile the project** (if using IDE, refresh/rebuild)
3. **Run `MultiSensorExample.java`** from `org.fog.test.perfeval` package
4. **Observe console output** showing sensor configuration and simulation results
5. **Analyze results** in terms of latency, throughput, and energy consumption

---

## References

- iFogSim2 Framework: https://github.com/Cloudslab/iFogSim
- Original Paper: Gupta et al., "iFogSim: A toolkit for modeling and simulation of resource management techniques in the Internet of Things, Edge and Fog computing environments"
- Exponential Distribution: https://en.wikipedia.org/wiki/Exponential_distribution
- Fog Computing Principles: Bonomi et al., "Fog Computing and Its Role in the Internet of Things"

---

**End of Document**
