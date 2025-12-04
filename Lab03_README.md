# Lab 03: Sensors with Different Tuple Emission Rates

## Quick Start Guide

This lab demonstrates the implementation of sensors with different tuple emission rates in iFogSim2.

### Files Created/Modified

1. **ExponentialDistribution.java** (`src/org/fog/utils/distribution/`)

   - New distribution class implementing exponential distribution for sensor emission rates
   - Follows the same pattern as existing distributions (Deterministic, Normal, Uniform)

2. **Distribution.java** (`src/org/fog/utils/distribution/`)

   - Updated to add `EXPONENTIAL = 4` constant

3. **MultiSensorExample.java** (`src/org/fog/test/perfeval/`)

   - Complete working example demonstrating three sensors:
     - Temperature Sensor: Deterministic 5ms
     - Heartbeat Sensor: Deterministic 2ms
     - Motion Sensor: Exponential mean 10ms
   - Includes edge device creation and actuator attachment

4. **Lab03_Sensors_Different_Emission_Rates_Documentation.md** (root directory)
   - Comprehensive documentation answering all lab questions
   - Detailed analysis of latency, throughput, and energy consumption

### How to Run

#### Option 1: Using IDE (IntelliJ IDEA, Eclipse, etc.)

1. Open the iFogSim project in your IDE
2. Navigate to `src/org/fog/test/perfeval/MultiSensorExample.java`
3. Right-click and select "Run" or click the Run button
4. Observe the output in the console

#### Option 2: Using Command Line

```bash
# Navigate to project directory
cd "/Users/sakshamshrey/Desktop/Sem-07/Edge Computing/iFogSim2/iFogSim"

# Compile (ensure all JARs are in classpath)
javac -cp "jars/*:src" src/org/fog/test/perfeval/MultiSensorExample.java

# Run
java -cp "jars/*:out/production/iFogSim" org.fog.test.perfeval.MultiSensorExample
```

### Expected Output

The simulation will display:

- Sensor configuration summary showing emission rates
- Throughput calculations for each sensor
- Simulation progress and results
- Performance metrics (latency, energy consumption)

### Understanding the Code

#### Sensor Creation Pattern

```java
// Deterministic Sensor (fixed interval)
Sensor sensor = new Sensor(
    "sensor-name",
    "TUPLE_TYPE",
    userId,
    appId,
    new DeterministicDistribution(intervalMs)
);

// Exponential Sensor (variable interval)
Sensor sensor = new Sensor(
    "sensor-name",
    "TUPLE_TYPE",
    userId,
    appId,
    new ExponentialDistribution(meanMs)
);
```

#### Attaching Sensor to Edge Device

```java
sensor.setGatewayDeviceId(edgeDevice.getId());
sensor.setLatency(1.0);  // latency in ms
sensors.add(sensor);
```

#### Creating Actuator

```java
Actuator actuator = new Actuator(
    "actuator-name",
    userId,
    appId,
    "ACTUATOR_TYPE"
);
actuator.setGatewayDeviceId(edgeDevice.getId());
actuator.setLatency(1.0);
actuators.add(actuator);
```

### Key Concepts Demonstrated

1. **Deterministic Distribution**: Fixed, predictable emission intervals

   - Temperature sensor: 200 tuples/sec (5ms interval)
   - Heartbeat sensor: 500 tuples/sec (2ms interval)

2. **Exponential Distribution**: Variable, random emission intervals

   - Motion sensor: ~100 tuples/sec average (mean 10ms)
   - Models real-world event-driven scenarios
   - Creates bursty traffic patterns

3. **Edge Device Attachment**: All sensors connected to same edge device

   - Demonstrates resource sharing and contention
   - Shows realistic multi-sensor deployment

4. **Application Flow**: Sensor → Processor Module → Alert Generator → Actuator
   - Complete end-to-end data processing pipeline
   - Includes monitoring loops for latency tracking

### Performance Characteristics

| Sensor      | Distribution  | Interval  | Throughput      | Behavior                 |
| ----------- | ------------- | --------- | --------------- | ------------------------ |
| Temperature | Deterministic | 5ms       | 200 tuples/sec  | Steady, predictable      |
| Heartbeat   | Deterministic | 2ms       | 500 tuples/sec  | High-frequency, critical |
| Motion      | Exponential   | Mean 10ms | ~100 tuples/sec | Bursty, variable         |

**Combined System Throughput**: ~800 tuples/second average

### Impact on System Metrics

**Latency:**

- Heartbeat sensor creates highest load → potential queue buildup
- Motion sensor causes latency spikes during bursts
- Temperature sensor provides moderate, predictable latency

**Energy Consumption:**

- Heartbeat sensor: Highest (500 tuples/sec)
- Temperature sensor: Moderate (200 tuples/sec)
- Motion sensor: Lowest average (100 tuples/sec)

**Throughput:**

- System must handle peak combined load
- Exponential distribution creates variable throughput
- Deterministic sensors provide predictable capacity planning

### Troubleshooting

**Issue**: Compilation errors

- **Solution**: Ensure all JAR files in `jars/` folder are in classpath
- Check that Java version is compatible (Java 8+)

**Issue**: Simulation doesn't start

- **Solution**: Verify CloudSim initialization in main method
- Check that broker, application, and devices are created correctly

**Issue**: No output or errors

- **Solution**: Enable logging by commenting out `Log.disable();`
- Check console for exception stack traces

### Extending the Example

You can modify the example to:

1. **Change emission rates**: Adjust distribution parameters

   ```java
   new DeterministicDistribution(10)  // Change to 10ms
   new ExponentialDistribution(20)    // Change mean to 20ms
   ```

2. **Add more sensors**: Follow the pattern in `createFogDevices()`

   ```java
   Sensor newSensor = new Sensor(...);
   newSensor.setGatewayDeviceId(edgeDevice.getId());
   sensors.add(newSensor);
   ```

3. **Use different distributions**:

   ```java
   new NormalDistribution(mean, stdDev)
   new UniformDistribution(min, max)
   ```

4. **Deploy across multiple edge devices**:

   - Create additional edge devices
   - Distribute sensors across them
   - Analyze load balancing effects

5. **Experiment with module placement**:
   - Move processing to cloud vs. edge
   - Compare latency and energy consumption

### Assignment Submission

Your submission should include:

1. **Documentation** (PDF or Word):

   - Answers to questions a, b, c, d
   - Code snippets with explanations
   - Analysis of performance impacts
   - Use `Lab03_Sensors_Different_Emission_Rates_Documentation.md` as reference

2. **Code Files**:

   - ExponentialDistribution.java
   - MultiSensorExample.java (or your modified version)
   - Any additional classes you created

3. **Results** (Optional but recommended):
   - Screenshots of simulation output
   - Performance graphs (if generated)
   - Observations and insights

### Additional Resources

- **iFogSim Documentation**: Check the GitHub repository for API details
- **Example Code**: Review other examples in `src/org/fog/test/perfeval/`
- **Distribution Theory**: Research exponential and deterministic distributions
- **Fog Computing**: Read papers on edge computing architectures

### Contact

For questions about the lab assignment, consult:

- Course instructor
- Lab manual
- iFogSim GitHub issues (for framework-specific questions)

---

**Good luck with your lab assignment!**
