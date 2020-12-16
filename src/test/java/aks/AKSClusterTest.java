package aks;

import cloud.kubernetes.aks.AKSCluster;
import cloud.kubernetes.aks.dto.CreateAzureClusterRequest;
import cloud.kubernetes.aks.dto.CreateNodePoolRequest;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.jcraft.jsch.JSchException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;

public class AKSClusterTest {

    final String tenentId = ""; //need to be set
    final String clientId = ""; //need to be set
    final String secret = ""; //need to be set
    private String clusterId = null;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    @Test(dataProvider = "testDataProvider")
    public void testCreateCluster(TestData testData) throws JSchException, UnsupportedEncodingException {
        AKSCluster aksCluster = new AKSCluster(tenentId,clientId, secret,null);
        CreateAzureClusterRequest createAzureClusterRequest = new CreateAzureClusterRequest();
        createAzureClusterRequest.setClusterName(testData.getClusterName());
        createAzureClusterRequest.setMachineCountInPool(testData.getMachineCountInPool());
        KubernetesCluster kubernetesCluster = aksCluster.createCluster(createAzureClusterRequest);
        Assert.assertEquals(kubernetesCluster.provisioningState(),"Succeeded");
        setClusterId(kubernetesCluster.id());
    }

    @Test(dependsOnMethods = {"testCreateCluster"})
    public void testGetClusterById(){
        AKSCluster aksCluster = new AKSCluster(tenentId,clientId, secret,null);
        KubernetesCluster kubernetesCluster = aksCluster.getClusterById(getClusterId());
        Assert.assertEquals(kubernetesCluster.provisioningState(),"Succeeded");
    }

    @Test(dataProvider = "testDataProvider",dependsOnMethods = {"testGetClusterById"})
    public void testCreateNodePool(TestData testData){
        AKSCluster aksCluster = new AKSCluster(tenentId,clientId, secret,null);
        CreateNodePoolRequest createNodePoolRequest = new CreateNodePoolRequest();
        createNodePoolRequest.setPoolName(testData.getUpdatePoolName());
        createNodePoolRequest.setClusterId(clusterId);
        aksCluster.createNodePool(createNodePoolRequest);
        KubernetesCluster kubernetesCluster = aksCluster.getClusterById(clientId);
        Assert.assertEquals(kubernetesCluster.provisioningState(),"Succeeded");
        Assert.assertEquals(kubernetesCluster.agentPools().size(),2);
    }

    @Test(dataProvider = "testDataProvider",dependsOnMethods = {"testCreateNodePool"})
    public void testScaleNodePool(TestData testData){
        AKSCluster aksCluster = new AKSCluster(tenentId,clientId, secret,null);
        aksCluster.scaleNodePool(clusterId,testData.getUpdatePoolName(),testData.getUpdateMachineCountInPool());
        KubernetesCluster kubernetesCluster = aksCluster.getClusterById(clientId);
        Assert.assertEquals(kubernetesCluster.provisioningState(),"Succeeded");
        Assert.assertEquals(kubernetesCluster.agentPools().get(testData.getUpdatePoolName()).count(),testData.getUpdateMachineCountInPool());
    }

    @Test(dependsOnMethods = {"testScaleNodePool"},expectedExceptions = ManagementException.class)
    public void testDeleteResourceGroup(){
        AKSCluster aksCluster = new AKSCluster(tenentId,clientId, secret,null);
        KubernetesCluster kubernetesCluster = aksCluster.getClusterById(getClusterId());
        aksCluster.deleteResourceGroup(kubernetesCluster.resourceGroupName());
        aksCluster.getClusterById(getClusterId());
    }


    @DataProvider(name = "testDataProvider")
    public Object[][] createEKSClusterDataProvider(){
        return new Object[][]{{getTestDataObject()}};
    }

    public TestData getTestDataObject(){
        TestData testData = new TestData();
        testData.setClusterName("cluster1");
        testData.setUpdatePoolName("pool1");
        testData.setMachineCountInPool(2);
        testData.setUpdateMachineCountInPool(2);
        return testData;
    }

}

class TestData {
    private String clusterName;
    private int machineCountInPool;
    private int updateMachineCountInPool;
    private String updatePoolName;

    public int getUpdateMachineCountInPool() {
        return updateMachineCountInPool;
    }

    public void setUpdateMachineCountInPool(int updateMachineCountInPool) {
        this.updateMachineCountInPool = updateMachineCountInPool;
    }

    public String getUpdatePoolName() {
        return updatePoolName;
    }

    public void setUpdatePoolName(String updatePoolName) {
        this.updatePoolName = updatePoolName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public int getMachineCountInPool() {
        return machineCountInPool;
    }

    public void setMachineCountInPool(int machineCountInPool) {
        this.machineCountInPool = machineCountInPool;
    }
}
