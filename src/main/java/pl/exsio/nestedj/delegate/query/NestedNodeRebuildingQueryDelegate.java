package pl.exsio.nestedj.delegate.query;

import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;
import java.util.List;

public interface NestedNodeRebuildingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>> {

    void destroyTree();

    N findFirst();

    void resetFirst(N first);

    List<N> getSiblings(ID first);

    List<N> getChildren(N parent);
}
