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
package pl.exsio.nestedj.model;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author exsio
 * @param <T>
 */
public class TreeImpl<T extends NestedNode> implements Tree<T> {

    private List<Tree<T>> children;

    private Tree<T> parent;

    private T node;
    
    private TreeImpl() {
        this.children = new LinkedList<Tree<T>>();
    }

    public TreeImpl(T node) {
        this();
        this.node = node;
    }

    public TreeImpl(T node, Tree<T> parent) {
        this();
        this.parent = parent;
        this.node = node;
    }
    
    @Override
    public void addChild(Tree<T> child) {
        this.children.add(child);
        child.setParent(this);
    }

    @Override
    public void setChildren(List<Tree<T>> children) {
        this.children = children;
    }

    @Override
    public List<Tree<T>> getChildren() {
        return this.children;
    }

    @Override
    public Tree<T> getParent() {
        return this.parent;
    }

    @Override
    public void setParent(Tree<T> parent) {
        this.parent = parent;
    }

    @Override
    public T getNode() {
        return this.node;
    }

    @Override
    public void setNode(T node) {
        this.node = node;
    }

}
