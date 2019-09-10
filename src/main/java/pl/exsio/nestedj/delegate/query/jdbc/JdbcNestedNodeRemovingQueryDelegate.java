package pl.exsio.nestedj.delegate.query.jdbc;

import pl.exsio.nestedj.config.jdbc.JdbcNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeRemovingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import java.io.Serializable;

public class JdbcNestedNodeRemovingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends JdbcNestedNodeQueryDelegate<ID, N>
        implements NestedNodeRemovingQueryDelegate<ID, N> {

    public JdbcNestedNodeRemovingQueryDelegate(JdbcNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }


    @Override
    public void setNewParentForDeletedNodesChildren(NestedNodeInfo<ID> node) {

    }

    @Override
    public void performSingleDeletion(NestedNodeInfo<ID> node) {

    }

    @Override
    public void decrementSideFieldsBeforeSingleNodeRemoval(Long from, String field) {

    }

    @Override
    public void pushUpDeletedNodesChildren(NestedNodeInfo<ID> node) {

    }

    @Override
    public void decrementSideFieldsAfterSubtreeRemoval(Long from, Long delta, String field) {

    }

    @Override
    public void performBatchDeletion(NestedNodeInfo<ID> node) {

    }
}
