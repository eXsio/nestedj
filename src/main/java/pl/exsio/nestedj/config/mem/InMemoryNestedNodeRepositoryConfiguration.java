package pl.exsio.nestedj.config.mem;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import pl.exsio.nestedj.config.mem.discriminator.InMemoryTreeDiscriminator;
import pl.exsio.nestedj.config.mem.identity.InMemoryNestedNodeIdentityGenerator;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

public class InMemoryNestedNodeRepositoryConfiguration<ID extends Serializable, N extends NestedNode<ID>> {

    private final InMemoryTreeDiscriminator<ID, N> treeDiscriminator;

    private final InMemoryNestedNodeIdentityGenerator<ID> identityGenerator;

    private final Set<N> nodes = Sets.newConcurrentHashSet();

    public InMemoryNestedNodeRepositoryConfiguration(InMemoryNestedNodeIdentityGenerator<ID> identityGenerator) {
        this(identityGenerator, Lists.newArrayList(), null);
    }

    public InMemoryNestedNodeRepositoryConfiguration(InMemoryNestedNodeIdentityGenerator<ID> identityGenerator, Collection<N> nodes) {
       this(identityGenerator, nodes, null);
    }

    public InMemoryNestedNodeRepositoryConfiguration(InMemoryNestedNodeIdentityGenerator<ID> identityGenerator, Collection<N> nodes, InMemoryTreeDiscriminator<ID, N> treeDiscriminator) {
        this.identityGenerator = identityGenerator;
        this.treeDiscriminator = treeDiscriminator;
        this.nodes.addAll(nodes);
    }

    public InMemoryTreeDiscriminator<ID, N> getTreeDiscriminator() {
        return treeDiscriminator;
    }

    public InMemoryNestedNodeIdentityGenerator<ID> getIdentityGenerator() {
        return identityGenerator;
    }

    public Set<N> getNodes() {
        return nodes;
    }
}
