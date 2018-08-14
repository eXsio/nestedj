package pl.exsio.nestedj.model;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

public class NestedNodeInfo<ID extends Serializable, N extends NestedNode<ID, N>> {

    private final ID id;

    private final Long parentId;

    private final Long left;

    private final Long right;

    private final Long level;

    private Class<N> nodeClass;

    public NestedNodeInfo(ID id, Long parentId, Long left, Long right, Long level) {
        this.id = id;
        this.parentId = parentId;
        this.left = left;
        this.right = right;
        this.level = level;
    }

    public ID getId() {
        return id;
    }

    public Long getParentId() {
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("parentId", parentId)
                .add("left", left)
                .add("right", right)
                .add("level", level)
                .add("nodeClass", nodeClass)
                .toString();
    }
}
