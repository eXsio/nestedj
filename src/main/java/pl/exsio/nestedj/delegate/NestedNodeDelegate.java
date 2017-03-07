package pl.exsio.nestedj.delegate;

import pl.exsio.nestedj.discriminator.TreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public abstract class NestedNodeDelegate<N extends NestedNode<N>> {

    private final TreeDiscriminator<N> treeDiscriminator;

    public NestedNodeDelegate(TreeDiscriminator<N> treeDiscriminator) {
        this.treeDiscriminator = treeDiscriminator;
    }

    protected Predicate[] getPredicates(CriteriaBuilder cb, Root<N> root, Predicate... predicates) {
        List<Predicate> predicateList = new ArrayList<>();
        predicateList.addAll(treeDiscriminator.getPredicates(cb, root));
        for (Predicate predicate : predicates) {
            predicateList.add(predicate);
        }
        return predicateList.toArray(new Predicate[predicateList.size()]);
    }

    protected Class<N> getNodeClass(N node) {
        return (Class<N>) node.getClass();
    }
}
