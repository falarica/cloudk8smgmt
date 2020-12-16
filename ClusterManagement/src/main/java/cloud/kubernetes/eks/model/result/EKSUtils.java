package cloud.kubernetes.eks.model.result;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

public class EKSUtils {

    public static AWSStaticCredentialsProvider getAWSStaticCredentialsProvider(String accessKey,String secretKey){
        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKey, secretKey);
        return new AWSStaticCredentialsProvider(basicAWSCredentials);
    }
}
