package no.ssb.exploration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.ssb.dapla.dataset.doc.model.lineage.Dataset;
import no.ssb.exploration.model.LineageObject;
import org.junit.jupiter.api.Test;


class LineageTemplateToLdsTest {

    @Test
    void test() throws JsonProcessingException {
        String json = TestUtils.load("testdata/lineage/one-level.json");
        Dataset dataset = new ObjectMapper().readValue(json, Dataset.class);

        LineagePersistenceProvider lineagePersistenceProvider = new LineagePersistenceProvider() {
            final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

            @Override
            public void save(LineageObject lineageObject) {
                try {
                    String json = objectWriter.writeValueAsString(lineageObject);
                    System.out.println(json);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        LineageTemplateToLds lineageTemplateToLds = new LineageTemplateToLds(dataset, lineagePersistenceProvider);
        lineageTemplateToLds.createLdsLinageObjects();
    }
}