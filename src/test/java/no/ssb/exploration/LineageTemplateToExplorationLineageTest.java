package no.ssb.exploration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.ssb.dapla.dataset.doc.model.lineage.Dataset;
import no.ssb.exploration.model.LDSObject;
import no.ssb.exploration.model.UnitDataSet;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


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
        Map<String, List<LDSObject>> ldsObjectsByType = new LinkedHashMap<>();
        lineageTemplateToLds.createLdsLinageObjects(ldsObjectsByType);

        final ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();

        for (Map.Entry<String, List<LDSObject>> typeEntries : ldsObjectsByType.entrySet()) {
            for (LDSObject ldsObject : typeEntries.getValue()) {
                try {
                    String json = objectWriter.writeValueAsString(ldsObject.get());
                    System.out.printf("%s/%s # %s%n%s%n", ldsObject.type, ldsObject.id, ldsObject.version.toString(), json);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}