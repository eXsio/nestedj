package pl.exsio.nestedj.delegate.query.mem;

import com.google.common.base.Preconditions;
import pl.exsio.nestedj.config.mem.InMemoryNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeMovingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;

import static pl.exsio.nestedj.model.NestedNode.*;

public class InMemoryNestedNodeMovingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends InMemoryNestedNodeQueryDelegate<ID, N>
        implements NestedNodeMovingQueryDelegate<ID, N> {

    private final static Long MARKING_MODIFIER = 1000L;

    private enum Mode {
        UP, DOWN
    }

    public InMemoryNestedNodeMovingQueryDelegate(InMemoryNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }


    @Override
    public Integer markNodeIds(NestedNodeInfo<ID> node) {
        return Math.toIntExact(nodesStream()
                .filter(n -> getLong(LEFT, n) >= node.getLeft())
                .filter(n -> getLong(RIGHT, n) <= node.getRight())
                .peek(n -> setLong(RIGHT, n, negate(getLong(RIGHT, n)) - MARKING_MODIFIER))
                .count());
    }

    @Override
    public void updateSideFieldsUp(Long delta, Long start, Long stop, String field) {
        updateFields(Mode.UP, delta, start, stop, field);
    }

    @Override
    public void updateSideFieldsDown(Long delta, Long start, Long stop, String field) {
        updateFields(Mode.DOWN, delta, start, stop, field);
    }

    @Override
    public void performMoveUp(Long nodeDelta, Long levelModificator) {
        performMove(Mode.UP, nodeDelta, levelModificator);
    }

    @Override
    public void performMoveDown(Long nodeDelta, Long levelModificator) {
        performMove(Mode.DOWN, nodeDelta, levelModificator);
    }

    @Override
    public void updateParentField(ID newParentId, NestedNodeInfo<ID> node) {
        Preconditions.checkNotNull(newParentId);
        doUpdateParentField(newParentId, node);
    }

    @Override
    public void clearParentField(NestedNodeInfo<ID> node) {
        doUpdateParentField(null, node);
    }

    private void updateFields(Mode mode, Long delta, Long start, Long stop, String field) {
        nodesStream()
                .filter(n -> getLong(field, n) > start)
                .filter(n -> getLong(field, n) < stop)
                .forEach(n -> {
                    if (Mode.DOWN.equals(mode)) {
                        setLong(field, n, getLong(field, n) - delta);
                    } else if (Mode.UP.equals(mode)) {
                        setLong(field, n, getLong(field, n) + delta);
                    }
                });
    }

    private void performMove(Mode mode, Long nodeDelta, Long levelModificator) {
        nodesStream()
                .filter(n -> getLong(RIGHT, n) < 0)
                .forEach(n -> {
                    setLong(LEVEL, n, getLong(LEVEL, n) + levelModificator);
                    Long right = negate(getLong(RIGHT, n) + MARKING_MODIFIER);
                    if (Mode.DOWN.equals(mode)) {
                        setLong(RIGHT, n, right - nodeDelta);
                        setLong(LEFT, n, getLong(LEFT, n) - nodeDelta);
                    } else if (Mode.UP.equals(mode)) {
                        setLong(RIGHT, n, right + nodeDelta);
                        setLong(LEFT, n, getLong(LEFT, n) + nodeDelta);
                    }
                });
    }

    private void doUpdateParentField(ID newParentId, NestedNodeInfo<ID> node) {
        nodesStream()
                .filter(n -> getSerializable(ID, n).equals(node.getId()))
                .forEach(n -> n.setParentId(newParentId));
    }

    private Long negate(Long value) {
        return value * -1;
    }
}
