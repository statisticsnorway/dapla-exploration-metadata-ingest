package no.ssb.exploration;

import org.apache.avro.Schema;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public class AvroSchemaTraverser {

    public static class Context {
        private static final Set<Schema.Type> CONTAINER_TYPES = Set.of(
                Schema.Type.UNION,
                Schema.Type.MAP,
                Schema.Type.RECORD,
                Schema.Type.ARRAY
        );
        public final Schema schema;
        public final Optional<Schema.Field> field;
        public final Optional<Schema.Type> parentType;

        public Context(Schema schema) {
            Objects.requireNonNull(schema);
            this.schema = schema;
            this.field = Optional.empty();
            this.parentType = Optional.empty();
        }

        public Context(Schema.Type parentType, Schema schema) {
            Objects.requireNonNull(schema);
            Objects.requireNonNull(parentType);
            this.schema = schema;
            this.field = Optional.empty();
            this.parentType = Optional.of(parentType);
        }

        public Context(Schema.Type parentType, Schema.Field field) {
            Objects.requireNonNull(field);
            Objects.requireNonNull(parentType);
            this.schema = field.schema();
            this.field = Optional.of(field);
            this.parentType = Optional.of(parentType);
        }

        public Schema getSchema() {
            return schema;
        }

        public Optional<Schema.Field> getField() {
            return field;
        }

        public Optional<Schema.Type> getParentType() {
            return parentType;
        }

        public String name() {
            return field.map(Schema.Field::name)
                    .orElseGet(() -> {
                                if (parentType.isEmpty()) {
                                    return "ROOT";
                                }
                                return parentType
                                        .filter(Schema.Type.ARRAY::equals)
                                        .map(t -> "[]")
                                        .orElseThrow(); // Will happen if UNION is used
                            }
                    );
        }

        public boolean isContainer() {
            return CONTAINER_TYPES.contains(schema.getType());
        }

        public boolean isLeaf() {
            return !isContainer();
        }
    }

    public static void dps(Schema schema, BiConsumer<Deque<Context>, Context> visitor) {
        Deque<Context> ancestors = new LinkedList<>();
        dps(ancestors, new Context(schema), visitor);
    }

    public static void dps(Deque<Context> ancestors, Context context, BiConsumer<Deque<Context>, Context> visitor) {
        Schema schema = context.schema;
        visitor.accept(ancestors, context);
        Schema.Type type = schema.getType();
        switch (type) {
            case MAP:
            case RECORD:
                for (Schema.Field field : schema.getFields()) {
                    ancestors.push(context);
                    dps(ancestors, new Context(type, field), visitor);
                    ancestors.pop();
                }
                break;
            case ARRAY:
                ancestors.push(context);
                dps(ancestors, new Context(type, schema.getElementType()), visitor);
                ancestors.pop();
                break;
            case UNION:
                ancestors.push(context);
                for (Schema unionElement : schema.getTypes()) {
                    dps(ancestors, new Context(type, unionElement), visitor);
                }
                ancestors.pop();
                break;
            case INT:
                break;
            case ENUM:
                break;
            case LONG:
                break;
            case NULL:
                break;
            case BYTES:
                break;
            case FIXED:
                break;
            case FLOAT:
                break;
            case DOUBLE:
                break;
            case STRING:
                break;
            case BOOLEAN:
                break;
        }
    }
}
