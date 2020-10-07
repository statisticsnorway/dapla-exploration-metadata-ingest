package no.ssb.exploration;

import no.ssb.dapla.dataset.doc.model.lineage.Field;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.BiConsumer;

public class FieldTraverser {

    public static void depthFirstTraversal(Field field, BiConsumer<Deque<Field>, Field> visitor) {
        Deque<Field> ancestors = new LinkedList<>();
        depthFirstTraversal(ancestors, field, visitor);
    }

    private static void depthFirstTraversal(Deque<Field> ancestors, Field field, BiConsumer<Deque<Field>, Field> visitor) {
        visitor.accept(ancestors, field);
        ancestors.push(field);
        for (Field child : field.getFields()) {
            depthFirstTraversal(ancestors, child, visitor);
        }
        ancestors.pop();
    }
}
