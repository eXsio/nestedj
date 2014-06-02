package pl.exsio.nestedj;

import pl.exsio.nestedj.model.NestedNode;

/**
 *
 * @author exsio
 */
public interface NestedNodeHierarchyManipulator<T extends NestedNode> {
    
    int MODE_FIRST_CHILD = 0;

    int MODE_LAST_CHILD = 1;

    int MODE_NEXT_SIBLING = 2;

    int MODE_PREV_SIBLING = 3;
   
}
