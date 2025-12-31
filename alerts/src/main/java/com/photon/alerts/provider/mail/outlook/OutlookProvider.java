package com.photon.alerts.provider.mail.outlook;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.GraphServiceClient;
import com.photon.alerts.dto.AlertProviderResponse;
import com.photon.alerts.dto.EmailRequest;
import com.photon.alerts.provider.mail.MailProvider;
import okhttp3.Request;
import reactor.core.publisher.Mono;

import java.util.List;

public class OutlookProvider implements MailProvider {

    private final GraphServiceClient<Request> graphClient;

    public OutlookProvider(String clientId, String clientSecret, String tenantId, String tokenUrl) {
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();

        TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(List.of(tokenUrl), credential);

        this.graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(authProvider)
                .buildClient();
    }

    @Override
    public Mono<AlertProviderResponse> sendEmail(EmailRequest request) {
        Message message = new Message();
        message.subject = request.getSubject();

        ItemBody body = new ItemBody();
        body.contentType = BodyType.TEXT;
        body.content = request.getBody();
        message.body = body;

        Recipient recipient = new Recipient();
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.address = request.getRecipient();
        recipient.emailAddress = emailAddress;

        message.toRecipients = List.of(recipient);

        graphClient.me().sendMail(UserSendMailParameterSet
                        .newBuilder()
                        .withMessage(message)
                        .withSaveToSentItems(false)
                        .build())
                .buildRequest()
                .post();

        return Mono.empty();
    }
}