/*
 *  The MIT License
 *
 *  Copyright (c) 2019 eXsio.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 *  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 *  the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 *  BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package pl.exsio.nestedj.delegate.query.jdbc;

import org.springframework.jdbc.core.ResultSetExtractor;
import pl.exsio.nestedj.config.jdbc.JdbcNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeRetrievingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class JdbcNestedNodeRetrievingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends JdbcNestedNodeQueryDelegate<ID, N>
        implements NestedNodeRetrievingQueryDelegate<ID, N> {

    public JdbcNestedNodeRetrievingQueryDelegate(JdbcNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }


    @Override
    public List<N> getTreeAsList(N node) {
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
    public List<N> getChildren(N node) {
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
    public List<N> getParents(N node) {
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
            return new LinkedList<>();
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
                    if (!rs.next()) {
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
