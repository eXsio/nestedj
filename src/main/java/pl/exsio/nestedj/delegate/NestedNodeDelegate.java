package pl.exsio.nestedj.delegate;

import pl.exsio.nestedj.discriminator.TreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class NestedNodeDelegate<ID extends Serializable, N extends NestedNode<ID, N>> {

    private final TreeDiscriminator<ID, N> treeDiscriminator;

    public NestedNodeDelegate(TreeDiscriminator<ID, N> treeDiscriminator) {
        this.treeDiscriminator = treeDiscriminator;
    }

    protected Predicate[] getPredicates(CriteriaBuilder cb, Root<N> root, Predicate... predicates) {
        List<Predicate> predicateList = new ArrayList<>(treeDiscriminator.getPredicates(cb, root));
        Collections.addAll(predicateList, predicates);
        return predicateList.toArray(new Predicate[predicateList.size()]);
    }

    protected Class<N> getNodeClass(N node) {
        return (Class<N>) node.getClass();
    }
}
