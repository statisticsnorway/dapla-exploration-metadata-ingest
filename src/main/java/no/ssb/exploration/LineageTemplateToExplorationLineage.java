package no.ssb.exploration;

import no.ssb.dapla.dataset.doc.model.lineage.Dataset;
import no.ssb.dapla.dataset.doc.model.lineage.Field;
import no.ssb.dapla.dataset.doc.model.lineage.Record;
import no.ssb.dapla.dataset.doc.model.lineage.Source;
import no.ssb.exploration.model.LineageDataset;
import no.ssb.exploration.model.LineageField;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Optional.ofNullable;

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
        return DatasetTools.lineageDatasetId(DatasetTools.datasetId(source.getPath()));
    }

    private String lineageDatasetId() {
        return DatasetTools.lineageDatasetId(datasetLDSObject.id);
    }

    public List<LDSObject> createLineageFieldLdsObjects(Map<String, LDSObject> instanceVariableById) {
        List<LDSObject> result = new LinkedList<>();
        FieldTraverser.depthFirstTraversal(lineageDataset.getRoot(), (ancestors, field) -> {
            if (field.getFields().size() > 0) {
                return; // not a leaf
            }
            String ancestorFieldName = StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(
                            ancestors.descendingIterator(),
                            Spliterator.ORDERED),
                    false)
                    .skip(1)
                    .map(Field::getName)
                    .collect(Collectors.joining("."));
            String qualifiedFieldName = ancestorFieldName.isBlank() ? field.getName() : String.join(".", ancestorFieldName, field.getName());
            String lineageFieldLdsId = DatasetTools.lineageFieldId(lineageDatasetId(), qualifiedFieldName);
            String lineageDatasetLink = "/LineageDataset/" + lineageDatasetId();
            List<String> lineageFieldLinks = getLineageFieldLinks(field.getSources());
            String instanceVariableId = DatasetTools.instanceVariableId(lineageDatasetId(), qualifiedFieldName);
            LDSObject instanceVariableLdsObject = instanceVariableById.get(instanceVariableId);
            LineageField lineageField = LineageField.newBuilder()
                    .id(lineageFieldLdsId)
                    .name(qualifiedFieldName)
                    .relationType(field.getType())
                    .confidence(field.getConfidence())
                    .instanceVariable(ofNullable(instanceVariableLdsObject)
                            .map(LDSObject::link)
                            .orElse(null)
                    )
                    .lineageDataset(lineageDatasetLink)
                    .lineage(lineageFieldLinks)
                    .build();
            result.add(new LDSObject("LineageField", lineageFieldLdsId, datasetLDSObject.version, () -> lineageField));
        });
        return result;
    }
}
