package no.ssb.dapla.gsim_metadata_ingest;

import io.helidon.common.http.Http;
import io.helidon.common.http.MediaType;
import io.helidon.media.common.DefaultMediaSupport;
import io.helidon.webclient.WebClient;
import io.helidon.webclient.WebClientResponse;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

public class PostToGsimLdsTest {

    @Test
    void testGet() {
        WebClient webClient = WebClient.builder()
                .addMediaSupport(DefaultMediaSupport.create(true))
                .addMediaSupport(io.helidon.media.jackson.common.JacksonSupport.create())
                .baseUri("http://localhost:29091/ns/")
                .build();

        WebClientResponse response = webClient.get()
                .path("InstanceVariable/08c65586-04d2-47de-a92e-eecee07edc0c")
                .readTimeout(30, ChronoUnit.SECONDS)
                .connectTimeout(30, ChronoUnit.SECONDS)
                .submit()
                .toCompletableFuture()
                .join();

        String body = response.content().as(String.class).toCompletableFuture().join();
        System.out.println(body);

        if (!Http.ResponseStatus.Family.SUCCESSFUL.equals(response.status().family())) {
            throw new RuntimeException(String.format("Got response code %d from GSIM LDS with reason: %s",
                    response.status().code(), response.status().reasonPhrase()));
        }

        String body2 = webClient.get()
                .path("InstanceVariable/08c65586-04d2-47de-a92e-eecee07edc0c")
                .request(String.class)
                .toCompletableFuture()
                .join();

        System.out.println(body2);
    }

    @Test
    void testPost() {
        String body = "{\"administrativeStatus\":\"DRAFT\",\"createdBy\":\"obv\",\"createdDate\":\"2019-07-15T12:58:26.617Z\",\"dataStructureComponentType\":\"MEASURE\",\"id\":\"08c65586-04d2-47de-a92e-eecee07edc0c\",\"population\":\"/Population/Population_DUMMY\",\"representedVariable\":\"/RepresentedVariable/RepresentertVariable_DUMMY\",\"shortName\":\"fødselår\",\"validFrom\":\"2019-07-15T12:58:26.617Z\",\"version\":\"1.0.0\",\"versionValidFrom\":\"2019-07-15T12:58:26.617Z\",\"description\":[{\"languageCode\":\"nb\",\"languageText\":\"fødselår\"}],\"name\":[{\"languageCode\":\"nb\",\"languageText\":\"fødselår\"}]}";

        WebClient webClient = WebClient.builder()
                .addMediaSupport(DefaultMediaSupport.create(true))
                .addMediaSupport(io.helidon.media.jackson.common.JacksonSupport.create())
                .baseUri("http://localhost:29091/ns/")
                .build();

        WebClientResponse response = webClient.put()
                .path("InstanceVariable/test")
                .readTimeout(30, ChronoUnit.SECONDS)
                .connectTimeout(30, ChronoUnit.SECONDS)
                .contentType(MediaType.APPLICATION_JSON)
                .submit(body)
                .toCompletableFuture()
                .join();

        System.out.println(response.content().as(String.class).toCompletableFuture().join());

        if (!Http.ResponseStatus.Family.SUCCESSFUL.equals(response.status().family())) {
            throw new RuntimeException(String.format("Got response code %d from GSIM LDS with reason: %s",
                    response.status().code(), response.status().reasonPhrase()));
        }
    }
}
