package pl.exsio.nestedj.lock;

import pl.exsio.nestedj.NestedNodeRepository;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;

public class NoLock<ID extends Serializable, N extends NestedNode<ID>> implements NestedNodeRepository.Lock<ID, N> {
    @Override
    public boolean lockNode(N node) {
        return true;
    }

    @Override
    public void unlockNode(N node) {

    }

    @Override
    public boolean lockRepository() {
        return true;
    }

    @Override
    public boolean unlockRepository() {
        return false;
    }
}
