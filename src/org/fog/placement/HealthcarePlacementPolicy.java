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


