package eks;
import cloud.kubernetes.eks.EKSCluster;
import cloud.kubernetes.eks.model.result.VPCResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.DescribeSubnetsResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.eks.model.CreateClusterRequest;
import com.amazonaws.services.eks.model.CreateClusterResult;
import com.amazonaws.services.eks.model.CreateNodegroupRequest;
import com.amazonaws.services.eks.model.CreateNodegroupResult;
import com.amazonaws.services.eks.model.DeleteClusterResult;
import com.amazonaws.services.eks.model.DeleteNodegroupResult;
import com.amazonaws.services.eks.model.DescribeNodegroupResult;
import com.amazonaws.services.eks.model.ListClustersResult;
import com.amazonaws.services.eks.model.NodegroupScalingConfig;
import com.amazonaws.services.eks.model.ResourceNotFoundException;
import com.amazonaws.services.eks.model.UpdateClusterVersionRequest;
import com.amazonaws.services.eks.model.UpdateClusterVersionResult;
import com.amazonaws.services.eks.model.UpdateNodegroupConfigRequest;
import com.amazonaws.services.eks.model.UpdateNodegroupConfigResult;
import com.amazonaws.services.eks.model.VpcConfigRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class EKSClusterTest {


final int MAX_ITERATIONS = 420;
final int EACH_ITERATIONS_SLEEP_TIME_IN_SEC = 5;
final String accessKey = ""; //need to set
final String secretKey = ""; //need to set
final String regionName = ""; //need to set

@Test(dataProvider = "testDataProvider")
public void testCreateEKSCluster(TestData testData){
    String clusterName = testData.getEksClusterName();
    List<String> subnets = testData.getSubnets();
    List<String> secutiryGroup = testData.getSecurityGroup();
    String version = testData.getVersion();
    String roleARN = testData.getRoleARN();
    EKSCluster eksCluster = new EKSCluster(accessKey,secretKey,regionName);
    VpcConfigRequest vpcConfigRequest = new VpcConfigRequest();
    vpcConfigRequest.setSecurityGroupIds(secutiryGroup);
    vpcConfigRequest.setSubnetIds(subnets);
    CreateClusterRequest createClusterRequest = new CreateClusterRequest();
    createClusterRequest.setName(clusterName);
    createClusterRequest.setVersion(version);
    createClusterRequest.setRoleArn(roleARN);
    createClusterRequest.setResourcesVpcConfig(vpcConfigRequest);
    CreateClusterResult createClusterResult = eksCluster.createCluster(createClusterRequest);
    assertEquals(createClusterResult.getCluster().getStatus(),"CREATING");
    wait(5);
    for(int i=0;i<MAX_ITERATIONS;i++){
        wait(EACH_ITERATIONS_SLEEP_TIME_IN_SEC);
        String status = eksCluster.getClusterStatus(clusterName);
        if(!status.equalsIgnoreCase("CREATING")){
            break;
        }
    }

    String status = eksCluster.getClusterStatus(clusterName);
    assertEquals(status,"ACTIVE");
}

@Test(dataProvider = "testDataProvider",dependsOnMethods = {"testCreateEKSCluster"})
public void testAddNodeGroupInEKSCluster(TestData testData){
    String clusterName = testData.getEksClusterName();
    String nodeGroupName = testData.getNodeGroupName();
    List<String> subnets = testData.getSubnets();
    int min = testData.getMin();
    int max = testData.getMax();
    int desired = testData.getDesired();
    List<String> instanceType = testData.getInstanceType();
    String amiType = testData.getAmiType();
    String nodeRole = testData.getNodeRole();
    Map<String,String> labels = testData.getNodeGroupLabels();
    EKSCluster eksCluster = new EKSCluster(accessKey,secretKey,regionName);
    CreateNodegroupRequest createNodegroupRequest = new CreateNodegroupRequest();
    createNodegroupRequest.setClusterName(clusterName);
    createNodegroupRequest.setDiskSize(testData.getDiskSize());
    createNodegroupRequest.setNodegroupName(nodeGroupName);
    NodegroupScalingConfig nodegroupScalingConfig = new NodegroupScalingConfig();
    nodegroupScalingConfig.setMinSize(min);
    nodegroupScalingConfig.setMaxSize(max);
    nodegroupScalingConfig.setDesiredSize(desired);
    createNodegroupRequest.setScalingConfig(nodegroupScalingConfig);
    createNodegroupRequest.setSubnets(subnets);
    createNodegroupRequest.setInstanceTypes(instanceType);
    createNodegroupRequest.setAmiType(amiType);
    createNodegroupRequest.setNodeRole(nodeRole);
    createNodegroupRequest.setLabels(labels);
    CreateNodegroupResult createNodegroupResult = eksCluster.addNodeGroupToCluster(createNodegroupRequest);
    assertEquals(createNodegroupResult.getNodegroup().getStatus(),"CREATING");
    wait(5);
    for(int i=0;i<MAX_ITERATIONS;i++){
        wait(EACH_ITERATIONS_SLEEP_TIME_IN_SEC);
        String status = eksCluster.getNodeGroupStatus(clusterName,nodeGroupName);
        if(!status.equalsIgnoreCase("CREATING")){
            break;
        }
    }
    String status = eksCluster.getNodeGroupStatus(clusterName,nodeGroupName);
    assertEquals(status,"ACTIVE");
}

@Test(dataProvider = "testDataProvider",dependsOnMethods = {"testAddNodeGroupInEKSCluster"})
public void testListClusters(TestData testData){
    EKSCluster eksCluster = new EKSCluster(accessKey,secretKey,regionName);
    ListClustersResult listClustersResult =  eksCluster.listCluster();
    List<String> listClusters = listClustersResult.getClusters();
    assertTrue(listClusters.contains(testData.getEksClusterName()));
}

@Test(dataProvider = "testDataProvider", dependsOnMethods = {"testListClusters"})
public void testUpgradeCluster(TestData testData){
    EKSCluster eksCluster = new EKSCluster(accessKey,secretKey,regionName);
    UpdateClusterVersionRequest updateClusterVersionRequest = new UpdateClusterVersionRequest();
    updateClusterVersionRequest.setVersion(testData.getNewVersion());
    updateClusterVersionRequest.setName(testData.getEksClusterName());
    UpdateClusterVersionResult updateClusterVersionResult = eksCluster.updateCluster(updateClusterVersionRequest);
    assertEquals(updateClusterVersionResult.getUpdate().getStatus(),"InProgress");
    wait(5);
    for(int i=0;i<MAX_ITERATIONS;i++){
        wait(EACH_ITERATIONS_SLEEP_TIME_IN_SEC);
        String status = eksCluster.getClusterStatus(testData.getEksClusterName());
        if(!status.equalsIgnoreCase("UPDATING")){
            break;
        }
    }
    String status = eksCluster.getClusterStatus(testData.getEksClusterName());
    assertEquals(status,"ACTIVE");
    assertEquals(eksCluster.getCluster(testData.getEksClusterName()).getVersion(),testData.getNewVersion());
}

@Test(dataProvider = "testDataProvider", dependsOnMethods = {"testUpgradeCluster"})
public void testUpgradeNodeGroup(TestData testData){
    EKSCluster eksCluster = new EKSCluster(accessKey,secretKey,regionName);
    UpdateNodegroupConfigRequest updateNodegroupConfigRequest = new UpdateNodegroupConfigRequest();
    updateNodegroupConfigRequest.setClusterName(testData.getEksClusterName());
    updateNodegroupConfigRequest.setNodegroupName(testData.getNodeGroupName());
    NodegroupScalingConfig nodegroupScalingConfig = new NodegroupScalingConfig();
    nodegroupScalingConfig.setMinSize(testData.getNodeGrpUpdateMin());
    nodegroupScalingConfig.setMaxSize(testData.getNodeGrpUpdateMax());
    nodegroupScalingConfig.setDesiredSize(testData.getNodeGrpUpdateDesired());
    updateNodegroupConfigRequest.setScalingConfig(nodegroupScalingConfig);
    UpdateNodegroupConfigResult updateNodegroupConfigResult = eksCluster.updateNodeGroup(updateNodegroupConfigRequest);
    assertEquals(updateNodegroupConfigResult.getUpdate().getStatus(),"InProgress");
    wait(5);
    for(int i=0;i<MAX_ITERATIONS;i++){
        wait(EACH_ITERATIONS_SLEEP_TIME_IN_SEC);
        String status = eksCluster.getNodeGroupStatus(testData.getEksClusterName(),testData.getNodeGroupName());
        if(!status.equalsIgnoreCase("UPDATING")){
            break;
        }
    }
    String status = eksCluster.getClusterStatus(testData.getEksClusterName());
    assertEquals(status,"ACTIVE");
    DescribeNodegroupResult describeNodegroupResult = eksCluster.getNodeGroup(testData.getEksClusterName(),testData.getNodeGroupName());
    assertEquals(describeNodegroupResult.getNodegroup().getScalingConfig().getMaxSize(),(Integer) testData.getNodeGrpUpdateMax());
    assertEquals(describeNodegroupResult.getNodegroup().getScalingConfig().getMinSize(),(Integer) testData.getMin());
    assertEquals(describeNodegroupResult.getNodegroup().getScalingConfig().getDesiredSize(),(Integer) testData.getDesired());
}

@Test(dataProvider = "testDataProvider", dependsOnMethods = {"testUpgradeNodeGroup"},expectedExceptions = ResourceNotFoundException.class)
public void testDeleteNodeGroupInEKSCluster(TestData testData){
    EKSCluster eksCluster = new EKSCluster(accessKey,secretKey,regionName);
    DeleteNodegroupResult deleteNodegroupResult = eksCluster.deleteNodeGroup(testData.getEksClusterName(),testData.getNodeGroupName());
    assertEquals(deleteNodegroupResult.getNodegroup().getStatus(),"DELETING");
    for(int i=0;i<MAX_ITERATIONS;i++){
        wait(EACH_ITERATIONS_SLEEP_TIME_IN_SEC);
        eksCluster.getNodeGroupStatus(testData.getEksClusterName(),testData.getNodeGroupName());
    }

}

@Test(dataProvider = "testDataProvider", dependsOnMethods = {"testDeleteNodeGroupInEKSCluster"},expectedExceptions = ResourceNotFoundException.class)
public void testDeleteEKSCluster(TestData testData){
    EKSCluster eksCluster = new EKSCluster(accessKey,secretKey,regionName);
    DeleteClusterResult deleteClusterResult = eksCluster.deleteCluster(testData.getEksClusterName());
    assertEquals(deleteClusterResult.getCluster().getStatus(),"DELETING");
    for(int i=0;i<MAX_ITERATIONS;i++){
        wait(EACH_ITERATIONS_SLEEP_TIME_IN_SEC);
        eksCluster.getClusterStatus(testData.getEksClusterName());
    }

}



public void wait(int ms){
    try {
        Thread.sleep(ms*1000);
    }catch (InterruptedException exp){

    }

}


/************ Data Provider code begins ************/

public TestData getTestDataObject(){
    TestData testData = new TestData();
    testData.setSubnets(getSubnets());
    testData.setSecurityGroup(getSecurityGroup());
    testData.setVersion("1.17");
    testData.setRoleARN("arn:aws:iam::607123010370:role/eks-master-role"); //need to set
    testData.setEksClusterName(getClusterName());

    testData.setNodeGroupName("eks-node-grp");
    testData.setInstanceType(Arrays.asList("t3.medium"));
    testData.setAmiType("AL2_x86_64");//need to set
    testData.setNodeRole("arn:aws:iam::607123010370:role/node-role");//need to set
    Map<String,String> labels = new HashMap<>();
    labels.put("k0","v0");
    testData.setNodeGroupLabels(labels);
    testData.setMin(1);
    testData.setMax(1);
    testData.setDesired(1);
    testData.setDiskSize(20);

    testData.setNodeGrpUpdateMin(1);
    testData.setNodeGrpUpdateMax(2);
    testData.setNodeGrpUpdateDesired(1);
    testData.setNewVersion("1.18");
    return testData;
}

@DataProvider(name = "testDataProvider")
public Object[][] createEKSClusterDataProvider(){
    return new Object[][]{{getTestDataObject()}};
}


public String getClusterName(){
    return "kube-9";
}

public String getVPCName(){
    return "gurushant-vpc-VPC";
}


public List<String> getSubnets(){
    String vpcName = getVPCName();
    EKSCluster eksCluster = new EKSCluster(accessKey,secretKey,regionName);
    List<VPCResult> vpcResultList = eksCluster.listVPC();
    String vpcId = null;
    for(VPCResult vpcResult:vpcResultList){
        if(vpcResult.getName().equals(vpcName)){
            vpcId = vpcResult.getVpcId();
            break;
        }
    }
    DescribeSubnetsResult describeSubnetsResult  = eksCluster.listSubnetsFromVPC(vpcId);
    List<String> subnets = new ArrayList<>();
    for(Subnet subnet: describeSubnetsResult.getSubnets()){
        subnets.add(subnet.getSubnetId());
    }
    return subnets;
}

public List<String> getSecurityGroup(){
    EKSCluster eksCluster = new EKSCluster(accessKey,secretKey,regionName);
    DescribeSecurityGroupsResult describeSecurityGroupsResult = eksCluster.listSecurityGroups();
    List<SecurityGroup> securityGroupList = describeSecurityGroupsResult.getSecurityGroups();
    List<String> stringArrayList = new ArrayList<>();
    if(stringArrayList.size() > 0){
        stringArrayList.add(securityGroupList.get(0).getGroupId());
    }
    return stringArrayList;
}
}

class TestData {
    private String eksClusterName;
    private List<String> subnets;
    private List<String> securityGroup;
    private String version;
    private String roleARN;

    private String nodeGroupName;
    List<String> instanceType;
    String amiType;
    String nodeRole;
    Map<String,String> nodeGroupLabels;
    int min;
    int max;
    int desired;
    int nodeGrpUpdateMin;
    int nodeGrpUpdateMax;
    int nodeGrpUpdateDesired;
    int diskSize;

    public int getDiskSize() {
        return diskSize;
    }

    public void setDiskSize(int diskSize) {
        this.diskSize = diskSize;
    }

    public int getNodeGrpUpdateMin() {
        return nodeGrpUpdateMin;
    }

    public void setNodeGrpUpdateMin(int nodeGrpUpdateMin) {
        this.nodeGrpUpdateMin = nodeGrpUpdateMin;
    }

    public int getNodeGrpUpdateMax() {
        return nodeGrpUpdateMax;
    }

    public void setNodeGrpUpdateMax(int nodeGrpUpdateMax) {
        this.nodeGrpUpdateMax = nodeGrpUpdateMax;
    }

    public int getNodeGrpUpdateDesired() {
        return nodeGrpUpdateDesired;
    }

    public void setNodeGrpUpdateDesired(int nodeGrpUpdateDesired) {
        this.nodeGrpUpdateDesired = nodeGrpUpdateDesired;
    }

    public String newVersion;

    public String getNewVersion() {
        return newVersion;
    }

    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getDesired() {
        return desired;
    }

    public void setDesired(int desired) {
        this.desired = desired;
    }

    public String getEksClusterName() {
        return eksClusterName;
    }

    public void setEksClusterName(String eksClusterName) {
        this.eksClusterName = eksClusterName;
    }

    public List<String> getSubnets() {
        return subnets;
    }

    public void setSubnets(List<String> subnets) {
        this.subnets = subnets;
    }

    public List<String> getSecurityGroup() {
        return securityGroup;
    }

    public void setSecurityGroup(List<String> securityGroup) {
        this.securityGroup = securityGroup;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRoleARN() {
        return roleARN;
    }

    public void setRoleARN(String roleARN) {
        this.roleARN = roleARN;
    }

    public String getNodeGroupName() {
        return nodeGroupName;
    }

    public void setNodeGroupName(String nodeGroupName) {
        this.nodeGroupName = nodeGroupName;
    }

    public List<String> getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(List<String> instanceType) {
        this.instanceType = instanceType;
    }

    public String getAmiType() {
        return amiType;
    }

    public void setAmiType(String amiType) {
        this.amiType = amiType;
    }

    public String getNodeRole() {
        return nodeRole;
    }

    public void setNodeRole(String nodeRole) {
        this.nodeRole = nodeRole;
    }

    public Map<String, String> getNodeGroupLabels() {
        return nodeGroupLabels;
    }

    public void setNodeGroupLabels(Map<String, String> nodeGroupLabels) {
        this.nodeGroupLabels = nodeGroupLabels;
    }
}
