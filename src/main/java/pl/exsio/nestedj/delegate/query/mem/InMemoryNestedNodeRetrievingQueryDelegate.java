package pl.exsio.nestedj.delegate.query.mem;

import pl.exsio.nestedj.config.mem.InMemoryNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeRetrievingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static pl.exsio.nestedj.model.NestedNode.*;

public class InMemoryNestedNodeRetrievingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends InMemoryNestedNodeQueryDelegate<ID, N>
        implements NestedNodeRetrievingQueryDelegate<ID, N> {

    public InMemoryNestedNodeRetrievingQueryDelegate(InMemoryNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }


    @Override
    public List<N> getTreeAsList(N node) {
        return nodesStream()
                .filter(n -> getLong(LEFT, n) >= node.getTreeLeft())
                .filter(n -> getLong(RIGHT, n) <= node.getTreeRight())
                .sorted(Comparator.comparing(NestedNode::getTreeLeft))
                .collect(Collectors.toList());
    }

    @Override
    public List<N> getChildren(N node) {
        return nodesStream()
                .filter(n -> getLong(LEFT, n) >= node.getTreeLeft())
                .filter(n -> getLong(RIGHT, n) <= node.getTreeRight())
                .filter(n -> getLong(LEVEL, n).equals(node.getTreeLevel() + 1))
                .sorted(Comparator.comparing(NestedNode::getTreeLeft))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<N> getParent(N node) {
        if (node.getTreeLevel() > 0) {
            return nodesStream()
                    .filter(n -> getLong(LEFT, n) < node.getTreeLeft())
                    .filter(n -> getLong(RIGHT, n) > node.getTreeRight())
                    .filter(n -> getLong(LEVEL, n).equals(node.getTreeLevel() - 1))
                    .min(Comparator.comparing(NestedNode::getTreeLeft));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<N> getParents(N node) {
        return nodesStream()
                .filter(n -> getLong(LEFT, n) < node.getTreeLeft())
                .filter(n -> getLong(RIGHT, n) > node.getTreeRight())
                .sorted(Comparator.<N, Long>comparing(NestedNode::getTreeLeft).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<N> getPrevSibling(N node) {
        return nodesStream()
                .filter(n -> getLong(RIGHT, n).equals(node.getTreeLeft() - 1))
                .filter(n -> getLong(LEVEL, n).equals(node.getTreeLevel()))
                .min(Comparator.comparing(NestedNode::getTreeLeft));
    }

    @Override
    public Optional<N> getNextSibling(N node) {
        return nodesStream()
                .filter(n -> getLong(LEFT, n).equals(node.getTreeRight() + 1))
                .filter(n -> getLong(LEVEL, n).equals(node.getTreeLevel()))
                .min(Comparator.comparing(NestedNode::getTreeLeft));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<NestedNodeInfo<ID>> getNodeInfo(ID nodeId) {
        Optional<N> node = nodesStream()
                .filter(n -> getSerializable(ID, n).equals(nodeId))
                .findFirst();
        return node.map(n -> new NestedNodeInfo<>(
                        (ID) getSerializable(ID, n),
                        (ID) getSerializable(PARENT_ID, n),
                        getLong(LEFT, n),
                        getLong(RIGHT, n),
                        getLong(LEVEL, n)
                ));
    }

    @Override
    public Optional<N> findFirstRoot() {
        return nodesStream()
                .filter(n -> getLong(LEVEL, n).equals(0L))
                .min(Comparator.comparing(NestedNode::getTreeLeft));
    }

    @Override
    public Optional<N> findLastRoot() {
        return nodesStream()
                .filter(n -> getLong(LEVEL, n).equals(0L))
                .max(Comparator.comparing(NestedNode::getTreeLeft));
    }
}
