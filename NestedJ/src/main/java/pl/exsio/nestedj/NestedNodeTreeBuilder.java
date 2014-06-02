package pl.exsio.nestedj;

import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.Tree;

/**
 *
 * @author exsio
 * @param <T>
 */
public interface NestedNodeTreeBuilder<T extends NestedNode> {
    
    Tree<T> build(T node);
}
