package no.ssb.exploration;

import java.time.ZonedDateTime;
import java.util.function.Supplier;

public class LDSObject {

    public final String type;
    public final String id;
    public final ZonedDateTime version;
    private final Supplier<Object> supplier; // Jackson serializable
    private Object object;

    public LDSObject(String type, String id, ZonedDateTime version, Supplier<Object> supplier) {
        this.type = type;
        this.id = id;
        this.version = version;
        this.supplier = supplier;
    }

    public String link() {
        return type + "/" + id;
    }

    public String id() {
        return id;
    }

    public Object get() {
        if (object == null) {
            object = supplier.get();
        }
        return object;
    }

    public <T> T get(Class<T> clazz) {
        return (T) get();
    }
}
