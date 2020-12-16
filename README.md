# cloudk8smgmt
Most common APIs of AKS and EKS have been implemented in Java are as below:

AWS Elastic Kubernetes Service (EKS) APIs:
 - Create cluster
 - Get cluster status
 - List cluster
 - Update cluster
 - Delete cluster
 - Add node group to cluster
 - Add fargated compute to cluster
 - Get status of fargated compute
 - Update node group
 - Delete node group
 - Create security group
 - Configure inbound rules for security group
 - Configure outbound rules for security groups
 - List security group
 - List roles
 - List VPC
 - List subnet from VPC
 - List kubernentes version
 
Azure Azure Kubernetes Service (AKS) APIs:
 - Create cluster
 - List cluster
 - Get cluster by id
 - Create node pool
 - Scale node pool
 - Delete node pool
 - Delete resource group
 - List vNet
 - List kubernentes version
 - List network security group
 - Add inbound rule to network security group
 - Add outbound rule to network security group
 - List regions
 - List subscriptions


How to run:
 - git clone https://github.com/falarica/cloudk8smgmt.git
 - To run AKS tests:
    - Go to AKSClusterTest.java and set tenentId,clientId and secret and run test method
 - To run EKS tests:
    - Go to EKSClusterTest.java and set accessKey,secretKey and run test method
 - Run 'mvn clean install' to build the code and run tests

 
