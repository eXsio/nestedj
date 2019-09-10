package pl.exsio.nestedj.delegate.query.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import pl.exsio.nestedj.config.jdbc.JdbcNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.jdbc.discriminator.JdbcTreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;
import java.util.function.Function;

public abstract class JdbcNestedNodeQueryDelegate<ID extends Serializable, N extends NestedNode<ID>> {

    protected final JdbcTemplate jdbcTemplate;

    protected final String tableName;

    protected final RowMapper<N> rowMapper;

    protected final String insertQuery;

    protected final Function<N, Object[]> insertValuesProvider;

    protected final JdbcTreeDiscriminator treeDiscriminator;

    protected final String selectQuery;

    protected final String id;

    protected final String parentId;

    protected final String left;

    protected final String right;

    protected final String level;

    public JdbcNestedNodeQueryDelegate(JdbcNestedNodeRepositoryConfiguration<ID, N> configuration) {
        this.jdbcTemplate = configuration.getJdbcTemplate();
        this.tableName = configuration.getTableName();
        this.rowMapper = configuration.getRowMapper();
        this.insertQuery = configuration.getInsertQuery();
        this.insertValuesProvider = configuration.getInsertValuesProvider();
        this.treeDiscriminator = configuration.getTreeDiscriminator();
        this.selectQuery = configuration.getSelectQuery();
        this.id = configuration.getTreeColumnNames().get(NestedNode.ID);
        this.parentId = configuration.getTreeColumnNames().get(NestedNode.PARENT_ID);
        this.left = configuration.getTreeColumnNames().get(NestedNode.LEFT);
        this.right = configuration.getTreeColumnNames().get(NestedNode.RIGHT);
        this.level = configuration.getTreeColumnNames().get(NestedNode.LEVEL);
    }
}
