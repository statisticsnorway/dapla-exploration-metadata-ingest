package no.ssb.dapla.exploration_metadata_ingest;

import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExplorationMetadataIngestService implements Service {

    private static final Logger LOG = LoggerFactory.getLogger(ExplorationMetadataIngestService.class);

    ExplorationMetadataIngestService() {
    }

    @Override
    public void update(Routing.Rules rules) {
        rules.get("/trigger", this::getRevisionHandler);
    }

    private void getRevisionHandler(ServerRequest request, ServerResponse response) {
        response.status(200).send();
    }
}
