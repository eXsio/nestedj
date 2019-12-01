package pl.exsio.nestedj.delegate.query.jdbc;

import com.google.common.collect.Maps;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import pl.exsio.nestedj.config.jdbc.JdbcNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.config.jdbc.discriminator.JdbcTreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
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

    protected final Map<String, String> treeColumnNames;

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
        this.treeColumnNames = configuration.getTreeColumnNames();
    }

    protected String getDiscriminatedQuery(String baseQuery) {
        String disriminatedQuery = treeDiscriminator.getQueryPart();
        String[] queryParts = baseQuery.split("order by");

        String modifiedQuery = queryParts[0].contains("where") ? String.format("%s and %s", queryParts[0], disriminatedQuery) : String.format("%s where %s", queryParts[0], disriminatedQuery);
        String s = queryParts.length == 1 ? modifiedQuery : String.format("%s order by %s", modifiedQuery, queryParts[1]);
        return s;
    }

    protected void setDiscriminatorParams(PreparedStatement ps, int offset) throws SQLException {
        for (int i = 0; i < treeDiscriminator.getParameters().size(); i++) {
            ps.setObject(i + offset, treeDiscriminator.getParameters().get(i));
        }
    }

    protected class Query {

        private final String query;

        private final Map<String, String> parts = Maps.newHashMap();

        protected Query(String query) {
            this.query = query;
        }

        public Query set(String label, String part) {
            parts.put(label, part);
            return this;
        }

        protected String build() {
            String q = query;
            q = q.replaceAll(":tableName", tableName);
            q = q.replaceAll(":parentId", parentId);
            q = q.replaceAll(":id", id);
            q = q.replaceAll(":left", left);
            q = q.replaceAll(":right", right);
            q = q.replaceAll(":level", level);
            for (Map.Entry<String, String> entry : parts.entrySet()) {
                String label = ":" + entry.getKey();
                String part = entry.getValue();
                q = q.replaceAll(label, part);
            }
            return q;
        }
    }
}
