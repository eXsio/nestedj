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
import pl.exsio.nestedj.annotation.LeftColumn;
import pl.exsio.nestedj.annotation.LevelColumn;
import pl.exsio.nestedj.annotation.ParentColumn;
import pl.exsio.nestedj.annotation.RightColumn;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "nested_nodes")
@Inheritance(strategy = InheritanceType.JOINED)
public class TestNodeImpl extends DummyObject implements NestedNode<TestNodeImpl> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @Column(name = "node_name", nullable = false)
    protected String name;

    @LeftColumn
    @Column(name = "tree_left", nullable = false)
    protected Long lft;

    @RightColumn
    @Column(name = "tree_right", nullable = false)
    protected Long rgt;

    @LevelColumn
    @Column(name = "tree_level", nullable = false)
    protected Long lvl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id", nullable = true)
    @ParentColumn
    protected TestNodeImpl parent;

    @Column(name = "discriminator", nullable = false)
    protected String discriminator;

    public TestNodeImpl() {
        super();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Long getLeft() {
        return lft;
    }

    @Override
    public Long getRight() {
        return rgt;
    }

    @Override
    public Long getLevel() {
        return lvl;
    }


    public String getDiscriminator() {
        return discriminator;
    }

    public String getName() {
        return name;
    }

    public TestNodeImpl setName(String name) {
        this.name = name;
        return this;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    @Override
    public TestNodeImpl getParent() {
        return parent;
    }

    @Override
    public void setLeft(Long left) {
        this.lft = left;
    }

    @Override
    public void setRight(Long right) {
        this.rgt = right;
    }

    @Override
    public void setLevel(Long level) {
        this.lvl = level;
    }

    @Override
    public void setParent(TestNodeImpl parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("lft", lft)
                .add("rgt", rgt)
                .add("lvl", lvl)
                .add("parent", parent != null ? parent.getId() : "null")
                .add("discriminator", discriminator)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TestNodeImpl) {
            return (this.hashCode() == o.hashCode());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.getId().intValue();
    }
}
