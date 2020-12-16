package cloud.kubernetes.aks.dto;

import com.azure.core.management.Region;

import java.util.Map;

public class CreateAzureClusterRequest {
    private String clusterName;
    private String resourceGroupName;
    private String rootUserName;
    private String agentPoolName;
    private int machineCountInPool;
    private String regionName;
    private String virtualMachineSize;
    private String osType;
    private Map<String,String> tagMap;
    private int osDiskInGb;
    private String networkPluginName;

    private String serviceAddressRange;
    private String dnsServiceIp;
    private String dockerBridgeAddress;

    public String getServiceAddressRange() {
        return serviceAddressRange;
    }

    public void setServiceAddressRange(String serviceAddressRange) {
        this.serviceAddressRange = serviceAddressRange;
    }

    public String getDnsServiceIp() {
        return dnsServiceIp;
    }

    public void setDnsServiceIp(String dnsServiceIp) {
        this.dnsServiceIp = dnsServiceIp;
    }

    public String getDockerBridgeAddress() {
        return dockerBridgeAddress;
    }

    public void setDockerBridgeAddress(String dockerBridgeAddress) {
        this.dockerBridgeAddress = dockerBridgeAddress;
    }

    public String getNetworkPluginName() {
        return networkPluginName;
    }

    public void setNetworkPluginName(String networkPluginName) {
        this.networkPluginName = networkPluginName;
    }

    public String getOsType() {
        return osType;
    }

    public int getOsDiskInGb() {
        return osDiskInGb;
    }

    public void setOsDiskInGb(int osDiskInGb) {
        this.osDiskInGb = osDiskInGb;
    }

    public Map<String, String> getTagMap() {
        return tagMap;
    }

    public void setTagMap(Map<String, String> tagMap) {
        this.tagMap = tagMap;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getVirtualMachineSize() {
        return virtualMachineSize;
    }

    public void setVirtualMachineSize(String virtualMachineSize) {
        this.virtualMachineSize = virtualMachineSize;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    public String getRootUserName() {
        return rootUserName;
    }

    public void setRootUserName(String rootUserName) {
        this.rootUserName = rootUserName;
    }

    public String getAgentPoolName() {
        return agentPoolName;
    }

    public void setAgentPoolName(String agentPoolName) {
        this.agentPoolName = agentPoolName;
    }

    public int getMachineCountInPool() {
        return machineCountInPool;
    }

    public void setMachineCountInPool(int machineCountInPool) {
        this.machineCountInPool = machineCountInPool;
    }
}
