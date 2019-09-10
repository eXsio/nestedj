package pl.exsio.nestedj.delegate.query.jdbc;

import pl.exsio.nestedj.config.jdbc.JdbcNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeInsertingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;

public class JdbcNestedNodeInsertingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends JdbcNestedNodeQueryDelegate<ID, N>
        implements NestedNodeInsertingQueryDelegate<ID, N> {

    public JdbcNestedNodeInsertingQueryDelegate(JdbcNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }

    @Override
    public void insert(N node) {
    }

    @Override
    public void incrementSideFieldsGreaterThan(Long from, String fieldName) {
    }

    @Override
    public void incermentSideFieldsGreaterThanOrEqualTo(Long from, String fieldName) {
    }
}
