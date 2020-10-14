package no.ssb.exploration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class JsonToFileProvider implements PersistenceProvider {

    ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private String folder;

    public JsonToFileProvider(String folder) {
        this.folder = folder;
    }

    @Override
    public void save(Map<String, List<LDSObject>> ldsObjectsByType) {
        for (Map.Entry<String, List<LDSObject>> entry : ldsObjectsByType.entrySet()) {
            for (LDSObject ldsObject : entry.getValue()) {
                String fileName = String.format("%s/%s_%s.json", folder, ldsObject.type, ldsObject.id, ldsObject.version.toString());
                try {
                    objectMapper.writeValue(Paths.get(fileName).toFile(), ldsObject);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
