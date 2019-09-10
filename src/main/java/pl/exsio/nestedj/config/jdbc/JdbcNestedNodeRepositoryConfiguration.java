package pl.exsio.nestedj.config.jdbc;

import com.google.common.collect.Maps;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import pl.exsio.nestedj.jdbc.discriminator.JdbcTreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

public class JdbcNestedNodeRepositoryConfiguration<ID extends Serializable, N extends NestedNode<ID>> {

    private final JdbcTemplate jdbcTemplate;

    private final String tableName;

    private final RowMapper<N> rowMapper;

    private final String insertQuery;

    private final Function<Object[], N> insertValuesProvider;

    private final JdbcTreeDiscriminator treeDiscriminator;

    private Map<String, String> treeColumnNames = Maps.newHashMap();

    public JdbcNestedNodeRepositoryConfiguration(JdbcTemplate jdbcTemplate, String tableName,
                                                  RowMapper<N> rowMapper, String insertQuery,
                                                  Function<Object[], N> insertValuesProvider,
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
    }

    public JdbcNestedNodeRepositoryConfiguration(JdbcTemplate jdbcTemplate, String tableName,
                                                 RowMapper<N> rowMapper, String insertQuery,
                                                 Function<Object[], N> insertValuesProvider) {
        this(jdbcTemplate, tableName, rowMapper, insertQuery, insertValuesProvider, () -> "");
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public String getTableName() {
        return tableName;
    }

    public RowMapper<N> getRowMapper() {
        return rowMapper;
    }

    public String getInsertQuery() {
        return insertQuery;
    }

    public Function<Object[], N> getInsertValuesProvider() {
        return insertValuesProvider;
    }

    public JdbcTreeDiscriminator getTreeDiscriminator() {
        return treeDiscriminator;
    }

    public Map<String, String> getTreeColumnNames() {
        return treeColumnNames;
    }

    public JdbcNestedNodeRepositoryConfiguration<ID, N> setIdColumnName(String field) {
        treeColumnNames.put(NestedNode.ID, field);
        return this;
    }

    public JdbcNestedNodeRepositoryConfiguration<ID, N> setParentIdColumnName(String field) {
        treeColumnNames.put(NestedNode.PARENT_ID, field);
        return this;
    }

    public JdbcNestedNodeRepositoryConfiguration<ID, N> setLeftColumnName(String field) {
        treeColumnNames.put(NestedNode.LEFT, field);
        return this;
    }

    public JdbcNestedNodeRepositoryConfiguration<ID, N> setRighColumnName(String field) {
        treeColumnNames.put(NestedNode.RIGHT, field);
        return this;
    }

    public JdbcNestedNodeRepositoryConfiguration<ID, N> setLevelColumnName(String field) {
        treeColumnNames.put(NestedNode.LEVEL, field);
        return this;
    }

    public JdbcNestedNodeRepositoryConfiguration<ID, N> setIdField(String field) {
        treeColumnNames.put(NestedNode.ID, field);
        return this;
    }


}
