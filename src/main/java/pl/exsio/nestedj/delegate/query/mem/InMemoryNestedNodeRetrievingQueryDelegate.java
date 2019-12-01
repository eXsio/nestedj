package pl.exsio.nestedj.delegate.query.mem;

import pl.exsio.nestedj.config.mem.InMemoryNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeRetrievingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;
import java.util.Optional;

public class InMemoryNestedNodeRetrievingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends InMemoryNestedNodeQueryDelegate<ID, N>
        implements NestedNodeRetrievingQueryDelegate<ID, N> {

    public InMemoryNestedNodeRetrievingQueryDelegate(InMemoryNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }


    @Override
    public Iterable<N> getTreeAsList(N node) {
        return null;
    }

    @Override
    public Iterable<N> getChildren(N node) {
        return null;
    }

    @Override
    public Optional<N> getParent(N node) {
        return Optional.empty();
    }

    @Override
    public Iterable<N> getParents(N node) {
        return null;
    }

    @Override
    public Optional<N> getPrevSibling(N node) {
        return Optional.empty();
    }

    @Override
    public Optional<N> getNextSibling(N node) {
        return Optional.empty();
    }

    @Override
    public Optional<NestedNodeInfo<ID>> getNodeInfo(ID nodeId) {
        return Optional.empty();
    }

    @Override
    public Optional<N> findFirstRoot() {
        return Optional.empty();
    }

    @Override
    public Optional<N> findLastRoot() {
        return Optional.empty();
    }
}
