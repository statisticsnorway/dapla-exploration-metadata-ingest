package no.ssb.exploration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.ssb.dapla.dataset.doc.model.lineage.Dataset;
import no.ssb.exploration.model.UnitDataSet;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;


class LineageTemplateToExplorationLineageTest {

    @Test
    void test() throws JsonProcessingException {
        String templateLineageJson = TestUtils.load("testdata/lineage/one-level.json");
        final ObjectMapper objectMapper = new ObjectMapper();

        Dataset dataset = objectMapper.readValue(templateLineageJson, Dataset.class);

        UnitDataSet unitDataSet = new UnitDataSet();
        unitDataSet.setId("path.to.dataset.1");
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        LDSObject datasetLdsObject = new LDSObject("UnitDataSet", unitDataSet.getId(), nowUtc, () -> unitDataSet);
        LineageTemplateToExplorationLineage lineageTemplateToLds = new LineageTemplateToExplorationLineage(dataset, datasetLdsObject);
        LDSObject lineageDatasetLdsObject = lineageTemplateToLds.createLineageDatasetLdsObject();
        List<LDSObject> lineageFieldLdsObject = lineageTemplateToLds.createLineageFieldLdsObjects(Collections.emptyMap());

        final ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();

        String lineageDatasetJson = objectWriter.writeValueAsString(lineageDatasetLdsObject.get());
        System.out.printf("%s/%s # %s%n%s%n", lineageDatasetLdsObject.type, lineageDatasetLdsObject.id, lineageDatasetLdsObject.version.toString(), lineageDatasetJson);

        for (LDSObject lineageFielLdsObject : lineageFieldLdsObject) {
            try {
                String json = objectWriter.writeValueAsString(lineageFielLdsObject.get());
                System.out.printf("%s/%s # %s%n%s%n", lineageFielLdsObject.type, lineageFielLdsObject.id, lineageFielLdsObject.version.toString(), json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}