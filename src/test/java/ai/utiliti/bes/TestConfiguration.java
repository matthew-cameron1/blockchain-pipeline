package ai.utiliti.bes;

import ai.utiliti.bes.services.SQSService;
import com.google.gson.Gson;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.sqs.SqsClient;

import javax.sql.DataSource;

@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {

    @Bean
    public DataSource getDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url("jdbc:postgresql://localhost/event-service?user=postgres&password=password").build();
    }

    @Bean
    public SQSService sqsService() {
        return Mockito.mock(SQSService.class);
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }

    @Bean
    public SqsClient sqsClient() {
        return Mockito.mock(SqsClient.class);
    }
}
