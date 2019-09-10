package pl.exsio.nestedj.delegate.query;

import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;

public interface NestedNodeInsertingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>> {

    Long INCREMENT_BY = 2L;

    void insert(N node);

    void incrementSideFieldsGreaterThan(Long from, String fieldName);

    void incermentSideFieldsGreaterThanOrEqualTo(Long from, String fieldName);
}
