package pl.exsio.nestedj.delegate.query.mem;

import pl.exsio.nestedj.config.mem.InMemoryNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.config.mem.discriminator.InMemoryTreeDiscriminator;
import pl.exsio.nestedj.config.mem.identity.InMemoryNestedNodeIdentityGenerator;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Stream;

public abstract class InMemoryNestedNodeQueryDelegate<ID extends Serializable, N extends NestedNode<ID>> {

    private final InMemoryTreeDiscriminator<ID, N> treeDiscriminator;

    private final InMemoryNestedNodeIdentityGenerator<ID> identityGenerator;

    private final Set<N> nodes;

    public InMemoryNestedNodeQueryDelegate(InMemoryNestedNodeRepositoryConfiguration<ID, N> configuration) {
        this.treeDiscriminator = configuration.getTreeDiscriminator();
        this.identityGenerator = configuration.getIdentityGenerator();
        this.nodes = configuration.getNodes();
    }

    protected Stream<N> nodesStream() {
        return treeDiscriminator != null ? nodes.stream().filter(treeDiscriminator::applies) : nodes.stream();
    }

    protected ID generateIdentity() {
        return identityGenerator.generateIdentity();
    }
}
