package pl.exsio.nestedj.config.mem.discriminator;

import pl.exsio.nestedj.model.TestNode;

public class TestInMemoryTreeDiscriminator implements InMemoryTreeDiscriminator<Long, TestNode> {
    @Override
    public boolean applies(TestNode node) {
        return "tree_1".equals(node.getDiscriminator());
    }
}
