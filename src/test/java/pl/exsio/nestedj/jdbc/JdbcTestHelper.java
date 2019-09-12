package pl.exsio.nestedj.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import pl.exsio.nestedj.base.TestHelper;
import pl.exsio.nestedj.model.TestNode;

import javax.sql.DataSource;

public class JdbcTestHelper implements TestHelper {

    private final JdbcTemplate jdbcTemplate;

    JdbcTestHelper(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public TestNode findNode(String symbol) {
        return jdbcTemplate.query("select id, node_name, tree_left, tree_right, tree_level, parent_id, discriminator from nested_nodes where node_name = '" + symbol + "'",
                TestNode::fromResultSet);
    }

    @Override
    public TestNode getParent(TestNode f) {
        refresh(f);
        TestNode parent = null;
        Long parentId = f.getParentId();
        if (parentId != null) {
            parent = findNode(parentId);
        }
        System.out.println(String.format("Parent of %s is %s", f.getName(), parent != null ? parent.getName() : "null"));
        return parent;
    }

    public TestNode findNode(Long id) {
        return jdbcTemplate.query("select id, node_name, tree_left, tree_right, tree_level, parent_id, discriminator from nested_nodes where id = '" + id + "'",
                TestNode::fromResultSet);
    }

    @Override
    public void breakTree() {
        jdbcTemplate.execute("update nested_nodes set tree_left = 0, tree_right = 0, tree_level = 0 where discriminator = 'tree_1'");
    }

    @Override
    public void resetParent(String symbol) {
        jdbcTemplate.execute("update nested_nodes set parent_id = null where node_name='"+symbol+"' and discriminator = 'tree_1'");
    }

    @Override
    public void removeTree() {
        jdbcTemplate.execute("delete from nested_nodes where discriminator = 'tree_1'");
    }

    public void flushAndClear() {
    }

    public void flush() {
    }

    public void refresh(TestNode node) {
        if(node.getId() == null) {
            return;
        }
        TestNode refreshed = findNode(node.getId());
        node.setParentId(refreshed.getParentId());
        node.setName(refreshed.getName());
        node.setTreeRight(refreshed.getTreeRight());
        node.setTreeLeft(refreshed.getTreeLeft());
        node.setTreeLevel(refreshed.getTreeLevel());
        node.setDiscriminator(refreshed.getDiscriminator());
    }

    public void save(TestNode node) {
        jdbcTemplate.execute(
                String.format("insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(%s,%s,%s,%s,'%s',%s,'%s')",
                            null, node.getTreeLeft(), node.getTreeLevel(), node.getTreeRight(), node.getName(), node.getParentId(), node.getDiscriminator()
                        )
        );
    }
}
