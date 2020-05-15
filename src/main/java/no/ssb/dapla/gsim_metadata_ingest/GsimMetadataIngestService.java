/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.ssb.dapla.gsim_metadata_ingest;

import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GsimMetadataIngestService implements Service {

    private static final Logger LOG = LoggerFactory.getLogger(GsimMetadataIngestService.class);

    GsimMetadataIngestService() {
    }

    @Override
    public void update(Routing.Rules rules) {
        rules.put("/trigger", this::putRevisionHandler);
    }

    private void putRevisionHandler(ServerRequest request, ServerResponse response) {
        response.status(200).send();
    }
}
