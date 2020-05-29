package no.ssb.dapla.exploration_metadata_ingest;

import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import io.helidon.config.Config;
import no.ssb.pubsub.PubSub;
import no.ssb.pubsub.PubSubAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasetUpstreamGooglePubSubIntegrationInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetUpstreamGooglePubSubIntegrationInitializer.class);

    public static void initializeTopicsAndSubscriptions(Config pubSubUpstreamConfig, PubSub pubSub) {
        try (TopicAdminClient topicAdminClient = pubSub.getTopicAdminClient()) {
            try (SubscriptionAdminClient subscriptionAdminClient = pubSub.getSubscriptionAdminClient()) {
                String upstreamProjectId = pubSubUpstreamConfig.get("projectId").asString().get();
                String upstreamTopicName = pubSubUpstreamConfig.get("topic").asString().get();
                String upstreamSubscriptionName = pubSubUpstreamConfig.get("subscription").asString().get();
                int upstreamAckDeadlineSeconds = pubSubUpstreamConfig.get("ack-deadline-seconds").asInt().orElse(30);
                Config upstreamDlq = pubSubUpstreamConfig.get("dlq");
                String upstreamDlqProjectId = upstreamDlq.get("projectId").asString().orElse(null);
                String upstreamDlqTopic = upstreamDlq.get("topic").asString().orElse(null);
                int upstreamDlqMaxRedeliveryAttempts = upstreamDlq.get("max-redelivery-attempts").asInt().orElse(10);
                String upstreamDlqSubscription = upstreamDlq.get("subscription").asString().orElse(null);

                LOG.info("Initializing topic: {}", upstreamTopicName);
                PubSubAdmin.createTopicIfNotExists(topicAdminClient, upstreamProjectId, upstreamTopicName);

                if (upstreamDlqTopic != null) {

                    LOG.info("Initializing DLQ: {}", upstreamDlqTopic);
                    PubSubAdmin.createTopicIfNotExists(topicAdminClient, upstreamDlqProjectId, upstreamDlqTopic);

                    if (upstreamDlqSubscription != null) {
                        PubSubAdmin.createSubscriptionIfNotExists(subscriptionAdminClient, upstreamDlqProjectId,
                                upstreamDlqTopic, upstreamDlqSubscription, 60);
                    }
                }

                PubSubAdmin.createSubscriptionIfNotExists(subscriptionAdminClient, upstreamProjectId,
                        upstreamTopicName, upstreamSubscriptionName, upstreamAckDeadlineSeconds,
                        upstreamDlqMaxRedeliveryAttempts, upstreamDlqProjectId, upstreamDlqTopic);
            }
        }
    }
}
