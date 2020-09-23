package no.ssb.exploration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import no.ssb.exploration.model.IdentifiableArtefact;
import no.ssb.exploration.model.PersistenceProvider;

import java.io.IOException;
import java.nio.file.Paths;

public class JsonToFileProvider implements PersistenceProvider {

    ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private String folder;

    public JsonToFileProvider(String folder) {
        this.folder = folder;
    }

    @Override
    public void save(IdentifiableArtefact identifiableArtefact) {
        String simpleName = identifiableArtefact.getClass().getSimpleName();

        String fileName = String.format("%s/%s_%s.json", folder, simpleName, identifiableArtefact.getName());

        try {
            objectMapper.writeValue(Paths.get(fileName).toFile(), identifiableArtefact);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
