package no.ssb.exploration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MetadataHelperTest {

    static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void thatMetadata1Works() throws JsonProcessingException {
        runMetadataTest("testdata/metadata-1");
    }

    @Test
    public void thatMetadata2Works() throws JsonProcessingException {
        runMetadataTest("testdata/metadata-2");
    }

    @Test
    public void thatMetadata3Works() throws JsonProcessingException {
        runMetadataTest("testdata/metadata-3");
    }

    private void runMetadataTest(String testDataFolder) throws JsonProcessingException {
        String datasetMetaJson = TestUtils.load(testDataFolder + "/dataset-meta.json");
        String datasetDocJson = TestUtils.load(testDataFolder + "/datadoc.json");
        String lineageJson = TestUtils.load(testDataFolder + "/lineage.json");
        String avroSchemaJson = TestUtils.load(testDataFolder + "/avro-schema.json");
        JsonNode datasetMetaNode = mapper.readTree(datasetMetaJson);
        JsonNode datasetDocNode = mapper.readTree(datasetDocJson);
        JsonNode lineageNode = mapper.readTree(lineageJson);
        JsonNode avroSchemaNode = mapper.readTree(avroSchemaJson);
        ObjectNode dataNode = mapper.createObjectNode();
        dataNode.set("dataset-meta", datasetMetaNode);
        dataNode.set("dataset-doc", datasetDocNode);
        dataNode.set("dataset-lineage", lineageNode);
        dataNode.set("avro-schema", avroSchemaNode);

        MetadataHelper helper = new MetadataHelper(mapper, dataNode);
        List<LDSObject> lineageFields = helper.lineageFields();
        System.out.printf("LINKS:%n");
        for (LDSObject lineageFieldLdsObject : lineageFields) {
            System.out.printf("%s%n", lineageFieldLdsObject.link());
        }
    }
}
