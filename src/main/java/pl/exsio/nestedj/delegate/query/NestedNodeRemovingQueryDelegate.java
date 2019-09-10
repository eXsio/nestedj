package pl.exsio.nestedj.delegate.query;

import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;

public interface NestedNodeRemovingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>> {

    void updateNodesParent(NestedNodeInfo<ID> node);

    void performSingleDeletion(NestedNodeInfo<ID> node);

    void updateSideFieldsBeforeSingleNodeRemoval(Long from, String field);

    void updateDeletedNodesChildren(NestedNodeInfo<ID> node);

    void updateSideFieldsAfterSubtreeRemoval(Long from, Long delta, String field);

    void performBatchDeletion(NestedNodeInfo<ID> node);
}
