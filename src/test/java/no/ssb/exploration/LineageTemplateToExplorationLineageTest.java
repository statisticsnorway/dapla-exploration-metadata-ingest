package no.ssb.exploration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.ssb.dapla.dataset.doc.model.lineage.Dataset;
import no.ssb.exploration.model.LineageObject;
import no.ssb.exploration.model.UnitDataSet;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
        LineageTemplateToExplorationLineage lineageTemplateToLds = new LineageTemplateToExplorationLineage(dataset, unitDataSet.getId(), nowUtc.toString(), unitDataSet);
        List<LineageObject> ldsLinageObjects = lineageTemplateToLds.createLdsLinageObjects();

        final ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();

        for (LineageObject lineageObject : ldsLinageObjects) {
            try {
                String json = objectWriter.writeValueAsString(lineageObject);
                System.out.println(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}