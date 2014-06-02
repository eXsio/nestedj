package pl.exsio.nestedj.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Entity;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.NestedNodeUtil;
import pl.exsio.nestedj.annotation.LeftColumn;
import pl.exsio.nestedj.annotation.LevelColumn;
import pl.exsio.nestedj.annotation.ParentColumn;
import pl.exsio.nestedj.annotation.RightColumn;
import pl.exsio.nestedj.config.NestedNodeConfigImpl;
import pl.exsio.nestedj.config.NestedNodeConfig;

/**
 *
 * @author exsio
 * @param <T>
 */
public class NestedNodeUtilImpl<T extends NestedNode> implements NestedNodeUtil<T> {

    protected Map<Class<? extends NestedNode>, NestedNodeConfig> configs;

    public NestedNodeUtilImpl() {
        this.configs = new HashMap<Class<? extends NestedNode>, NestedNodeConfig>();
    }

    @Override
    public boolean isNodeNew(T node) {
        return (node.getLeft() == null && node.getRight() == null);
    }

    @Override
    public NestedNodeConfig getNodeConfig(Class<? extends NestedNode> nodeClass) {
        if (!this.configs.containsKey(nodeClass)) {
            NestedNodeConfigImpl config = new NestedNodeConfigImpl();

            Entity entity = nodeClass.getAnnotation(Entity.class);
            String name = entity.name();
            config.setEntityName((name != null && name.length() > 0) ? name : nodeClass.getSimpleName());

            for (Field field : nodeClass.getDeclaredFields()) {
                if (field.getAnnotation(LeftColumn.class) != null) {
                    config.setLeftFieldName(field.getName());
                } else if (field.getAnnotation(RightColumn.class) != null) {
                    config.setRightFieldName(field.getName());
                } else if (field.getAnnotation(LevelColumn.class) != null) {
                    config.setLevelFieldName(field.getName());
                } else if (field.getAnnotation(ParentColumn.class) != null) {
                    config.setParentFieldName(field.getName());
                }
            }

            this.configs.put(nodeClass, config);
        }

        return this.configs.get(nodeClass);
    }

}
