package no.ssb.exploration;


import no.ssb.exploration.model.LineageObject;

public interface LineagePersistenceProvider {
    void save(LineageObject lineageObject);
}
