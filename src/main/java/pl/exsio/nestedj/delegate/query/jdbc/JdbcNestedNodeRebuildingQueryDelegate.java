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
