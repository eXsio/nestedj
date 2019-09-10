package pl.exsio.nestedj.model;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

public class NestedNodeInfo<ID extends Serializable> {

    private final ID id;

    private final ID parentId;

    private final Long left;

    private final Long right;

    private final Long level;

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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("parentId", parentId)
                .add("left", left)
                .add("right", right)
                .add("level", level)
                .toString();
    }
}
