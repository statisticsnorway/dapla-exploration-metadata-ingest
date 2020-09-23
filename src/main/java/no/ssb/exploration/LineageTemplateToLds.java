package no.ssb.exploration;

import no.ssb.dapla.dataset.doc.model.lineage.Dataset;
import no.ssb.dapla.dataset.doc.model.lineage.Record;
import no.ssb.dapla.dataset.doc.model.lineage.Source;
import no.ssb.exploration.model.LineageDataSet;

import java.util.List;
import java.util.stream.Collectors;

public class LineageTemplateToLds {

    private final Dataset dataset;
    private final LineagePersistenceProvider lineagePersistenceProvider;

    public LineageTemplateToLds(Dataset dataset, LineagePersistenceProvider lineagePersistenceProvider) {
        this.dataset = dataset;
        this.lineagePersistenceProvider = lineagePersistenceProvider;
    }

    public void createLdsLinageObjects() {
        Record root = dataset.getRoot();

        List<Source> sources = root.getSources();
        List<String> lineageDatasets = getLineageDatasets(sources);

        LineageDataSet lineageDataSet = LdsLineageBuilder.create()
                .id("LineageDataSet-id") // TODO: create correct ID
                .lineageDataSet()
                .dataset("DataSet-id") // TODO: make sure we have a dataset. Reuse code to create from SimpleToGsim
                .lineageDataSets(lineageDatasets)
                .build();

        lineagePersistenceProvider.save(lineageDataSet);
    }

    private List<String> getLineageDatasets(List<Source> sources) {
        return sources.stream().map(Source::getDataset).collect(Collectors.toList());

    }

}
