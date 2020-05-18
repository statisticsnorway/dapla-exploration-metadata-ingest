package no.ssb.pubsub;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ComputeEngineCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class RealPubSub implements PubSub {

    public static no.ssb.pubsub.RealPubSub createWithServiceAccountKeyCredentials(String serviceAccountKeyPath) {
        Path serviceAccountKeyFilePath = Paths.get(serviceAccountKeyPath);
        Credentials credentials;
        try {
            credentials = ServiceAccountCredentials.fromStream(
                    Files.newInputStream(serviceAccountKeyFilePath, StandardOpenOption.READ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new no.ssb.pubsub.RealPubSub(new CredentialsProvider() {
            @Override
            public Credentials getCredentials() throws IOException {
                return credentials;
            }
        });
    }

    public static no.ssb.pubsub.RealPubSub createWithComputeEngineCredentials() {
        GoogleCredentials credentials = ComputeEngineCredentials.create();
        return new no.ssb.pubsub.RealPubSub(new CredentialsProvider() {
            @Override
            public Credentials getCredentials() throws IOException {
                return credentials;
            }
        });
    }

    public static no.ssb.pubsub.RealPubSub createWithDefaultCredentials() {
        GoogleCredentials credentials = null;
        return new no.ssb.pubsub.RealPubSub(new CredentialsProvider() {
            @Override
            public Credentials getCredentials() throws IOException {
                return credentials;
            }
        });
    }

    final CredentialsProvider credentialsProvider;

    public RealPubSub(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    @Override
    public TopicAdminClient getTopicAdminClient() {
        try {
            return TopicAdminClient.create(
                    TopicAdminSettings.newBuilder()
                            .setCredentialsProvider(credentialsProvider)
                            .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SubscriptionAdminClient getSubscriptionAdminClient() {
        try {
            return SubscriptionAdminClient.create(
                    SubscriptionAdminSettings.newBuilder()
                            .setCredentialsProvider(credentialsProvider)
                            .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Publisher getPublisher(String projectId, String subscriptionName) {
        ProjectTopicName projectTopicName = ProjectTopicName.of(projectId, subscriptionName);
        try {
            return Publisher.newBuilder(projectTopicName)
                    .setCredentialsProvider(credentialsProvider)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Subscriber getSubscriber(String projectId, String subscriptionName, MessageReceiver messageReceiver) {
        ProjectSubscriptionName projectSubscriptionName = ProjectSubscriptionName.of(projectId, subscriptionName);
        return Subscriber.newBuilder(projectSubscriptionName, messageReceiver)
                .setCredentialsProvider(credentialsProvider)
                .build();
    }

    @Override
    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    @Override
    public void close() {
    }
}