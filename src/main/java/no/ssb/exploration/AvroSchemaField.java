package no.ssb.exploration;

import java.util.Objects;

public class AvroSchemaField {
    final String qualifiedName;
    final String type;

    public AvroSchemaField(String qualifiedName, String type) {
        this.qualifiedName = qualifiedName;
        this.type = type;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvroSchemaField that = (AvroSchemaField) o;
        return qualifiedName.equals(that.qualifiedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualifiedName);
    }

    @Override
    public String toString() {
        return qualifiedName;
    }
}
