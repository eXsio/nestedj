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
        jdbcTemplate.update(insertQuery, insertValuesProvider.apply(node));
    }

    @Override
    public void incrementSideFieldsGreaterThan(Long from, String fieldName) {
        updateFields(from, fieldName, false);
    }

    @Override
    public void incermentSideFieldsGreaterThanOrEqualTo(Long from, String fieldName) {
        updateFields(from, fieldName, true);
    }

    private void updateFields(Long from, String fieldName, boolean gte) {
        String columnName = treeColumnNames.get(fieldName);
        String sign = gte ? ">=" : ">";
        jdbcTemplate.update(
                getDiscriminatedQuery(String.format("update %s set %s = (%s + ?) where %s %s ?", tableName, columnName, columnName, columnName, sign)),
                preparedStatement -> {
                    preparedStatement.setLong(1, INCREMENT_BY);
                    preparedStatement.setLong(2, from);
                }
        );
    }
}
