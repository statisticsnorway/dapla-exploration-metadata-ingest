package no.ssb.dapla.exploration_metadata_ingest;

import io.helidon.common.http.Http;
import io.helidon.common.http.MediaType;
import io.helidon.webclient.WebClient;
import io.helidon.webclient.WebClientResponse;
import no.ssb.dapla.dataset.doc.model.gsim.IdentifiableArtefact;
import no.ssb.dapla.dataset.doc.model.gsim.PersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;

public class ExplorationLdsHttpProvider implements PersistenceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ExplorationLdsHttpProvider.class);

    private final WebClient explorationLdsWebClient;

    public ExplorationLdsHttpProvider(WebClient explorationLdsWebClient) {
        this.explorationLdsWebClient = explorationLdsWebClient;
    }

    @Override
    public void save(IdentifiableArtefact identifiableArtefact) {
        String path = String.format("%s/%s", identifiableArtefact.getGsimName(), identifiableArtefact.getId());
        LOG.info("Posting gsim object {} to path: {}", identifiableArtefact.getGsimName(), identifiableArtefact.getId());
        WebClientResponse response = explorationLdsWebClient.put()
                .path(path)
                .contentType(MediaType.APPLICATION_JSON)
                .readTimeout(30, ChronoUnit.SECONDS)
                .connectTimeout(30, ChronoUnit.SECONDS)
                .submit(identifiableArtefact)
                .toCompletableFuture()
                .join();

        if (!Http.ResponseStatus.Family.SUCCESSFUL.equals(response.status().family())) {
            // To se error message from lds
            LOG.warn("Exploration LDS: {}", response.content().as(String.class).toCompletableFuture().join());
            throw new RuntimeException(String.format("Got response code %d from Exploration LDS with reason: %s",
                    response.status().code(), response.status().reasonPhrase()));
        }
    }
}
