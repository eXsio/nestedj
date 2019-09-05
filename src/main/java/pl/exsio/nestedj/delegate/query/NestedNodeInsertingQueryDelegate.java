package pl.exsio.nestedj.delegate.query;

import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;

import static pl.exsio.nestedj.delegate.NestedNodeHierarchyManipulator.Mode;

public interface NestedNodeInsertingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>> {

    void saveNode(N node);

    void updateFields(Long from, Mode mode, String fieldName, boolean applyGte);
}
