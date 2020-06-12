package no.ssb.dapla.exploration_metadata_ingest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import no.ssb.pubsub.EmulatorPubSub;
import no.ssb.pubsub.PubSub;
import org.junit.jupiter.api.Test;

public class PubSubMessagePublisherTest {

    @Test
    public void publishOneMessage() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode dataNode = mapper.createObjectNode();
        dataNode.put("parentUri", "dummy");

        ObjectNode datasetMetaNode = dataNode.putObject("dataset-meta");
        {
            datasetMetaNode
                    .putObject("id")
                    .put("path", "/path/to/dataset");
            datasetMetaNode.put("createdBy", "PubSubMessagePublisherTest");
        }
        ObjectNode datasetDocNode = dataNode.putObject("dataset-doc");
        {
            ObjectNode logicalRecordRoot = datasetDocNode.putObject("logical-record-root");
            logicalRecordRoot.put("name", "konto");
            ArrayNode ivs = logicalRecordRoot.putArray("instanceVariables");
            ivs.addObject()
                    .put("name", "kontonummer")
                    .put("description", "vilkårlig lang sekvens av tegn inkludert aksenter og spesielle tegn fra standardiserte tegnsett");
            ivs.addObject()
                    .put("name", "innskudd")
                    .put("description", "9 sifret nummer gitt de som er registrert i Enhetsregisteret.");
            ivs.addObject()
                    .put("name", "gjeld")
                    .put("description", "en sum av penger i hele kroner brukt i en kontekst. Dette kan være en transaksjon, saldo o.l.");
            logicalRecordRoot.put("path", "konto");
        }

        PubSub pubSub = new EmulatorPubSub("localhost", 8538);
        Publisher publisher = pubSub.getPublisher("dapla", "exploration-metadata-1");
        try {
            publisher.publish(PubsubMessage.newBuilder()
                    .setData(ByteString.copyFromUtf8(mapper.writeValueAsString(dataNode)))
                    .build());
        } finally {
            publisher.shutdown();
        }
    }
}