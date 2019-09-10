package pl.exsio.nestedj.delegate.query;

import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;

public interface NestedNodeRemovingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>> {

    Long DECREMENT_BY = 2L;

    void setNewParentForDeletedNodesChildren(NestedNodeInfo<ID> node);

    void performSingleDeletion(NestedNodeInfo<ID> node);

    void decrementSideFieldsBeforeSingleNodeRemoval(Long from, String field);

    void pushUpDeletedNodesChildren(NestedNodeInfo<ID> node);

    void decrementSideFieldsAfterSubtreeRemoval(Long from, Long delta, String field);

    void performBatchDeletion(NestedNodeInfo<ID> node);
}
