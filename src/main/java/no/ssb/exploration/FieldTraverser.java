package no.ssb.exploration;

import no.ssb.dapla.dataset.doc.model.lineage.Field;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.BiConsumer;

public class FieldTraverser {

    public static void depthFirstTraversal(Field field, BiConsumer<Deque<Field>, Field> visitor) {
        Deque<Field> anscestors = new LinkedList<>();
        depthFirstTraversal(anscestors, field, visitor);
    }

    private static void depthFirstTraversal(Deque<Field> anscestors, Field field, BiConsumer<Deque<Field>, Field> visitor) {
        visitor.accept(anscestors, field);
        anscestors.push(field);
        for (Field child : field.getFields()) {
            depthFirstTraversal(anscestors, child, visitor);
        }
        anscestors.pop();
    }
}
