package pl.exsio.nestedj.delegate.query.mem;

import pl.exsio.nestedj.config.mem.InMemoryNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeInsertingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;


public class InMemoryNestedNodeInsertingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends InMemoryNestedNodeQueryDelegate<ID, N>
        implements NestedNodeInsertingQueryDelegate<ID, N> {


    public InMemoryNestedNodeInsertingQueryDelegate(InMemoryNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }

    @Override
    public void insert(N node) {
        if (node.getId() == null) {
            doInsert(node);
        } else {
            update(node);
        }
    }

    private void update(N node) {
        nodesStream()
                .filter(n -> getSerializable(NestedNode.ID, n).equals(node.getId()))
                .forEach(n -> {
                    n.setTreeLevel(node.getTreeLevel());
                    n.setTreeLeft(node.getTreeLeft());
                    n.setTreeRight(node.getTreeRight());
                    n.setParentId(node.getParentId());
                });
    }

    private void doInsert(N node) {
        ID newId = generateIdentity();
        node.setId(newId);
        nodes.add(node);
    }

    @Override
    public void incrementSideFieldsGreaterThan(Long from, String fieldName) {
        nodesStream()
                .filter(n -> getLong(fieldName, n) > from)
                .forEach(n -> setLong(fieldName, n, getLong(fieldName, n) + INCREMENT_BY));
    }

    @Override
    public void incermentSideFieldsGreaterThanOrEqualTo(Long from, String fieldName) {
        nodesStream()
                .filter(n -> getLong(fieldName, n) >= from)
                .forEach(n -> setLong(fieldName, n, getLong(fieldName, n) + INCREMENT_BY));
    }

}
