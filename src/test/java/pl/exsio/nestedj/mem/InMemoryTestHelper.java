package pl.exsio.nestedj.mem;

import pl.exsio.nestedj.TestConfiguration;
import pl.exsio.nestedj.base.TestHelper;
import pl.exsio.nestedj.config.mem.InMemoryNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.model.TestNode;

public class InMemoryTestHelper implements TestHelper {

    private final InMemoryNestedNodeRepositoryConfiguration<Long, TestNode> config = TestConfiguration.IN_MEM_CONFIG;

    @Override
    public TestNode findNode(String symbol) {
        return config.getNodes().stream().filter(n -> n.getName().equals(symbol)).findFirst().get();
    }

    @Override
    public TestNode getParent(TestNode f) {
        if (f.getParentId() == null) {
            return null;
        }
        return config.getNodes().stream().filter(n -> n.getId().equals(f.getId())).findFirst().get();
    }

    @Override
    public void breakTree() {
        config.getNodes().forEach(n -> {
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
        config.getNodes().clear();
    }

    @Override
    public void save(TestNode node) {
        node.setId(config.getIdentityGenerator().generateIdentity());
        config.getNodes().add(node);
    }

    public void rollback() {
        config.getNodes().clear();
        config.getNodes().addAll(TestConfiguration.IN_MEM_NODES);
    }
}
