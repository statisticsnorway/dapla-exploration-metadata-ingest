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
    public void thatLineageFieldsAreComplete() throws JsonProcessingException {
        String datasetMetaJson = TestUtils.load("testdata/metadata-1/dataset-meta.json");
        String datasetDocJson = TestUtils.load("testdata/metadata-1/datadoc.json");
        String lineageJson = TestUtils.load("testdata/metadata-1/lineage.json");
        JsonNode datasetMetaNode = mapper.readTree(datasetMetaJson);
        JsonNode datasetDocNode = mapper.readTree(datasetDocJson);
        JsonNode lineageNode = mapper.readTree(lineageJson);
        ObjectNode dataNode = mapper.createObjectNode();
        dataNode.set("dataset-meta", datasetMetaNode);
        dataNode.set("dataset-doc", datasetDocNode);
        dataNode.set("dataset-lineage", lineageNode);

        MetadataHelper helper = new MetadataHelper(mapper, dataNode);
        List<LDSObject> lineageFields = helper.lineageFields();
        System.out.printf("LINKS:%n");
        for (LDSObject lineageFieldLdsObject : lineageFields) {
            System.out.printf("%s%n", lineageFieldLdsObject.link());
        }
    }
}
