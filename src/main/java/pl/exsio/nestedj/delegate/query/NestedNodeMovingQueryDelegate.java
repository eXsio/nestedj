package pl.exsio.nestedj.delegate.query;

import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;

public interface NestedNodeMovingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>> {

    Integer markNodeIds(NestedNodeInfo<ID> node);

    void updateFieldsUp(Long delta, Long start, Long stop, String field);

    void updateFieldsDown(Long delta, Long start, Long stop, String field);

    void performMoveUp(Long nodeDelta, Long levelModificator);

    void performMoveDown(Long nodeDelta, Long levelModificator);

    void updateParentField(ID newParentId, NestedNodeInfo<ID> node);

    void clearParentField(NestedNodeInfo<ID> node);
}
