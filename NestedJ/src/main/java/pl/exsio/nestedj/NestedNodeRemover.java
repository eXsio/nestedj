package pl.exsio.nestedj;

import pl.exsio.nestedj.model.NestedNode;

/**
 *
 * @author exsio
 * @param <T>
 */
public interface NestedNodeRemover<T extends NestedNode> {

    void removeSingle(T node);

    void removeSubtree(T node);
}
