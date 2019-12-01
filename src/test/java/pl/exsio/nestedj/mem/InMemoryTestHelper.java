package pl.exsio.nestedj.mem;

import pl.exsio.nestedj.TestConfiguration;
import pl.exsio.nestedj.base.TestHelper;
import pl.exsio.nestedj.config.mem.InMemoryNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.model.TestNode;

import java.util.stream.Collectors;

public class InMemoryTestHelper implements TestHelper {

    private final InMemoryNestedNodeRepositoryConfiguration<Long, TestNode> config = TestConfiguration.IN_MEM_CONFIG;

    @Override
    public TestNode findNode(String symbol) {
        System.out.println(config.getNodes());
        return config.getNodes().stream().filter(n -> n.getName().equals(symbol)).findFirst().orElse(null);
    }

    @Override
    public TestNode getParent(TestNode f) {
        if (f.getParentId() == null) {
            return null;
        }
        TestNode parent = config.getNodes().stream().filter(n -> n.getId().equals(f.getParentId())).findFirst().orElse(null);
        System.out.println(String.format("Parent of %s is %s", f.getName(), parent != null ? parent.getName() : "null"));
        return parent;
    }

    @Override
    public void breakTree() {
        config.getNodes().stream()
                .filter(n -> n.getDiscriminator()
                        .equals("tree_1")).forEach(n -> {
            n.setTreeLevel(0L);
            n.setTreeLeft(0L);
            n.setTreeRight(0L);
        });
    }

    @Override
    public void resetParent(String symbol) {
        findNode(symbol).setParentId(null);
    }

    @Override
    public void removeTree() {
        config.getNodes().stream().filter(n -> n.getDiscriminator().equals("tree_1"))
                .forEach(n -> config.getNodes().remove(n));
    }

    @Override
    public void save(TestNode node) {
        node.setId(config.getIdentityGenerator().generateIdentity());
        config.getNodes().add(node);
    }

    public void rollback() {
        config.getNodes().clear();
        config.getNodes().addAll(TestConfiguration.IN_MEM_NODES.stream().map(TestNode::copy).collect(Collectors.toList()));
    }
}
