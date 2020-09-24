package no.ssb.dapla.exploration_metadata_ingest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.helidon.common.http.Http;
import io.helidon.common.http.MediaType;
import io.helidon.webclient.WebClient;
import io.helidon.webclient.WebClientResponse;
import no.ssb.exploration.model.LDSObject;
import no.ssb.exploration.model.PersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ExplorationLdsHttpProvider implements PersistenceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ExplorationLdsHttpProvider.class);

    private final WebClient explorationLdsWebClient;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public ExplorationLdsHttpProvider(WebClient explorationLdsWebClient) {
        this.explorationLdsWebClient = explorationLdsWebClient;
    }

    @Override
    public void save(LDSObject ldsObject) {
        String path = String.format("%s/%s", ldsObject.type, ldsObject.id);
        String json;
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ldsObject);
            LOG.info("PUT /{}/{}?timestamp={}\n{}", ldsObject.type, ldsObject.id, ldsObject.version.toString(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("", e);
        }
        WebClientResponse response = explorationLdsWebClient.put()
                .path(path)
                .queryParam("timestamp", ldsObject.version.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .submit(json)
                .toCompletableFuture()
                .join();

        if (!Http.ResponseStatus.Family.SUCCESSFUL.equals(response.status().family())) {
            // To see error message from lds
            String messageFrom = response.content().as(String.class).toCompletableFuture().join();
            LOG.warn("Exploration LDS: {}", messageFrom);
            throw new RuntimeException(String.format("Got response code %d from Exploration LDS with reason: %s",
                    response.status().code(), response.status().reasonPhrase()));
        }
    }
}
