package no.ssb.exploration;

import no.ssb.exploration.model.DimensionalDataset;
import no.ssb.exploration.model.LineageDataSet;
import no.ssb.exploration.model.UnitDataSet;

import java.util.List;
import java.util.stream.Collectors;

public class LdsLineageBuilder {
    private LdsLineageBuilder() {
    }

    public static BaseBuilder create() {
        return new BaseBuilder();
    }

    public static class BaseBuilder {
        private String id;

        public BaseBuilder id(String id) {
            this.id = id;
            return this;
        }

        public LineageDataSetBuilder lineageDataSet() {
            return new LineageDataSetBuilder(this);
        }
    }

    public static class LineageDataSetBuilder {
        private BaseBuilder baseBuilder;

        public LineageDataSetBuilder(BaseBuilder baseBuilder) {
            this.baseBuilder = baseBuilder;
        }

        private LineageDataSet lineageDataSet = new LineageDataSet();

        public LineageDataSetBuilder dataset(no.ssb.exploration.model.Dataset dataset) {
            String datasetConcreteType;
            if (dataset instanceof UnitDataSet) {
                datasetConcreteType = "UnitDataSet";
            } else if (dataset instanceof DimensionalDataset) {
                datasetConcreteType = "DimensionalDataset";
            } else {
                throw new IllegalArgumentException("Unknown Dataset type: " + dataset.getClass().getName());
            }
            lineageDataSet.setDataset("/" + datasetConcreteType + "/" + dataset.getId());
            return this;
        }

        public LineageDataSetBuilder lineageDataSets(List<String> lineageDataSets) {
            List<String> lineage = lineageDataSets.stream().map(lr -> "/LineageDataSet/" + lr).collect(Collectors.toList());
            lineageDataSet.setLineage(lineage);
            return this;
        }

        public LineageDataSet build() {
            lineageDataSet.setId(baseBuilder.id);
            return lineageDataSet;
        }

    }
}

