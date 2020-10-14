package no.ssb.exploration;

import java.util.List;
import java.util.Map;

public interface PersistenceProvider {
    void save(Map<String, List<LDSObject>> ldsObject);
}
