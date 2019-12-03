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

package pl.exsio.nestedj.config.jdbc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import pl.exsio.nestedj.config.jdbc.discriminator.JdbcTreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Configuration class that serves as a base of creating new instances of JDBC Repository.
 *
 * @param <ID> - Nested Node Identifier Class
 * @param <N> - Nested Node Class
 */
public class JdbcNestedNodeRepositoryConfiguration<ID extends Serializable, N extends NestedNode<ID>> {

    private final JdbcTemplate jdbcTemplate;

    private final String tableName;

    private final RowMapper<N> rowMapper;

    private final String insertQuery;

    private final Function<N, Object[]> insertValuesProvider;

    private final JdbcTreeDiscriminator treeDiscriminator;

    private String selectQuery;

    private Map<String, String> treeColumnNames = Maps.newHashMap();

    /**
     * Creates new JDBC Repository with custm Tree Discriminator.
     *
     * @param jdbcTemplate - Spring JDBC Template to be used by the Repository
     * @param tableName - name of the Database Table with Nested Nodes
     * @param rowMapper - Spring RowMapper that can create instances of N from SQL ResultSet
     * @param insertQuery - SQL Query used to insert the Nodes to the Table
     * @param insertValuesProvider - provider of Insert SQL values
     * @param treeDiscriminator - custom Tree Discriminator
     */
    public JdbcNestedNodeRepositoryConfiguration(JdbcTemplate jdbcTemplate, String tableName,
                                                 RowMapper<N> rowMapper, String insertQuery,
                                                 Function<N, Object[]> insertValuesProvider,
                                                 JdbcTreeDiscriminator treeDiscriminator) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
        this.rowMapper = rowMapper;
        this.insertQuery = insertQuery;
        this.insertValuesProvider = insertValuesProvider;
        this.treeDiscriminator = treeDiscriminator;
        treeColumnNames.put(NestedNode.ID, NestedNode.ID);
        treeColumnNames.put(NestedNode.LEFT, NestedNode.LEFT);
        treeColumnNames.put(NestedNode.RIGHT, NestedNode.RIGHT);
        treeColumnNames.put(NestedNode.LEVEL, NestedNode.LEVEL);
        treeColumnNames.put(NestedNode.PARENT_ID, NestedNode.PARENT_ID);
        this.selectQuery = String.format("select * from %s", tableName);
    }

    /**
     * Creates new JDBC Repository with no Tree Discriminator.
     *
     * @param jdbcTemplate - Spring JDBC Template to be used by the Repository
     * @param tableName - name of the Database Table with Nested Nodes
     * @param rowMapper - Spring RowMapper that can create instances of N from SQL ResultSet
     * @param insertQuery - SQL Query used to insert the Nodes to the Table
     * @param insertValuesProvider - provider of Insert SQL values
     */
    public JdbcNestedNodeRepositoryConfiguration(JdbcTemplate jdbcTemplate, String tableName,
                                                 RowMapper<N> rowMapper, String insertQuery,
                                                 Function<N, Object[]> insertValuesProvider) {
        this(jdbcTemplate, tableName, rowMapper, insertQuery, insertValuesProvider, new JdbcTreeDiscriminator() {
            @Override
            public String getQueryPart() {
                return "";
            }

            @Override
            public List<Object> getParameters() {
                return Lists.newLinkedList();
            }
        });
    }

    /**
     * @return Spring JdbcTemplate used by this Configuration
     */
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
     * @return Database Table name used by this Configuration
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @return Spring RowMapper used by this Configuration
     */
    public RowMapper<N> getRowMapper() {
        return rowMapper;
    }

    /**
     * @return Insert SQL Query used by this Configuration
     */
    public String getInsertQuery() {
        return insertQuery;
    }

    /**
     * @return Insert statement values provider used by this Configuration
     */
    public Function<N, Object[]> getInsertValuesProvider() {
        return insertValuesProvider;
    }

    /**
     * @return Tree Discriminator used by this Configuration
     */
    public JdbcTreeDiscriminator getTreeDiscriminator() {
        return treeDiscriminator;
    }

    /**
     * @return Maping of SQL column names to standarized field names used by this Configuration
     */
    public Map<String, String> getTreeColumnNames() {
        return treeColumnNames;
    }

    /**
     * Sets custom ID column name
     */
    public JdbcNestedNodeRepositoryConfiguration<ID, N> setIdColumnName(String field) {
        treeColumnNames.put(NestedNode.ID, field);
        return this;
    }

    /**
     * Sets custom PARENT_ID column name
     */
    public JdbcNestedNodeRepositoryConfiguration<ID, N> setParentIdColumnName(String field) {
        treeColumnNames.put(NestedNode.PARENT_ID, field);
        return this;
    }

    /**
     * Sets custom LEFT column name
     */
    public JdbcNestedNodeRepositoryConfiguration<ID, N> setLeftColumnName(String field) {
        treeColumnNames.put(NestedNode.LEFT, field);
        return this;
    }

    /**
     * Sets custom RIGHT name
     */
    public JdbcNestedNodeRepositoryConfiguration<ID, N> setRightColumnName(String field) {
        treeColumnNames.put(NestedNode.RIGHT, field);
        return this;
    }

    /**
     * Sets custom LEVEL column name
     */
    public JdbcNestedNodeRepositoryConfiguration<ID, N> setLevelColumnName(String field) {
        treeColumnNames.put(NestedNode.LEVEL, field);
        return this;
    }

    /**
     * @return SQL Select Query used by this Configuration
     */
    public String getSelectQuery() {
        return selectQuery;
    }

    /**
     * Sets custom SQL Select query
     */
    public void setSelectQuery(String selectQuery) {
        this.selectQuery = selectQuery;
    }
}
