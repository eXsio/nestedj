package pl.exsio.nestedj.delegate.query;

import pl.exsio.nestedj.delegate.jpa.JpaNestedNodeMover;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;
import java.util.List;

public interface NestedNodeMovingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>> {

    void updateParentField(ID newParentId, NestedNodeInfo<ID, N> node);

    void clearParentField(NestedNodeInfo<ID, N> node);

    void updateFields(JpaNestedNodeMover.Sign sign, Long delta, Long start, Long stop, String field);

    List<ID> getNodeIds(NestedNodeInfo<ID, N> node);

    void performMove(JpaNestedNodeMover.Sign nodeSign, Long nodeDelta, List<ID> nodeIds, Long levelModificator);
}
