package no.ssb.exploration.model;

public interface PersistenceProvider {
    void save(IdentifiableArtefact identifiableArtefact);
}
