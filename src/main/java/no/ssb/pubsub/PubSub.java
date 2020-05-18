package no.ssb.pubsub;

import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;

public interface PubSub {

    void close();

    TopicAdminClient getTopicAdminClient();

    SubscriptionAdminClient getSubscriptionAdminClient();

    Publisher getPublisher(String projectId, String topic);

    Subscriber getSubscriber(String projectId, String subscriptionName, MessageReceiver messageReceiver);

    CredentialsProvider getCredentialsProvider();
}