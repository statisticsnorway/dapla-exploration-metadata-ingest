package no.ssb.pubsub;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannel;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class EmulatorPubSub implements PubSub {
    final String host;
    final int port;
    final ManagedChannel pubSubChannel;
    final FixedTransportChannelProvider channelProvider;
    final CredentialsProvider credentialsProvider;

    public EmulatorPubSub(String host, int port) {
        this.host = host;
        this.port = port;
        pubSubChannel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(pubSubChannel));
        credentialsProvider = NoCredentialsProvider.create();
    }

    @Override
    public TopicAdminClient getTopicAdminClient() {
        try {
            return TopicAdminClient.create(
                    TopicAdminSettings.newBuilder()
                            .setTransportChannelProvider(channelProvider)
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
                            .setTransportChannelProvider(channelProvider)
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
                    .setChannelProvider(channelProvider)
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
                .setChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build();
    }

    @Override
    public void close() {
        try {
            TransportChannel channel = channelProvider.getTransportChannel();
            channel.shutdown();
            if (!channel.awaitTermination(3, TimeUnit.SECONDS)) {
                channel.shutdownNow();
                if (!channel.awaitTermination(3, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Unable to close channel");
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public ManagedChannel getPubSubChannel() {
        return pubSubChannel;
    }

    public FixedTransportChannelProvider getChannelProvider() {
        return channelProvider;
    }

    @Override
    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }
}