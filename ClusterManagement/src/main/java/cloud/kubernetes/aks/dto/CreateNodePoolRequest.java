package cloud.kubernetes.aks.dto;


public class CreateNodePoolRequest {
    private String clusterId;
    private String poolName;
    private String virtualMachineSize;
    private String osType;
    private int osDiskInGb;
    private int machineCountInPool;

    public int getMachineCountInPool() {
        return machineCountInPool;
    }

    public void setMachineCountInPool(int machineCountInPool) {
        this.machineCountInPool = machineCountInPool;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public String getVirtualMachineSize() {
        return virtualMachineSize;
    }

    public void setVirtualMachineSize(String virtualMachineSize) {
        this.virtualMachineSize = virtualMachineSize;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public int getOsDiskInGb() {
        return osDiskInGb;
    }

    public void setOsDiskInGb(int osDiskInGb) {
        this.osDiskInGb = osDiskInGb;
    }
}
