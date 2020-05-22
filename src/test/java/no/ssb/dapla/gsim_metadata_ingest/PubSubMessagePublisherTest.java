package no.ssb.dapla.gsim_metadata_ingest;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import no.ssb.pubsub.EmulatorPubSub;
import no.ssb.pubsub.PubSub;
import org.junit.jupiter.api.Test;

public class PubSubMessagePublisherTest {

    @Test
    public void publishOneMessage() {
        String json = "{\n" +
                "  \"dataset-path\" : \"/path/to/dataset\",\n" +
                "  \"logical-record-root\" : {\n" +
                "    \"name\" : \"konto\",\n" +
                "    \"instanceVariables\" : [ {\n" +
                "      \"name\" : \"kontonummer\",\n" +
                "      \"description\" : \"vilkårlig lang sekvens av tegn inkludert aksenter og spesielle tegn fra standardiserte tegnsett\"\n" +
                "    }, {\n" +
                "      \"name\" : \"innskudd\",\n" +
                "      \"description\" : \"9 sifret nummer gitt de som er registrert i Enhetsregisteret.\"\n" +
                "    }, {\n" +
                "      \"name\" : \"gjeld\",\n" +
                "      \"description\" : \"en sum av penger i hele kroner brukt i en kontekst. Dette kan være en transaksjon, saldo o.l.\"\n" +
                "    } ],\n" +
                "    \"path\" : \"konto\"\n" +
                "  }\n" +
                "}\n";

        PubSub pubSub = new EmulatorPubSub("localhost", 8538);
        Publisher publisher = pubSub.getPublisher("dapla", "gsim-metadata-1");
        try {
            publisher.publish(PubsubMessage.newBuilder()
                    .setData(ByteString.copyFromUtf8(json))
                    .build());
        } finally {
            publisher.shutdown();
        }
    }
}