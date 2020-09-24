package no.ssb.exploration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import no.ssb.dapla.dataset.doc.model.simple.Record;
import no.ssb.exploration.model.LDSObject;
import no.ssb.exploration.model.LogicalRecord;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleToGsimTest {
    final private static String TEST_DATA_FOLDER = "testdata/gsim_files";

    String json = "{\n" +
            "  \"name\" : \"root\",\n" +
            "  \"logicalRecords\" : [ {\n" +
            "    \"name\" : \"person\",\n" +
            "    \"logicalRecords\" : [ {\n" +
            "      \"name\" : \"address\",\n" +
            "      \"logicalRecords\" : [ ],\n" +
            "      \"instanceVariables\" : [ {\n" +
            "        \"name\" : \"street\",\n" +
            "        \"description\" : \"street\"\n" +
            "      }, {\n" +
            "        \"name\" : \"postcode\",\n" +
            "        \"description\" : \"postcode\"\n" +
            "      } ]\n" +
            "    } ],\n" +
            "    \"instanceVariables\" : [ {\n" +
            "      \"name\" : \"name\",\n" +
            "      \"description\" : \"name\"\n" +
            "    }, {\n" +
            "      \"name\" : \"sex\",\n" +
            "      \"description\" : \"sex\"\n" +
            "    } ]\n" +
            "  } ],\n" +
            "  \"instanceVariables\" : [ {\n" +
            "    \"name\" : \"group\",\n" +
            "    \"description\" : \"group\"\n" +
            "  } ]\n" +
            "}\n";

    @Test
    void createGsimObjectsFor2Levels_AndWriteToFiles() throws JsonProcessingException {
        Record root = new ObjectMapper().readValue(json, Record.class);

        // to generate files
        //new File(TEST_DATA_FOLDER).mkdirs();
        //new SimpleToGsim(root, new JsonToFileProvider(TEST_DATA_FOLDER)).createGsimObjects();

        Map<String, List<LDSObject>> ldsObjectsByType = new LinkedHashMap<>();
        new SimpleToGsim(root, "/path/to/dataset", ZonedDateTime.parse("2020-01-01T00:00Z")).createGsimObjects(ldsObjectsByType);

        for (Map.Entry<String, List<LDSObject>> typeEntries : ldsObjectsByType.entrySet()) {
            for (LDSObject ldsObject : typeEntries.getValue()) {
                String fileName = String.format("testdata/gsim_2_levels/%s_%s.json", ldsObject.type, ldsObject.id);
                String expected = TestUtils.load(fileName);
                assertThat(getJson(ldsObject)).isEqualTo(expected);
            }
        }
    }

    @Test
    void createGsimObjectsForZeroLevels() throws JsonProcessingException {
        String json = "{\n" +
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
                "    } ]\n" +
                "  }\n" +
                "}\n";

        Record root = new ObjectMapper().readValue(json, Record.class);
        List<String> list = Arrays.asList(
                "/InstanceVariable/path.to.dataset.konto.kontonummer",
                "/InstanceVariable/path.to.dataset.konto.innskudd",
                "/InstanceVariable/path.to.dataset.konto.gjeld");

        Queue<String> paths = new LinkedList<>();
        paths.add("path.to.dataset.konto");
        paths.add("path.to.dataset.konto.kontonummer");
        paths.add("path.to.dataset.konto.innskudd");
        paths.add("path.to.dataset.konto.gjeld");

        Queue<String> gsimNames = new LinkedList<>();
        gsimNames.add("LogicalRecord");
        gsimNames.add("InstanceVariable");
        gsimNames.add("InstanceVariable");
        gsimNames.add("InstanceVariable");


        Map<String, List<LDSObject>> ldsObjectsByType = new LinkedHashMap<>();
        new SimpleToGsim(root, "/path/to/dataset", ZonedDateTime.parse("2020-01-01T00:00Z")).createGsimObjects(ldsObjectsByType);

        for (Map.Entry<String, List<LDSObject>> typeEntries : ldsObjectsByType.entrySet()) {
            for (LDSObject ldsObject : typeEntries.getValue()) {
                String fileName = String.format("testdata/gsim_1_level/%s_%s.json", ldsObject.type, ldsObject.id);
                String expected = TestUtils.load(fileName);
                assertThat(getJson(ldsObject)).isEqualTo(expected);

                if (ldsObject.get() instanceof LogicalRecord) {
                    List<String> instanceVariables = ((LogicalRecord) ldsObject.get()).getInstanceVariables();
                    assertThat(instanceVariables).isEqualTo(list);
                }
                assertThat(ldsObject.type).isEqualTo(gsimNames.remove());
                assertThat(ldsObject.id).isEqualTo(paths.remove());
            }
        }

        assertThat(paths).isEmpty();
        assertThat(gsimNames).isEmpty();
    }

    @Test
    void checkFullFormatSimpleToGsim() throws JsonProcessingException {
        String json = TestUtils.load("testdata/template/simple.json");
        Record root = new ObjectMapper().readValue(json, Record.class);

        Map<String, List<LDSObject>> ldsObjectsByType = new LinkedHashMap<>();
        new SimpleToGsim(root, "/path/to/dataset", ZonedDateTime.parse("2020-01-01T00:00Z")).createGsimObjects(ldsObjectsByType);

        for (Map.Entry<String, List<LDSObject>> typeEntries : ldsObjectsByType.entrySet()) {
            for (LDSObject ldsObject : typeEntries.getValue()) {
                String fileName = String.format("testdata/template/gsim_result/%s_%s.json", ldsObject.type, ldsObject.id);
                String expected = TestUtils.load(fileName);
                assertThat(getJson(ldsObject)).isEqualTo(expected);
            }
        }
    }

    String getJson(LDSObject ldsObject) {
        try {
            return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                    .writeValueAsString(ldsObject.get());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}