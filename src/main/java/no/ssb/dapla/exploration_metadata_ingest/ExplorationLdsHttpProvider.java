package no.ssb.dapla.exploration_metadata_ingest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.helidon.common.http.Http;
import io.helidon.common.http.MediaType;
import io.helidon.webclient.WebClient;
import io.helidon.webclient.WebClientResponse;
import no.ssb.exploration.LDSObject;
import no.ssb.exploration.PersistenceProvider;
import no.ssb.exploration.model.UnitDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ExplorationLdsHttpProvider implements PersistenceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ExplorationLdsHttpProvider.class);

    private final WebClient explorationLdsWebClient;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final String ldsNamespace;

    public ExplorationLdsHttpProvider(WebClient explorationLdsWebClient, String ldsNamespace) {
        this.explorationLdsWebClient = explorationLdsWebClient;
        this.ldsNamespace = ldsNamespace;
    }

    @Override
    public void save(Map<String, List<LDSObject>> ldsObjectsByType) {
        ArrayNode batchNode = mapper.createArrayNode();
        for (Map.Entry<String, List<LDSObject>> entry : ldsObjectsByType.entrySet()) {
            ObjectNode groupNode = batchNode.addObject();
            groupNode.put("operation", "put");
            groupNode.put("type", entry.getKey());
            ArrayNode entries = groupNode.putArray("entries");
            for (LDSObject ldsObject : entry.getValue()) {
                ObjectNode entryNode = entries.addObject();
                entryNode.put("id", ldsObject.id());
                entryNode.put("timestamp", ldsObject.version.toString());
                ObjectNode data = mapper.convertValue(ldsObject.get(), ObjectNode.class);
                entryNode.set("data", data);
            }
        }
        LDSObject unitDataSetLdsObject = ldsObjectsByType.get("UnitDataSet").get(0);
        UnitDataSet unitDataSet = unitDataSetLdsObject.get(UnitDataSet.class);
        for (String type : List.of("InstanceVariable", "LogicalRecord", "LineageField")) {
            List<LDSObject> ldsObjects = ldsObjectsByType.get(type);
            if (ldsObjects == null || ldsObjects.isEmpty()) {
                continue;
            }
            ObjectNode groupNode = batchNode.addObject();
            groupNode.put("operation", "delete");
            groupNode.put("type", type);
            groupNode.put("timestamp", ldsObjects.get(0).version.toString());
            ObjectNode matchNode = groupNode.putObject("match");
            ArrayNode andNode = matchNode.putArray("and");
            ObjectNode and1Node = andNode.addObject();
            and1Node.put("id-starts-with", unitDataSet.getId());
            ObjectNode and2Node = andNode.addObject();
            ArrayNode idNotInNode = and2Node.putArray("id-not-in");
            for (LDSObject ldsObject : ldsObjects) {
                idNotInNode.add(ldsObject.id());
            }
        }

        String path = String.format("/batch/%s", ldsNamespace);
        String json;
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(batchNode);
            LOG.debug("PUT {}\n{}", path, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("", e);
        }
        String sourceId = unitDataSet.getDataSourcePath() + "/" + unitDataSetLdsObject.version.toInstant().toEpochMilli();
        WebClientResponse response = explorationLdsWebClient.put()
                .skipUriEncoding()
                .path(path)
                .queryParam("sourceId", sourceId)
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
