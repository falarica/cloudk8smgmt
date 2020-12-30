package cloud.kubernetes.eks;

import cloud.kubernetes.eks.model.result.EKSUtils;
import cloud.kubernetes.eks.model.result.VPCResult;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.eks.AmazonEKS;
import com.amazonaws.services.eks.AmazonEKSClientBuilder;
import com.amazonaws.services.eks.model.*;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.*;
import com.amazonaws.services.lightsail.AmazonLightsail;
import com.amazonaws.services.lightsail.AmazonLightsailClientBuilder;
import com.amazonaws.services.lightsail.model.AccessDeniedException;
import com.amazonaws.services.lightsail.model.AccountSetupInProgressException;
import com.amazonaws.services.lightsail.model.GetRegionsRequest;
import com.amazonaws.services.lightsail.model.GetRegionsResult;
import com.amazonaws.services.lightsail.model.InvalidInputException;
import com.amazonaws.services.lightsail.model.OperationFailureException;
import com.amazonaws.services.lightsail.model.ServiceException;
import com.amazonaws.services.lightsail.model.UnauthenticatedException;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EKSCluster {
    private AmazonEKS eks;
    private AmazonIdentityManagement amazonIdentityManagement;
    private AmazonEC2 amazonEC2;
    private AmazonLightsail lightsailClient;


    public EKSCluster(String accessKey,String secretKey,String regionName){
        this.lightsailClient = AmazonLightsailClientBuilder.standard().withCredentials(EKSUtils.getAWSStaticCredentialsProvider(accessKey,secretKey)).withRegion(regionName).build();
        this.eks = AmazonEKSClientBuilder.standard().withCredentials(EKSUtils.getAWSStaticCredentialsProvider(accessKey,secretKey)).withRegion(regionName).build();
        this.amazonIdentityManagement = AmazonIdentityManagementClientBuilder.standard()
                .withCredentials(EKSUtils.getAWSStaticCredentialsProvider(accessKey,secretKey)).withRegion(regionName).build();
        this.amazonEC2 = AmazonEC2ClientBuilder.standard().withCredentials(EKSUtils.getAWSStaticCredentialsProvider(accessKey,secretKey)).withRegion(regionName).build();
    }



    /**
     * This method is used to create the EKS cluster on AWS based upon the input parameter
     * @param createClusterRequest
     * @return
     * @throws ResourceInUseException
     * @throws ResourceLimitExceededException
     * @throws InvalidParameterException
     * @throws ClientException
     * @throws ServerException
     * @throws ServiceUnavailableException
     * @throws UnsupportedAvailabilityZoneException
     */
    public CreateClusterResult createCluster(CreateClusterRequest createClusterRequest)  throws
            ResourceInUseException,
            ResourceLimitExceededException,
            InvalidParameterException,
            ClientException,
            ServerException,
            ServiceUnavailableException,
            UnsupportedAvailabilityZoneException
    {
        return eks.createCluster(createClusterRequest);
    }


    /**
     * This method is used to create node group based upon the input parameter for a EKS cluster
     * @param createNodegroupRequest
     * @return
     * @throws ResourceInUseException
     * @throws ResourceLimitExceededException
     * @throws InvalidRequestException
     * @throws InvalidParameterException
     * @throws ClientException
     * @throws ServerException
     * @throws ServiceUnavailableException
     */
    public CreateNodegroupResult addNodeGroupToCluster(CreateNodegroupRequest createNodegroupRequest) throws
            ResourceInUseException,
            ResourceLimitExceededException,
            InvalidRequestException,
            InvalidParameterException,
            ClientException,
            ServerException,
            ServiceUnavailableException
    {
        return eks.createNodegroup(createNodegroupRequest);
    }

    /**
     * This method is used to update the node group based upon the input parameter for a EKS cluster
     * @param updateNodegroupConfigRequest
     * @return
     * @throws ResourceInUseException
     * @throws ResourceLimitExceededException
     * @throws InvalidRequestException
     * @throws InvalidParameterException
     * @throws ClientException
     * @throws ServerException
     * @throws ServiceUnavailableException
     */
    public UpdateNodegroupConfigResult updateNodeGroup(UpdateNodegroupConfigRequest updateNodegroupConfigRequest) throws
            ResourceInUseException,
            ResourceLimitExceededException,
            InvalidRequestException,
            InvalidParameterException,
            ClientException,
            ServerException,
            ServiceUnavailableException
    {
        return eks.updateNodegroupConfig(updateNodegroupConfigRequest);
    }

    /**
     * This method is used to list EKS cluster
     * @return
     * @throws InvalidParameterException
     * @throws ClientException
     * @throws ServerException
     * @throws ServiceUnavailableException
     */
    public ListClustersResult listCluster() throws
            InvalidParameterException,
            ClientException,
            ServerException,
            ServiceUnavailableException
    {
        ListClustersRequest request = new ListClustersRequest();
        ListClustersResult listClustersResult =  eks.listClusters(request);
        return listClustersResult;
    }

    /**
     * This method is used to get the cluster status
     * @param clusterName
     * @return
     * @throws ResourceNotFoundException
     * @throws ClientException
     * @throws ServerException
     * @throws ServiceUnavailableException
     */
    public String getClusterStatus(String clusterName) throws
            ResourceNotFoundException,
            ClientException,
            ServerException,
            ServiceUnavailableException

    {
        DescribeClusterRequest describeClusterRequest = new DescribeClusterRequest().withName(clusterName);
        return eks.describeCluster(describeClusterRequest).getCluster().getStatus();
    }


    /**
     * This method is used to get the node group status from the EKS cluster
     * @param clusterName
     * @param nodeGroupName
     * @return
     * @throws InvalidParameterException
     * @throws ResourceNotFoundException
     * @throws ClientException
     * @throws ServerException
     * @throws ServiceUnavailableException
     */
    public String getNodeGroupStatus(String clusterName,String nodeGroupName) throws
            InvalidParameterException,
            ResourceNotFoundException,
            ClientException,
            ServerException,
            ServiceUnavailableException

    {
        DescribeNodegroupRequest describeNodegroupRequest = new DescribeNodegroupRequest()
                .withClusterName(clusterName)
                .withNodegroupName(nodeGroupName);
        DescribeNodegroupResult describeNodegroupResult = eks.describeNodegroup(describeNodegroupRequest);
        return describeNodegroupResult.getNodegroup().getStatus();
    }


    /**
     * This method is used to delete the EKS cluster
     * @param clusterName
     * @return
     * @throws ResourceInUseException
     * @throws ResourceNotFoundException
     * @throws ClientException
     * @throws ServerException
     * @throws ServiceUnavailableException
     */
    public DeleteClusterResult deleteCluster(String clusterName) throws
            ResourceInUseException,
            ResourceNotFoundException,
            ClientException,
            ServerException,
            ServiceUnavailableException

    {
        DeleteClusterRequest deleteClusterRequest = new DeleteClusterRequest();
        deleteClusterRequest.setName(clusterName);
        return eks.deleteCluster(deleteClusterRequest);
    }

    /**
     * This method is used to delete the node group in a EKS cluster
     * @param clusterName
     * @param nodeGroupName
     * @return
     */
    public DeleteNodegroupResult deleteNodeGroup(String clusterName,String nodeGroupName) throws
            ResourceInUseException,
            ResourceNotFoundException,
            InvalidParameterException,
            ClientException,
            ServerException,
            ServiceUnavailableException

    {
        DeleteNodegroupRequest deleteNodegroupRequest = new DeleteNodegroupRequest()
                .withClusterName(clusterName)
                .withNodegroupName(nodeGroupName);
        return eks.deleteNodegroup(deleteNodegroupRequest);
    }

    /**
     * Lists all roles
     * @return
     */

    public ListRolesResult listRoles(){
        return amazonIdentityManagement.listRoles();
    }

    /**
     * Lists all security groups
     * @return
     */
    public DescribeSecurityGroupsResult listSecurityGroups(){
        DescribeSecurityGroupsResult describeSecurityGroupsResult = amazonEC2.describeSecurityGroups();
        return describeSecurityGroupsResult;
    }

    public CreateSecurityGroupResult createSecurityGroup(String groupName,String groupDesc,String vpcId){
        CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest()
                .withDescription(groupDesc)
                .withGroupName(groupName)
                .withVpcId(vpcId);
        return amazonEC2.createSecurityGroup(createSecurityGroupRequest);
    }

    /**
     * Configure inbound rule for security group
     * @param securityGroupId
     * @param cidrIpRange
     * @param ipProtocol
     * @param fromPort
     * @param toPort
     * @return
     */
    public AuthorizeSecurityGroupIngressResult configureSecurityGroupInboundRules(String securityGroupId, String cidrIpRange, String ipProtocol, int fromPort, int toPort){
        if(cidrIpRange == null){
            cidrIpRange = "0.0.0.0/0";
        }
        IpRange ipRange = new IpRange().withCidrIp(cidrIpRange);
        IpPermission ipPermission = new IpPermission().withIpProtocol(ipProtocol)
                .withFromPort(fromPort)
                .withToPort(toPort)
                .withIpv4Ranges(ipRange)
                ;

        AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest()
                .withGroupId(securityGroupId)
                .withIpPermissions(ipPermission)
                ;
        return amazonEC2.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
    }

    /**
     * Configure outbound rule for security group
     * @param securityGroupId
     * @param cidrIpRange
     * @param ipProtocol
     * @param fromPort
     * @param toPort
     * @return
     */
    public AuthorizeSecurityGroupEgressResult configureSecurityGroupOutbountRules(String securityGroupId, String cidrIpRange, String ipProtocol, int fromPort, int toPort){
        if(cidrIpRange == null){
            cidrIpRange = "0.0.0.0/0";
        }
        IpRange ipRange = new IpRange().withCidrIp(cidrIpRange);
        IpPermission ipPermission = new IpPermission().withIpProtocol(ipProtocol)
                .withFromPort(fromPort)
                .withToPort(toPort)
                .withIpv4Ranges(ipRange)
                ;

        AuthorizeSecurityGroupEgressRequest authorizeSecurityGroupEgressRequest = new AuthorizeSecurityGroupEgressRequest()
                .withGroupId(securityGroupId)
                .withIpPermissions(ipPermission)
                ;
        return amazonEC2.authorizeSecurityGroupEgress(authorizeSecurityGroupEgressRequest);
    }

    /**
     * Lists all VPCs
     * @return
     */
    public List<VPCResult> listVPC(){
        List<VPCResult> resultList = new ArrayList<>();
        DescribeVpcsResult describeVpcsResult = amazonEC2.describeVpcs();
        List<Vpc> vpcList = describeVpcsResult.getVpcs();
        for (Vpc vpc:vpcList){
            VPCResult vpcResult = new VPCResult();
            vpcResult.setVpcId(vpc.getVpcId());
            if(vpc.isDefault()){
                vpcResult.setName("default");
            }else {
                List<Tag> tagList = vpc.getTags();
                for(Tag tag : tagList){
                    if(tag.getKey().equalsIgnoreCase("Name")){
                        vpcResult.setName(tag.getValue());
                        break;
                    }
                }
            }
            resultList.add(vpcResult);
        }
        return resultList;
    }

    /**
     * List subnets from given VPC id
     * @param vpcId
     * @return
     */
    public DescribeSubnetsResult listSubnetsFromVPC(String vpcId){
        DescribeSubnetsRequest describeSubnetsRequest = new DescribeSubnetsRequest().withFilters(
                new Filter().withName("vpc-id").withValues(vpcId)
        );
        DescribeSubnetsResult describeSubnetsResult = amazonEC2.describeSubnets(describeSubnetsRequest);
        return describeSubnetsResult;
    }


    /**
     * This method is used to update the kubernetes version in the cluster
     * Todo: This method needs to be tested.
     * @param updateClusterVersionRequest
     * @return
     * @throws InvalidParameterException
     * @throws ClientException
     * @throws ServerException
     * @throws ResourceInUseException
     * @throws ResourceNotFoundException
     * @throws InvalidRequestException
     */
    public UpdateClusterVersionResult updateCluster(UpdateClusterVersionRequest updateClusterVersionRequest) throws
            InvalidParameterException,
            ClientException,
            ServerException,
            ResourceInUseException,
            ResourceNotFoundException,
            InvalidRequestException
    {
        return eks.updateClusterVersion(updateClusterVersionRequest);
    }


    /**
     * This method is used to get the status of the fargated compute
     * @param clusterName
     * @param name
     * @return
     * @throws InvalidParameterException
     * @throws ClientException
     * @throws ServerException
     * @throws ResourceNotFoundException
     */
    public String getFargatedStatus(String clusterName,String name) throws
            InvalidParameterException,
            ClientException,
            ServerException,
            ResourceNotFoundException
    {
        DescribeFargateProfileRequest describeFargateProfileRequest = new DescribeFargateProfileRequest()
                                                                .withClusterName(clusterName)
                                                                .withFargateProfileName(name);
        return eks.describeFargateProfile(describeFargateProfileRequest).getFargateProfile().getStatus();

    }

    /**
     * This method is used to add the fargated compute to the kubernetes cluster
     * @param createFargateProfileRequest
     * @return
     * @throws InvalidParameterException
     * @throws InvalidRequestException
     * @throws ClientException
     * @throws ServerException
     * @throws ResourceLimitExceededException
     * @throws UnsupportedAvailabilityZoneException
     */
    public CreateFargateProfileResult addFargateCompute(CreateFargateProfileRequest createFargateProfileRequest) throws
            InvalidParameterException,
            InvalidRequestException,
            ClientException,
            ServerException,
            ResourceLimitExceededException,
            UnsupportedAvailabilityZoneException
    {
        return eks.createFargateProfile(createFargateProfileRequest);
    }


    public void listAMIImages(){

    }

    public void listInstanceTypes(){

    }

    public DescribeKeyPairsResult listKeyPairs(){
        return amazonEC2.describeKeyPairs();
    }
    ////////// Extra methods to be added ///////////
    /**
     * This method is used to get the cluster status
     * @param clusterName
     * @return
     * @throws ResourceNotFoundException
     * @throws ClientException
     * @throws ServerException
     * @throws ServiceUnavailableException
     */
    public Cluster getCluster(String clusterName) throws
            ResourceNotFoundException,
            ClientException,
            ServerException,
            ServiceUnavailableException

    {
        DescribeClusterRequest describeClusterRequest = new DescribeClusterRequest().withName(clusterName);
        return eks.describeCluster(describeClusterRequest).getCluster();
    }

    /**
     * This method is used to get the cluster status
     * @param clusterName
     * @return
     * @throws ResourceNotFoundException
     * @throws ClientException
     * @throws ServerException
     * @throws ServiceUnavailableException
     */
    public DescribeNodegroupResult getNodeGroup(String clusterName,String nodeGrpName) throws
            ResourceNotFoundException,
            ClientException,
            ServerException,
            ServiceUnavailableException

    {
        DescribeNodegroupRequest describeNodegroupRequest = new DescribeNodegroupRequest().withClusterName(clusterName).withNodegroupName(nodeGrpName);
        return eks.describeNodegroup(describeNodegroupRequest);
    }


    /**
     * Method to list down the kubernetes version
     * @return
     */
    public List<String> listKubernetesVersion(){
        return Arrays.asList("1.15","1.16","1.17","1.18");
    }

    /**
     *
     * @return
     */
    public GetRegionsResult getRegions() throws
            ServiceException,
            InvalidInputException,
            NotFoundException,
            OperationFailureException,
            AccessDeniedException,
            AccountSetupInProgressException,
            UnauthenticatedException {
        GetRegionsRequest getRegionsRequest = new GetRegionsRequest();
        return lightsailClient.getRegions(getRegionsRequest);
    }

    public static void main(String []arg){
        EKSCluster eksCluster = new EKSCluster("","","us-east-2");
        GetRegionsResult getRegionsResult = eksCluster.getRegions();
        System.out.println(getRegionsResult);
    }
}
