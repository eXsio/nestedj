package pl.exsio.nestedj.delegate.query.jdbc;

import pl.exsio.nestedj.config.jdbc.JdbcNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeRebuildingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;
import java.util.List;

public class JdbcNestedNodeRebuildingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends JdbcNestedNodeQueryDelegate<ID, N>
        implements NestedNodeRebuildingQueryDelegate<ID, N> {

    public JdbcNestedNodeRebuildingQueryDelegate(JdbcNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }


    @Override
    public void destroyTree() {
        jdbcTemplate.update(
                getDiscriminatedQuery(
                        new Query("update :tableName set :left = 0, :right = 0, :level = 0").build()
                ),
                preparedStatement -> setDiscriminatorParams(preparedStatement, 1)
        );
    }

    @Override
    public N findFirst() {
        List<N> result =  jdbcTemplate.query(
                getDiscriminatedQuery(
                        new Query("select * from :tableName where :parentId is null order by :id desc").build()
                ),
                preparedStatement -> {
                    setDiscriminatorParams(preparedStatement, 1);
                },
                rowMapper
        );
        return result.stream().findFirst().orElse(null);
    }

    @Override
    public void resetFirst(N first) {
        jdbcTemplate.update(
                getDiscriminatedQuery(
                        new Query("update :tableName set :left = 1, :right = 2, :level = 0 where :id = ?").build()
                ),
                preparedStatement -> {
                    preparedStatement.setObject(1, first.getId());
                    setDiscriminatorParams(preparedStatement, 2);
                }
        );
    }

    @Override
    public List<N> getSiblings(ID first) {
        return jdbcTemplate.query(
                getDiscriminatedQuery(
                        new Query("select * from :tableName where :parentId is null and :id <> ? order by :id asc").build()
                ),
                preparedStatement -> {
                    preparedStatement.setObject(1, first);
                    setDiscriminatorParams(preparedStatement, 2);
                },
                rowMapper
        );
    }

    @Override
    public List<N> getChildren(N parent) {
        return jdbcTemplate.query(
                getDiscriminatedQuery(
                        new Query("select * from :tableName where :parentId = ? order by :id asc").build()
                ),
                preparedStatement -> {
                    preparedStatement.setObject(1, parent.getId());
                    setDiscriminatorParams(preparedStatement, 2);
                },
                rowMapper
        );
    }
}
