package pl.exsio.nestedj.lock;

import pl.exsio.nestedj.NestedNodeRepository;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;

/**
 *  Default no-op Lock implementation
 *
 * @param <ID> - Nested Node Identifier Class
 * @param <N> - Nested Node Class
 */
public class NoLock<ID extends Serializable, N extends NestedNode<ID>> implements NestedNodeRepository.Lock<ID, N> {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean lockNode(N node) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unlockNode(N node) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean lockRepository() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unlockRepository() {
    }
}
