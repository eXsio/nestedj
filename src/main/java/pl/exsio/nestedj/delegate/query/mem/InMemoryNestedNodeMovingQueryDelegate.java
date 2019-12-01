package pl.exsio.nestedj.delegate.query.mem;

import pl.exsio.nestedj.config.mem.InMemoryNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeMovingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;

public class InMemoryNestedNodeMovingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends InMemoryNestedNodeQueryDelegate<ID, N>
        implements NestedNodeMovingQueryDelegate<ID, N> {

    public InMemoryNestedNodeMovingQueryDelegate(InMemoryNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }


    @Override
    public Integer markNodeIds(NestedNodeInfo<ID> node) {
        return null;
    }

    @Override
    public void updateSideFieldsUp(Long delta, Long start, Long stop, String field) {

    }

    @Override
    public void updateSideFieldsDown(Long delta, Long start, Long stop, String field) {

    }

    @Override
    public void performMoveUp(Long nodeDelta, Long levelModificator) {

    }

    @Override
    public void performMoveDown(Long nodeDelta, Long levelModificator) {

    }

    @Override
    public void updateParentField(ID newParentId, NestedNodeInfo<ID> node) {

    }

    @Override
    public void clearParentField(NestedNodeInfo<ID> node) {

    }
}
