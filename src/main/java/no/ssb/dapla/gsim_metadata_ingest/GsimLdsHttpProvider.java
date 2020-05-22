package no.ssb.dapla.gsim_metadata_ingest;

import io.helidon.common.http.Http;
import io.helidon.common.http.MediaType;
import io.helidon.webclient.WebClient;
import io.helidon.webclient.WebClientResponse;
import no.ssb.dapla.dataset.doc.model.gsim.IdentifiableArtefact;
import no.ssb.dapla.dataset.doc.model.gsim.PersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;

public class GsimLdsHttpProvider implements PersistenceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GsimLdsHttpProvider.class);

    WebClient gsimLdsWebClient;

    public GsimLdsHttpProvider(WebClient gsimLdsWebClient) {
        this.gsimLdsWebClient = gsimLdsWebClient;
    }

    @Override
    public void save(IdentifiableArtefact identifiableArtefact) {
        String entityType = identifiableArtefact.getClass().getSimpleName();
        String path = String.format("%s/%s", entityType, identifiableArtefact.getId());
        LOG.info("Will post to path: {}", path);
        WebClientResponse response = gsimLdsWebClient.put()
                .path(path)
                .contentType(MediaType.APPLICATION_JSON)
                .readTimeout(30, ChronoUnit.SECONDS)
                .connectTimeout(30, ChronoUnit.SECONDS)
                .submit(identifiableArtefact)
                .toCompletableFuture()
                .join();

        System.out.println(response.content().as(String.class).toCompletableFuture().join());

        if (!Http.ResponseStatus.Family.SUCCESSFUL.equals(response.status().family())) {
            throw new RuntimeException(String.format("Got response code %d from GSIM LDS with reason: %s",
                    response.status().code(), response.status().reasonPhrase()));
        }
    }
}
