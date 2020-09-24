package no.ssb.exploration;

import no.ssb.dapla.dataset.doc.model.lineage.Dataset;
import no.ssb.dapla.dataset.doc.model.lineage.Record;
import no.ssb.dapla.dataset.doc.model.lineage.Source;
import no.ssb.exploration.model.LineageDataSet;
import no.ssb.exploration.model.LineageObject;
import no.ssb.exploration.model.UnitDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LineageTemplateToExplorationLineage {

    private final Dataset lineageDataset;
    private final String path;
    private final String version;
    private final UnitDataSet unitDataSet;

    public LineageTemplateToExplorationLineage(Dataset lineageDataset, String path, String version, UnitDataSet unitDataSet) {
        this.lineageDataset = lineageDataset;
        this.path = path;
        this.version = version;
        this.unitDataSet = unitDataSet;
    }

    public List<LineageObject> createLdsLinageObjects() {
        List<LineageObject> result = new ArrayList<>();

        Record root = lineageDataset.getRoot();

        List<Source> sources = root.getSources();
        List<String> lineageDatasets = getLineageDatasets(sources);

        LineageDataSet lineageDataSet = LdsLineageBuilder.create()
                .id(path + "#" + version)
                .lineageDataSet()
                .dataset(unitDataSet)
                .lineageDataSets(lineageDatasets)
                .build();

        result.add(lineageDataSet);

        return result;
    }

    private List<String> getLineageDatasets(List<Source> sources) {
        return sources.stream().map(s -> s.getPath()).collect(Collectors.toList());
    }
}
