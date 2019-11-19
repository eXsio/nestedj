package pl.exsio.nestedj.delegate.query;

import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;
import java.util.Optional;

public interface NestedNodeRetrievingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>> {

    Iterable<N> getTreeAsList(N node);

    Iterable<N> getChildren(N node);

    Optional<N> getParent(N node);

    Iterable<N> getParents(N node);

    Optional<N> getPrevSibling(N node);

    Optional<N> getNextSibling(N node);

    Optional<NestedNodeInfo<ID>> getNodeInfo(ID nodeId);

    Optional<N> findFirstRoot();

    Optional<N> findLastRoot();
}
