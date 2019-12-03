package pl.exsio.nestedj.delegate.query;

import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface NestedNodeRetrievingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>> {

    List<N> getTreeAsList(N node);

    List<N> getChildren(N node);

    Optional<N> getParent(N node);

    List<N> getParents(N node);

    Optional<N> getPrevSibling(N node);

    Optional<N> getNextSibling(N node);

    Optional<NestedNodeInfo<ID>> getNodeInfo(ID nodeId);

    Optional<N> findFirstRoot();

    Optional<N> findLastRoot();
}
