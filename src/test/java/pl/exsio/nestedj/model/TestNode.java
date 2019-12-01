/*
 * The MIT License
 *
 * Copyright 2015 exsio.
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
package pl.exsio.nestedj.model;

import com.google.common.base.MoreObjects;

import javax.persistence.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

@Entity
@Table(name = "nested_nodes")
@SequenceGenerator(name="seq", initialValue=1, allocationSize=1)
public class TestNode implements NestedNode<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    private Long id;

    @Column(name = "node_name", nullable = false, unique = true)
    private String name;

    @Column(name = "tree_left", nullable = false)
    private Long treeLeft;

    @Column(name = "tree_right", nullable = false)
    private Long treeRight;

    @Column(name = "tree_level", nullable = false)
    private Long treeLevel;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "discriminator", nullable = false)
    private String discriminator;

    public TestNode() {
    }

    public TestNode(Long id, Long treeLeft, Long treeLevel, Long treeRight, String name, Long parentId, String discriminator) {
        this.id = id;
        this.name = name;
        this.treeLeft = treeLeft;
        this.treeRight = treeRight;
        this.treeLevel = treeLevel;
        this.parentId = parentId;
        this.discriminator = discriminator;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Long getTreeLeft() {
        return treeLeft;
    }

    @Override
    public Long getTreeRight() {
        return treeRight;
    }

    @Override
    public Long getTreeLevel() {
        return treeLevel;
    }

    @Override
    public Long getParentId() {
        return parentId;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public String getName() {
        return name;
    }

    public TestNode setName(String name) {
        this.name = name;
        return this;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    @Override
    public void setTreeLeft(Long treeLeft) {
        this.treeLeft = treeLeft;
    }

    @Override
    public void setTreeRight(Long treeRight) {
        this.treeRight = treeRight;
    }

    @Override
    public void setTreeLevel(Long treeLevel) {
        this.treeLevel = treeLevel;
    }

    @Override
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("treeLeft", treeLeft)
                .add("treeRight", treeRight)
                .add("treeLevel", treeLevel)
                .add("parentId", parentId)
                .add("discriminator", discriminator)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestNode)) return false;
        TestNode testNode = (TestNode) o;
        return Objects.equals(getId(), testNode.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public static TestNode fromResultSet(ResultSet resultSet) throws SQLException {
        TestNode n = new TestNode();
        n.setId(resultSet.getLong("ID"));
        n.setTreeLeft(resultSet.getLong("TREE_LEFT"));
        n.setTreeLevel(resultSet.getLong("TREE_LEVEL"));
        n.setTreeRight(resultSet.getLong("TREE_RIGHT"));
        n.setName(resultSet.getString("NODE_NAME"));
        n.setParentId(resultSet.getLong("PARENT_ID"));
        if(resultSet.wasNull()) {
            n.setParentId(null);
        }
        n.setDiscriminator(resultSet.getString("DISCRIMINATOR"));
        return n;
    }

    public TestNode copy() {
        return new TestNode(id, treeLeft, treeLevel, treeRight, name, parentId, discriminator);
    }
}
