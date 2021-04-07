package no.ssb.dapla.dataset.doc.model.lineage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Dataset {
    @JsonProperty("lineage")
    Record root;

    public Record getRoot() {
        return root;
    }

    public void setRoot(Record root) {
        this.root = root;
    }
}
