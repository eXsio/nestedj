package pl.exsio.nestedj.delegate.query.jdbc;

import com.google.common.collect.Lists;
import org.springframework.jdbc.core.ResultSetExtractor;
import pl.exsio.nestedj.config.jdbc.JdbcNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeRetrievingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;
import java.util.Optional;

public class JdbcNestedNodeRetrievingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends JdbcNestedNodeQueryDelegate<ID, N>
        implements NestedNodeRetrievingQueryDelegate<ID, N> {

    public JdbcNestedNodeRetrievingQueryDelegate(JdbcNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }


    @Override
    public Iterable<N> getTreeAsList(N node) {
        return jdbcTemplate.query(
                getDiscriminatedQuery(
                        new Query("select * from :tableName where :left >= ? and :right <= ? order by :left asc").build()
                ),
                preparedStatement -> {
                    preparedStatement.setObject(1, node.getTreeLeft());
                    preparedStatement.setObject(2, node.getTreeRight());
                    setDiscriminatorParams(preparedStatement, 3);
                },
                rowMapper
        );
    }

    @Override
    public Iterable<N> getChildren(N node) {
        return jdbcTemplate.query(
                getDiscriminatedQuery(
                        new Query("select * from :tableName where :left >= ? and :right <= ? and :level = ? order by :left asc").build()
                ),
                preparedStatement -> {
                    preparedStatement.setObject(1, node.getTreeLeft());
                    preparedStatement.setObject(2, node.getTreeRight());
                    preparedStatement.setObject(3, node.getTreeLevel() + 1);
                    setDiscriminatorParams(preparedStatement, 4);
                },
                rowMapper
        );
    }

    @Override
    public Optional<N> getParent(N node) {
        if (node.getTreeLevel() > 0) {
            return jdbcTemplate.query(
                    getDiscriminatedQuery(
                            new Query("select * from :tableName where :left < ? and :right > ? and :level = ? order by :left asc").build()
                    ),
                    preparedStatement -> {
                        preparedStatement.setObject(1, node.getTreeLeft());
                        preparedStatement.setObject(2, node.getTreeRight());
                        preparedStatement.setObject(3, node.getTreeLevel() - 1);
                        setDiscriminatorParams(preparedStatement, 4);
                    },
                    rowMapper
            ).stream().findFirst();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Iterable<N> getParents(N node) {
        if (node.getTreeLevel() > 0) {
            return jdbcTemplate.query(
                    getDiscriminatedQuery(
                            new Query("select * from :tableName where :left < ? and :right > ? order by :left desc").build()
                    ),
                    preparedStatement -> {
                        preparedStatement.setObject(1, node.getTreeLeft());
                        preparedStatement.setObject(2, node.getTreeRight());
                        setDiscriminatorParams(preparedStatement, 3);
                    },
                    rowMapper
            );
        } else {
            return Lists.newLinkedList();
        }
    }

    @Override
    public Optional<N> getPrevSibling(N node) {
        return jdbcTemplate.query(
                getDiscriminatedQuery(
                        new Query("select * from :tableName where :right = ? and :level = ? order by :left asc").build()
                ),
                preparedStatement -> {
                    preparedStatement.setObject(1, node.getTreeLeft() - 1);
                    preparedStatement.setObject(2, node.getTreeLevel());
                    setDiscriminatorParams(preparedStatement, 3);
                },
                rowMapper
        ).stream().findFirst();
    }

    @Override
    public Optional<N> getNextSibling(N node) {
        return jdbcTemplate.query(
                getDiscriminatedQuery(
                        new Query("select * from :tableName where :left = ? and :level = ? order by :left asc").build()
                ),
                preparedStatement -> {
                    preparedStatement.setObject(1, node.getTreeRight() + 1);
                    preparedStatement.setObject(2, node.getTreeLevel());
                    setDiscriminatorParams(preparedStatement, 3);
                },
                rowMapper
        ).stream().findFirst();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<NestedNodeInfo<ID>> getNodeInfo(ID nodeId) {
        NestedNodeInfo<ID> info = jdbcTemplate.query(
                getDiscriminatedQuery(
                        new Query("select :id, :parentId, :left, :right, :level from :tableName where :id = ?").build()
                ),
                preparedStatement -> {
                    preparedStatement.setObject(1, nodeId);
                    setDiscriminatorParams(preparedStatement, 2);
                },
                (ResultSetExtractor<NestedNodeInfo<ID>>) rs -> {
                    if(!rs.next()) {
                        return null;
                    }
                    return new NestedNodeInfo((ID) rs.getObject(id), (ID) rs.getObject(parentId), rs.getLong(left), rs.getLong(right), rs.getLong(level));
                }
        );
        return Optional.ofNullable(info);
    }

    @Override
    public Optional<N> findFirstRoot() {
        return jdbcTemplate.query(
                getDiscriminatedQuery(
                        new Query("select * from :tableName where :level = 0 order by :left asc").build()
                ),
                preparedStatement -> setDiscriminatorParams(preparedStatement, 1),
                rowMapper
        ).stream().findFirst();
    }

    @Override
    public Optional<N> findLastRoot() {
        return jdbcTemplate.query(
                getDiscriminatedQuery(
                        new Query("select * from :tableName where :level = 0 order by :left desc").build()
                ),
                preparedStatement -> setDiscriminatorParams(preparedStatement, 1),
                rowMapper
        ).stream().findFirst();
    }
}
