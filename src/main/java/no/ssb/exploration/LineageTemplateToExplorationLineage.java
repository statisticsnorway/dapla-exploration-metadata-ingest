package no.ssb.exploration;

import no.ssb.dapla.dataset.doc.model.lineage.Dataset;
import no.ssb.dapla.dataset.doc.model.lineage.Record;
import no.ssb.dapla.dataset.doc.model.lineage.Source;
import no.ssb.exploration.model.LDSObject;
import no.ssb.exploration.model.LineageDataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LineageTemplateToExplorationLineage {

    private final Dataset lineageDataset;
    private final LDSObject datasetLDSObject;

    public LineageTemplateToExplorationLineage(Dataset lineageDataset, LDSObject datasetLDSObject) {
        this.lineageDataset = lineageDataset;
        this.datasetLDSObject = datasetLDSObject;
    }

    public LDSObject createLdsLinageObjects(Map<String, List<LDSObject>> ldsObjectsByType) {
        Record root = lineageDataset.getRoot();

        List<Source> sources = root.getSources();
        List<String> lineageDatasets = getLineageDatasets(sources);

        LineageDataset lineageDataSet = LdsLineageBuilder.create()
                .id(datasetLDSObject.id + "#" + datasetLDSObject.version.toInstant().toEpochMilli())
                .lineageDataSet()
                .lineageDataSets(lineageDatasets)
                .build();

        LDSObject result = new LDSObject("LineageDataset", lineageDataSet.getId(), datasetLDSObject.version, () -> lineageDataSet);
        ldsObjectsByType.computeIfAbsent("LineageDataset", k -> new ArrayList<>())
                .add(result);

        return result;
    }

    private List<String> getLineageDatasets(List<Source> sources) {
        return sources.stream().map(s -> s.getPath().substring(1).replaceAll("/", ".") + "#" + s.getVersion()).collect(Collectors.toList());
    }
}
