package pl.exsio.nestedj;

import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.config.NestedNodeConfig;

/**
 *
 * @author exsio
 * @param <T>
 */
public interface NestedNodeUtil<T extends NestedNode> {

    boolean isNodeNew(T node);

    NestedNodeConfig getNodeConfig(Class<? extends NestedNode> nodeClass);
    
}
