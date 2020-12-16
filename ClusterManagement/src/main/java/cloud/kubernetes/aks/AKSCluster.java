package cloud.kubernetes.aks;
import cloud.kubernetes.aks.dto.CreateAzureClusterRequest;
import cloud.kubernetes.aks.dto.CreateNodePoolRequest;
import cloud.kubernetes.aks.utils.SSHShell;
import cloud.kubernetes.aks.utils.Utils;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.fluent.ComputeManagementClient;
import com.azure.resourcemanager.containerservice.implementation.KubernetesClusterAgentPoolImpl;
import com.azure.resourcemanager.containerservice.implementation.KubernetesClusterNetworkProfileImpl;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.containerservice.models.NetworkPlugin;
import com.azure.resourcemanager.containerservice.models.OSType;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import com.azure.resourcemanager.resources.models.Subscription;
import com.jcraft.jsch.JSchException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AKSCluster {
    private AzureResourceManager azureResourceManager = null;
    private AzureResourceManager.Authenticated authenticated = null;
    private String clientId;
    private String secret;

    public AKSCluster(String tenentId, String clientId,String secret,String subscriptionId){
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new ClientSecretCredentialBuilder().tenantId(tenentId)
                                                                        .clientId(clientId)
                                                                        .clientSecret(secret)
                                                                        .build();
        this.authenticated = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile);

        if(subscriptionId == null)
        {
            this.azureResourceManager = this.authenticated.withDefaultSubscription();
        }else {
            this.azureResourceManager = this.authenticated.withSubscription(subscriptionId);
        }

        this.clientId = clientId;
        this.secret = secret;
    }

    /**
     * List available subscriptions of the user
     * @return
     */
    public Map<String,String> listSubscriptions(){
        Map<String,String> map = new LinkedHashMap<>();
        PagedIterable<Subscription> pagedIterable = this.authenticated.subscriptions().list();
        for (Subscription subscription:pagedIterable){
            map.put(subscription.subscriptionId(),subscription.displayName());
        }
        return map;
    }


    /**
     * List available AKS clusters
     * @return
     */
    public PagedIterable<KubernetesCluster>listClusters(){
        return azureResourceManager.kubernetesClusters().list();
    }

    /**
     * Create kubernetes cluster on Azure
     * @param createAzureClusterRequest
     * @return
     * @throws UnsupportedEncodingException
     * @throws JSchException
     */
    public KubernetesCluster createCluster(CreateAzureClusterRequest createAzureClusterRequest) throws UnsupportedEncodingException, JSchException {
        final String rootUserName = "aksuser";
        if(createAzureClusterRequest.getResourceGroupName() == null){
            createAzureClusterRequest.setResourceGroupName(Utils.randomResourceName(azureResourceManager, "rgaks", 15));
        }
        if(createAzureClusterRequest.getVirtualMachineSize() == null){
            createAzureClusterRequest.setVirtualMachineSize("Standard_D2_v2");
        }
        if(createAzureClusterRequest.getRegionName() == null){
            createAzureClusterRequest.setRegionName(Region.US_EAST.label());
        }
        if(createAzureClusterRequest.getOsType() == null){
            createAzureClusterRequest.setOsType(OSType.LINUX.toString());
        }
        if(createAzureClusterRequest.getAgentPoolName() == null){
            createAzureClusterRequest.setAgentPoolName("agentpool");
        }
        if(createAzureClusterRequest.getTagMap() == null){
            createAzureClusterRequest.setTagMap(new LinkedHashMap<String, String>());
        }
        if(createAzureClusterRequest.getOsDiskInGb() == 0){
            createAzureClusterRequest.setOsDiskInGb(30);
        }
        if(createAzureClusterRequest.getNetworkPluginName() == null){
            createAzureClusterRequest.setNetworkPluginName(NetworkPlugin.KUBENET.toString());
        }



        SSHShell.SshPublicPrivateKey sshKeys = SSHShell.generateSSHKeys("", "ACS");
        KubernetesClusterNetworkProfileImpl kubernetesClusterNetworkProfile = (KubernetesClusterNetworkProfileImpl)azureResourceManager.kubernetesClusters()
                .define(createAzureClusterRequest.getClusterName())
                .withRegion(Region.fromName(createAzureClusterRequest.getRegionName()))
                .withNewResourceGroup(createAzureClusterRequest.getResourceGroupName())
                .withDefaultVersion()
                .withRootUsername(rootUserName)
                .withSshKey(sshKeys.getSshPublicKey())
                .withServicePrincipalClientId(this.clientId)
                .withServicePrincipalSecret(this.secret)
                .defineAgentPool(createAzureClusterRequest.getAgentPoolName())
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.fromString(createAzureClusterRequest.getVirtualMachineSize()))
                .withAgentPoolVirtualMachineCount(createAzureClusterRequest.getMachineCountInPool())
                .withOSType(OSType.fromString(createAzureClusterRequest.getOsType()))
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .withOSDiskSizeInGB(createAzureClusterRequest.getOsDiskInGb())
                .attach()
                .withTags(createAzureClusterRequest.getTagMap())
                .withDnsPrefix("dns-" + createAzureClusterRequest.getClusterName())
                .defineNetworkProfile().withNetworkPlugin(NetworkPlugin.fromString(createAzureClusterRequest.getNetworkPluginName()));

        if(createAzureClusterRequest.getNetworkPluginName().equals(NetworkPlugin.AZURE.toString())){
            if(createAzureClusterRequest.getDnsServiceIp() != null){
                kubernetesClusterNetworkProfile = kubernetesClusterNetworkProfile.withDnsServiceIP(createAzureClusterRequest.getDnsServiceIp());
            }
            if(createAzureClusterRequest.getServiceAddressRange() != null){
                kubernetesClusterNetworkProfile = kubernetesClusterNetworkProfile.withServiceCidr(createAzureClusterRequest.getServiceAddressRange());
            }
            if(createAzureClusterRequest.getDockerBridgeAddress() != null){
                kubernetesClusterNetworkProfile = kubernetesClusterNetworkProfile.withDockerBridgeCidr(createAzureClusterRequest.getDockerBridgeAddress());
            }

        }
        KubernetesCluster kubernetesCluster = kubernetesClusterNetworkProfile.attach().create();
        return kubernetesCluster;
    }


    /**
     * Get kubernetes cluster by id
     * @param id
     * @return
     */
    public KubernetesCluster getClusterById(String id){
        return this.azureResourceManager.kubernetesClusters().getById(id);
    }


    /**
     * List regions for the user selection
     * @return
     */
    public Map<String,String> listRegions(){
        Map<String,String> regionMap = new LinkedHashMap();
        Collection<Region> regionCollection = Region.values();
        for(Region region:regionCollection){
            regionMap.put(region.name(),region.label());
        }
        return regionMap;
    }

    /**
     * Delete resource group along with the kubernetes cluster
     * @param resourceGroupName
     */
    public void deleteResourceGroup(String resourceGroupName){
        azureResourceManager.resourceGroups().deleteByName(resourceGroupName);
    }

    /**
     * Create node pool for existing kubernetes cluster
     * @param createNodePoolRequest
     */
    public KubernetesCluster createNodePool(CreateNodePoolRequest createNodePoolRequest){
        if(createNodePoolRequest.getOsDiskInGb() == 0){
            createNodePoolRequest.setOsDiskInGb(30);
        }
        if(createNodePoolRequest.getOsType() == null){
            createNodePoolRequest.setOsType(OSType.LINUX.toString());
        }
        if(createNodePoolRequest.getVirtualMachineSize() == null){
            createNodePoolRequest.setVirtualMachineSize("Standard_D2_v2");
        }
        KubernetesCluster kubernetesCluster = this.azureResourceManager.kubernetesClusters().getById(createNodePoolRequest.getClusterId())
                .update()
                .defineAgentPool(createNodePoolRequest.getPoolName())
                .withVirtualMachineSize(ContainerServiceVMSizeTypes.fromString(createNodePoolRequest.getVirtualMachineSize()))
                .withAgentPoolVirtualMachineCount(createNodePoolRequest.getMachineCountInPool())
                .withOSType(OSType.fromString(createNodePoolRequest.getOsType()))
                .withAgentPoolMode(AgentPoolMode.SYSTEM)
                .withOSDiskSizeInGB(createNodePoolRequest.getOsDiskInGb())
                .attach().apply();
        return kubernetesCluster;
    }


    /**
     * ToDO : To be implemented
     * @param id
     */
    public void upgradeClusterKubernetesVersion(String id){
        throw new UnsupportedOperationException();
    }

    /**
     * List down kubernetes version by passing region
     * @param region
     * @return
     */
    public Set<String> listKubernetesVersions(Region region){
        return this.azureResourceManager.kubernetesClusters().listKubernetesVersions(region);
    }

    /**
     * Scale existing node pool by adding more machines
     * @param clusterId
     * @param poolName
     * @param machineCount
     */
    public void scaleNodePool(String clusterId,String poolName, int machineCount){
        KubernetesClusterAgentPoolImpl kubernetesClusterAgentPool= (KubernetesClusterAgentPoolImpl)this.azureResourceManager.kubernetesClusters()
                .getById(clusterId)
                .update()
                .updateAgentPool(poolName)
                .withAgentPoolVirtualMachineCount(machineCount);
        kubernetesClusterAgentPool.attach().apply();
    }

    /**
     * Listing of VNet networks
     * @return
     */
    public Map<String,String> listVnet(){
        Map<String,String> networkMap = new LinkedHashMap<>();
        for(Network network: this.azureResourceManager.networks().list()){
            networkMap.put(network.id(),network.name());
        }
        return networkMap;
    }

    /**
     * Create inbound rule for network security group .
     * @param securityGroup
     * @param ruleName
     * @param securityRuleProtocol
     * @param toPort
     */
    public void addInboundSecurityGroupRule(String securityGroup, String ruleName, SecurityRuleProtocol securityRuleProtocol,
                                            int toPort){
        this.azureResourceManager.networkSecurityGroups()
                .getById(securityGroup)
                .update()
                .defineRule(ruleName)
                .allowInbound()
                .fromAnyAddress()
                .fromAnyPort()
                .toAnyAddress()
                .toPort(toPort)
                .withProtocol(securityRuleProtocol)
                .attach().apply();
    }

    /**
     * Create outbound rule for network security group .
     * @param securityGroup
     * @param ruleName
     * @param port
     * @param securityRuleProtocol
     */
    public void addOutboundSecurityGroupRule(String securityGroup,String ruleName, SecurityRuleProtocol securityRuleProtocol,
                                             int port){
        this.azureResourceManager.networkSecurityGroups()
                .getById(securityGroup)
                .update()
                .defineRule(ruleName)
                .allowOutbound()
                .fromAnyAddress()
                .fromPort(port)
                .toAnyAddress()
                .toAnyPort()
                .withProtocol(securityRuleProtocol)
                .attach().apply();
    }


    /**
     * List network security groups
     * @return
     */
    public Map<String,String> listNetworkSecurityGroup(){
        PagedIterable<NetworkSecurityGroup> networkSecurityGroupPagedIterable = this.azureResourceManager.networkSecurityGroups().list();
        Map<String,String> securityGroupMap = new LinkedHashMap<>();
        for (NetworkSecurityGroup networkSecurityGroup:networkSecurityGroupPagedIterable){
            securityGroupMap.put(networkSecurityGroup.id(),networkSecurityGroup.name());
        }
        return securityGroupMap;
    }

}