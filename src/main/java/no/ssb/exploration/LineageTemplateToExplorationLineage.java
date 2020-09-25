package no.ssb.exploration;

import no.ssb.dapla.dataset.doc.model.lineage.Dataset;
import no.ssb.dapla.dataset.doc.model.lineage.Field;
import no.ssb.dapla.dataset.doc.model.lineage.Record;
import no.ssb.dapla.dataset.doc.model.lineage.Source;
import no.ssb.exploration.model.LineageDataset;
import no.ssb.exploration.model.LineageField;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class LineageTemplateToExplorationLineage {

    private final Dataset lineageDataset;
    private final LDSObject datasetLDSObject;

    public LineageTemplateToExplorationLineage(Dataset lineageDataset, LDSObject datasetLDSObject) {
        this.lineageDataset = lineageDataset;
        this.datasetLDSObject = datasetLDSObject;
    }

    public LDSObject createLineageDatasetLdsObject() {
        Record root = lineageDataset.getRoot();

        List<Source> sources = root.getSources();
        List<String> lineageDatasets = getLineageDatasetLinks(sources);

        LineageDataset lineageDataSet = LineageDataset.newBuilder()
                .id(lineageDatasetId())
                .lineage(lineageDatasets)
                .build();

        LDSObject lineageDataset = new LDSObject("LineageDataset", lineageDatasetId(), datasetLDSObject.version, () -> lineageDataSet);
        return lineageDataset;
    }

    private List<String> getLineageDatasetLinks(List<Source> sources) {
        return sources.stream()
                .map(source -> "/LineageDataset/" + lineageDatasetId(source))
                .collect(Collectors.toList());
    }

    private List<String> getLineageFieldLinks(List<Source> sources) {
        return sources.stream()
                .map(source -> "/LineageField/" + lineageDatasetId(source) + "$" + source.getField())
                .collect(Collectors.toList());
    }

    private String lineageDatasetId(Source source) {
        String datasetId = source.getPath().substring(1).replaceAll("/", ".") + "#" + source.getVersion();
        return datasetId;
    }

    private String lineageDatasetId() {
        return datasetLDSObject.id + "#" + datasetLDSObject.version.toInstant().toEpochMilli();
    }

    public List<LDSObject> createLineageFieldLdsObjects() {
        List<LDSObject> result = new LinkedList<>();
        FieldTraverser.depthFirstTraversal(lineageDataset.getRoot(), (anscestors, field) -> {
            if (field.getFields().size() > 0) {
                return; // not a leaf
            }
            String anscestorFieldName = anscestors.stream().map(Field::getName).collect(Collectors.joining("."));
            String qualifiedFieldName = String.join(".", anscestorFieldName, field.getName());
            String lineageFieldLdsId = lineageDatasetId() + "$" + qualifiedFieldName;
            String lineageDatasetLink = "/LineageDataset/" + lineageDatasetId();
            List<String> lineageFieldLinks = getLineageFieldLinks(field.getSources());
            LineageField lineageField = LineageField.newBuilder()
                    .id(lineageFieldLdsId)
                    .name(qualifiedFieldName)
                    .relationType(field.getType())
                    .confidence(field.getConfidence())
                    .instanceVariable(null) // TODO
                    .lineageDataset(lineageDatasetLink)
                    .lineage(lineageFieldLinks)
                    .build();
            result.add(new LDSObject("LineageField", lineageFieldLdsId, datasetLDSObject.version, () -> lineageField));
        });
        return result;
    }
}
