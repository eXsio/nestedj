package pl.exsio.nestedj.delegate.query.jdbc;

import org.springframework.jdbc.support.KeyHolder;
import pl.exsio.nestedj.config.jdbc.JdbcNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeInsertingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Types;

public class JdbcNestedNodeInsertingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends JdbcNestedNodeQueryDelegate<ID, N>
        implements NestedNodeInsertingQueryDelegate<ID, N> {

    public JdbcNestedNodeInsertingQueryDelegate(JdbcNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }

    @Override

    public void insert(N node) {
        if(node.getId() == null) {
            doInsert(node);
        } else {
            update(node);
        }

    }

    private void update(N node) {
        jdbcTemplate.update(
                getDiscriminatedQuery(
                        new Query("update :tableName set :left = ?, :right = ?, :level = ?, :parentId = ? where :id = ?").build()
                ),
                preparedStatement -> {
                    preparedStatement.setObject(1, node.getTreeLeft());
                    preparedStatement.setObject(2, node.getTreeRight());
                    preparedStatement.setObject(3, node.getTreeLevel());
                    if(node.getParentId() == null) {
                        preparedStatement.setNull(4, Types.OTHER);
                    } else {
                        preparedStatement.setObject(4, node.getParentId());
                    }
                    preparedStatement.setObject(5, node.getId());
                    setDiscriminatorParams(preparedStatement, 6);
                }
        );
    }

    @SuppressWarnings("unchecked")
    private void doInsert(N node) {
        KeyHolder keyHolder = new JdbcKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(insertQuery, new String[]{id});
            Object[] params = insertValuesProvider.apply(node);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keyHolder);
        node.setId((ID) keyHolder.getKey());
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
                getDiscriminatedQuery(
                        new Query("update :tableName set :columnName = (:columnName + ?) where :columnName :sign ?")
                                .set("columnName", columnName)
                                .set("sign", sign)
                                .build()
                ),
                preparedStatement -> {
                    preparedStatement.setLong(1, INCREMENT_BY);
                    preparedStatement.setLong(2, from);
                    setDiscriminatorParams(preparedStatement, 3);
                }
        );
    }
}
