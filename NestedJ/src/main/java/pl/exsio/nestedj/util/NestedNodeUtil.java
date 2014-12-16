/* 
 * The MIT License
 *
 * Copyright 2014 exsio.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pl.exsio.nestedj.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.Id;
import pl.exsio.nestedj.annotation.LeftColumn;
import pl.exsio.nestedj.annotation.LevelColumn;
import pl.exsio.nestedj.annotation.ParentColumn;
import pl.exsio.nestedj.annotation.RightColumn;
import pl.exsio.nestedj.config.NestedNodeConfig;
import pl.exsio.nestedj.config.NestedNodeConfigImpl;
import pl.exsio.nestedj.model.NestedNode;

/**
 *
 * @author exsio
 * @param <T>
 */
public class NestedNodeUtil {

    protected static final Map<Class<? extends NestedNode>, NestedNodeConfig> configs;

    static {
        configs = new HashMap<Class<? extends NestedNode>, NestedNodeConfig>();
    }

    public static boolean isNodeNew(NestedNode node) {
        return (node.getLeft() == null && node.getRight() == null);
    }

    public static NestedNodeConfig getNodeConfig(Class<? extends NestedNode> nodeClass) {
        if (!configs.containsKey(nodeClass)) {
            NestedNodeConfigImpl config = new NestedNodeConfigImpl();

            Entity entity = nodeClass.getAnnotation(Entity.class);
            String name = entity.name();
            config.setEntityName((name != null && name.length() > 0) ? name : nodeClass.getSimpleName());
            config = getConfigForClass(nodeClass, config);
            configs.put(nodeClass, config);
        }

        return configs.get(nodeClass);
    }

    private static NestedNodeConfigImpl getConfigForClass(Class nodeClass, NestedNodeConfigImpl config) {

        if (!nodeClass.getCanonicalName().equals(Object.class.getCanonicalName())) {
            for (Field field : nodeClass.getDeclaredFields()) {
                if (field.getAnnotation(LeftColumn.class) != null && config.getLeftFieldName() == null) {
                    config.setLeftFieldName(field.getName());
                } else if (field.getAnnotation(RightColumn.class) != null && config.getRightFieldName() == null) {
                    config.setRightFieldName(field.getName());
                } else if (field.getAnnotation(LevelColumn.class) != null && config.getLevelFieldName() == null) {
                    config.setLevelFieldName(field.getName());
                } else if (field.getAnnotation(ParentColumn.class) != null && config.getParentFieldName() == null) {
                    config.setParentFieldName(field.getName());
                } else if (field.getAnnotation(Id.class) != null && config.getIdFieldName() == null) {
                    config.setIdFieldName(field.getName());
                }
            }
            config = getConfigForClass(nodeClass.getSuperclass(), config);
        }
        return config;
    }

    public static String left(Class<? extends NestedNode> nodeClass) {
        return getNodeConfig(nodeClass).getLeftFieldName();
    }

    public static String right(Class<? extends NestedNode> nodeClass) {
        return getNodeConfig(nodeClass).getRightFieldName();
    }

    public static String level(Class<? extends NestedNode> nodeClass) {
        return getNodeConfig(nodeClass).getLevelFieldName();
    }

    public static String parent(Class<? extends NestedNode> nodeClass) {
        return getNodeConfig(nodeClass).getParentFieldName();
    }

    public static String id(Class<? extends NestedNode> nodeClass) {
        return getNodeConfig(nodeClass).getIdFieldName();
    }

    public static String entity(Class<? extends NestedNode> nodeClass) {
        return getNodeConfig(nodeClass).getEntityName();
    }

}
