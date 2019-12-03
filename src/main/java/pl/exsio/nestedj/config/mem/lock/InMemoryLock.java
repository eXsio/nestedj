package pl.exsio.nestedj.config.mem.lock;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import pl.exsio.nestedj.NestedNodeRepository;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * In Memory Lock - stores a Set of Lock Handles to determine whether a Node can be modified or not.
 * It is up to the user to provide the Lock Handle Object taht fits the needs. The Handle should have a proper
 * equals() and hashCode() to be correctly stored and removed in/from Set.
 *
 * Additionally the Lock check whether the whole Repository is locked or not. Locked repository takes precedence before
 * locking single Node.
 *
 * @param <ID> - Nested Node Identier Class
 * @param <N> - Nested Node Class
 */
public class InMemoryLock<ID extends Serializable, N extends NestedNode<ID>> implements NestedNodeRepository.Lock<ID, N> {

    private final AtomicBoolean repositoryLocked = new AtomicBoolean(false);

    private final Set<Object> lockHandles = Sets.newConcurrentHashSet();

    private final Function<N, Object> lockHandleProvider;

    public InMemoryLock() {
        this.lockHandleProvider = null;
    }

    public InMemoryLock(Function<N, Object> lockHandleProvider) {
        Preconditions.checkNotNull(lockHandleProvider);
        this.lockHandleProvider = lockHandleProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean lockNode(N node) {
        if (repositoryLocked.get()) {
            return false;
        }
        if(lockHandleProvider == null) {
            return lockRepository();
        }

        Object handle = lockHandleProvider.apply(node);
        if (lockHandles.contains(handle)) {
            return false;
        }
        lockHandles.add(handle);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void unlockNode(N node) {
        if(lockHandleProvider == null) {
            unlockRepository();
        } else {
            lockHandles.remove(lockHandleProvider.apply(node));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean lockRepository() {
        if (repositoryLocked.get()) {
            return false;
        }
        repositoryLocked.set(true);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void unlockRepository() {
        repositoryLocked.set(false);
    }
}
