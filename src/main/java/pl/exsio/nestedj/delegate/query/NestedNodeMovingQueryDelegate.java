package pl.exsio.nestedj.delegate.query;

import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;
import java.util.List;

public interface NestedNodeMovingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>> {

    List<ID> getNodeIds(NestedNodeInfo<ID, N> node);

    void updateFieldsUp(Long delta, Long start, Long stop, String field);

    void updateFieldsDown(Long delta, Long start, Long stop, String field);

    void performMoveUp(Long nodeDelta, List<ID> nodeIds, Long levelModificator);

    void performMoveDown(Long nodeDelta, List<ID> nodeIds, Long levelModificator);

    void updateParentField(ID newParentId, NestedNodeInfo<ID, N> node);

    void clearParentField(NestedNodeInfo<ID, N> node);
}
