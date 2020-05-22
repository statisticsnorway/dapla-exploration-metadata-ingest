package no.ssb.dapla.exploration_metadata_ingest;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import no.ssb.pubsub.EmulatorPubSub;
import no.ssb.pubsub.PubSub;
import org.junit.jupiter.api.Test;

public class PubSubMessagePublisherTest {

    @Test
    public void publishOneMessage() {
        PubSub pubSub = new EmulatorPubSub("localhost", 8538);
        Publisher publisher = pubSub.getPublisher("dapla", "exploration-metadata-1");
        try {
            publisher.publish(PubsubMessage.newBuilder()
                    .setData(ByteString.copyFromUtf8("{\"test\":\"me\"}"))
                    .build());
        } finally {
            publisher.shutdown();
        }
    }
}