package no.ssb.exploration.model;

import java.time.ZonedDateTime;

public class LDSObject {
    public final String type;
    public final String id;
    public final ZonedDateTime version;
    public final Object object; // Jackson serializable

    public LDSObject(String type, String id, ZonedDateTime version, Object object) {
        this.type = type;
        this.id = id;
        this.version = version;
        this.object = object;
    }

    public String link() {
        return type + "/" + id;
    }
}
