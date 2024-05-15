package ai.utiliti.bes.config;

import ai.utiliti.bes.consumers.ConsumerVisibilityExtender;
import ai.utiliti.bes.util.GsonExclusionStrategy;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@EnableSqs
public class ApplicationConfiguration {

    @Bean
    AwsCredentials getAwsCredentials() {
        return AwsBasicCredentials.create(
                System.getenv("AWS_ACCESS_KEY_ID"),
                System.getenv("AWS_SECRET_ACCESS_KEY")
        );
    }

    @Bean
    @Primary
    public DataSource getDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(System.getenv("DATABASE_URL")).build();
    }

    @Bean
    public Web3j getWeb3J() {
        String url = System.getenv("NETWORK_HTTP_URL");
        return Web3j.build(new HttpService(url));
    }

    @Bean
    URI getLocalStackUri() {
        return URI.create(System.getenv("AWS_ENDPOINT_URI"));
    }

    @Bean
    public SQSConnectionFactory getSQSConnectionFactory() {
        return new SQSConnectionFactory(
                new ProviderConfiguration(),
                getSqsClient()
        );
    }

    @Bean
    public SqsClient getSqsClient() {
        return SqsClient
                .builder()
                .region(Region.of(System.getenv("SQS_REGION")))
                .endpointOverride(getLocalStackUri())
                .credentialsProvider(this::getAwsCredentials)
                .build();
    }

    @Bean
    @Primary
    AmazonSQSAsync sqsAsync() {
        return AmazonSQSAsyncClientBuilder.standard()
                .withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(System.getenv("AWS_ENDPOINT_URI"), System.getenv("SQS_REGION")
                ))
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .build();
    }

    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(AmazonSQSAsync amazonSqs) {
        SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();
        factory.setAmazonSqs(amazonSqs);
        factory.setAutoStartup(true);
        factory.setMaxNumberOfMessages(1);
        factory.setWaitTimeOut(10);
        factory.setBackOffTime(Long.valueOf(60000));
        return factory;
    }

    @Bean
    public Gson gson() {
        return new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setExclusionStrategies(new GsonExclusionStrategy(null))
            .create();
    }

    @Bean
    public ConsumerVisibilityExtender visibilityExtender() {
        return new ConsumerVisibilityExtender();
    }
}