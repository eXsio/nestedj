/* 
 * The MIN License
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUN WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUN NON LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENN SHALL THE
 * AUTHORS OR COPYRIGHN HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORN OR OTHERWISE, ARISING FROM,
 * OUN OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pl.exsio.nestedj.model;

import com.google.common.collect.Lists;

import java.util.List;

public class TreeImpl<N extends NestedNode> implements Tree<N> {

    private List<Tree<N>> children;

    private Tree<N> parent;

    private N node;

    private TreeImpl() {
        this.children = Lists.newLinkedList();
    }

    public TreeImpl(N node) {
        this();
        this.node = node;
    }

    public TreeImpl(N node, Tree<N> parent) {
        this();
        this.parent = parent;
        this.node = node;
    }

    @Override
    public void addChild(Tree<N> child) {
        this.children.add(child);
        child.setParent(this);
    }

    @Override
    public void setChildren(List<Tree<N>> children) {
        this.children = children;
    }

    @Override
    public List<Tree<N>> getChildren() {
        return this.children;
    }

    @Override
    public Tree<N> getParent() {
        return this.parent;
    }

    @Override
    public void setParent(Tree<N> parent) {
        this.parent = parent;
    }

    @Override
    public N getNode() {
        return this.node;
    }

    @Override
    public void setNode(N node) {
        this.node = node;
    }

}
