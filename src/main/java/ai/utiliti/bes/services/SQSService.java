package ai.utiliti.bes.services;

import ai.utiliti.bes.model.EventJob;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.nio.charset.StandardCharsets;

@Service
public class SQSService {

  private final SQSConnectionFactory connectionFactory;
  private final AwsCredentials credentials;

  @Autowired
  private Gson gson;

  @Autowired
  private SqsClient sqsClient;

  private final Logger logger = LoggerFactory.getLogger(SQSService.class);

  @Autowired
  public SQSService(AwsCredentials credentials, SQSConnectionFactory connectionFactory) {
    this.credentials = credentials;
    this.connectionFactory = connectionFactory;
  }


  public void sendObjectToQueue(String queueURL, Object message) {
      try {

          String jsonString = gson.toJson(message);
          byte[] utf8MessageBytes = jsonString.getBytes(StandardCharsets.UTF_8);

          SendMessageRequest request = SendMessageRequest
                  .builder()
                  .queueUrl(queueURL)
                  .messageGroupId("Default")
                  .messageBody(new String(utf8MessageBytes, StandardCharsets.UTF_8))
                  .build();

          sqsClient.sendMessage(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendEventJobToSQS(EventJob job) {

      try {
          String jsonString = gson.toJson(job);
          byte[] utf8MessageBytes = jsonString.getBytes(StandardCharsets.UTF_8);

          SendMessageRequest request = SendMessageRequest
            .builder()
            .queueUrl(System.getenv("EVENT_LOG_QUEUE_URL"))
            .messageBody(new String(utf8MessageBytes, StandardCharsets.UTF_8))
            .build();

          sqsClient.sendMessage(request);
      } catch (Exception e) {
          logger.error("Could not get queue " + System.getenv("EVENT_LOG_QUEUE_NAME"), e);
          e.printStackTrace();
      } catch (StackOverflowError e) {
        System.out.println("Stack overflow error in SQS Service");
        e.printStackTrace();
        System.exit(1);
      }
    }
}