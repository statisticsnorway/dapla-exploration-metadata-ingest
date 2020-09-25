package no.ssb.exploration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Paths;

public class JsonToFileProvider implements PersistenceProvider {

    ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private String folder;

    public JsonToFileProvider(String folder) {
        this.folder = folder;
    }

    @Override
    public void save(LDSObject ldsObject) {
        String fileName = String.format("%s/%s_%s.json", folder, ldsObject.type, ldsObject.id, ldsObject.version.toString());

        try {
            objectMapper.writeValue(Paths.get(fileName).toFile(), ldsObject);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
