package ai.utiliti.bes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.sesv2.SesV2AsyncClient;

import java.net.URI;

@Configuration
public class SesConfig {

    private final String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
    private final String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
    private final String endpoint = System.getenv("SES_ENDPOINT_URI");

    @Bean
    public SesV2AsyncClient sesClient() {
        return SesV2AsyncClient
                .builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        accessKey,
                        secretKey
                )))
                .build();
    }
}
