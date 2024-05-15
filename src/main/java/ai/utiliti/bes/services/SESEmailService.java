package ai.utiliti.bes.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2AsyncClient;
import software.amazon.awssdk.services.sesv2.model.*;

@Service
public class SESEmailService implements EmailService {

    private final SesV2AsyncClient sesClient;

    @Autowired
    public SESEmailService(SesV2AsyncClient sesClient) {
        this.sesClient = sesClient;
    }

    @Override
    public void sendEmail(String to, String subject, String message) {

        Destination destination = Destination
                .builder()
                .toAddresses(to)
                .build();

        Content content = Content.builder()
                .data(message)
                .build();

        Content subjectContent = Content.builder()
                .data(subject)
                .build();

        Body body = Body.builder()
                .text(content)
                .build();

        Message simpleMessage = Message
                .builder()
                .subject(subjectContent)
                .body(body)
                .build();

        EmailContent emailContent = EmailContent
                .builder()
                .simple(simpleMessage)
                .build();

        SendEmailRequest request = SendEmailRequest
                .builder()
                .destination(destination)
                .content(emailContent)
                .fromEmailAddress("notifications@utiliti.ai")
                .build();

        try {
                sesClient.sendEmail(request);
        } catch (SesV2Exception e) {
                System.err.println(e.awsErrorDetails().errorMessage());
        }
    }
}
