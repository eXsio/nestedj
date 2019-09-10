package pl.exsio.nestedj.delegate.query.jdbc;

import com.google.common.collect.Maps;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import pl.exsio.nestedj.config.jdbc.JdbcNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.jdbc.discriminator.JdbcTreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

public abstract class JdbcNestedNodeQueryDelegate<ID extends Serializable, N extends NestedNode<ID>> {

    protected final JdbcTemplate jdbcTemplate;

    protected final String tableName;

    protected final RowMapper<N> rowMapper;

    protected final String insertQuery;

    protected final Function<N, Object[]> insertValuesProvider;

    protected final JdbcTreeDiscriminator treeDiscriminator;

    protected Map<String, String> treeColumnNames = Maps.newHashMap();

    public JdbcNestedNodeQueryDelegate(JdbcNestedNodeRepositoryConfiguration<ID, N> configuration) {
        this.jdbcTemplate = configuration.getJdbcTemplate();
        this.tableName = configuration.getTableName();
        this.rowMapper = configuration.getRowMapper();
        this.insertQuery = configuration.getInsertQuery();
        this.insertValuesProvider = configuration.getInsertValuesProvider();
        this.treeDiscriminator = configuration.getTreeDiscriminator();

    }

}
