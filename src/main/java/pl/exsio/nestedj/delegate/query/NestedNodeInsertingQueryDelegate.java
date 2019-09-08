package pl.exsio.nestedj.delegate.query;

import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;

public interface NestedNodeInsertingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>> {

    void insert(N node);

    void updateFieldsGreaterThan(Long from, String fieldName);

    void updateFieldsGreaterThanOrEqualTo(Long from, String fieldName);
}
