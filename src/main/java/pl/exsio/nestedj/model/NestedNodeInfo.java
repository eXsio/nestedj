package pl.exsio.nestedj.model;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

public class NestedNodeInfo<ID extends Serializable, N extends NestedNode<ID>> {

    private final ID id;

    private final ID parentId;

    private final Long left;

    private final Long right;

    private final Long level;

    private Class<N> nodeClass;

    private Class<ID> idClass;

    public NestedNodeInfo(ID id, ID parentId, Long left, Long right, Long level) {
        this.id = id;
        this.parentId = parentId;
        this.left = left;
        this.right = right;
        this.level = level;
    }

    public ID getId() {
        return id;
    }

    public ID getParentId() {
        return parentId;
    }

    public Long getLeft() {
        return left;
    }

    public Long getRight() {
        return right;
    }

    public Long getLevel() {
        return level;
    }

    public Class<N> getNodeClass() {
        return nodeClass;
    }

    public void setNodeClass(Class<N> nodeClass) {
        this.nodeClass = nodeClass;
    }

    public Class<ID> getIdClass() {
        return idClass;
    }

    public void setIdClass(Class<ID> idClass) {
        this.idClass = idClass;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("parentId", parentId)
                .add("left", left)
                .add("right", right)
                .add("level", level)
                .add("nodeClass", nodeClass)
                .add("idClass", idClass)
                .toString();
    }
}
