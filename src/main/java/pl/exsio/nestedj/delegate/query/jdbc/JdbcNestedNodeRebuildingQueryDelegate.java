package pl.exsio.nestedj.delegate.query.jdbc;

import pl.exsio.nestedj.config.jdbc.JdbcNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeRebuildingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;
import java.util.List;

public class JdbcNestedNodeRebuildingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends JdbcNestedNodeQueryDelegate<ID, N>
        implements NestedNodeRebuildingQueryDelegate<ID, N> {

    public JdbcNestedNodeRebuildingQueryDelegate(JdbcNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }


    @Override
    public void destroyTree() {

    }

    @Override
    public N findFirst() {
        return null;
    }

    @Override
    public void resetFirst(N first) {

    }

    @Override
    public List<N> getSiblings(ID first) {
        return null;
    }

    @Override
    public List<N> getChildren(N parent) {
        return null;
    }
}
