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
package pl.exsio.nestedj.delegate.impl;

import pl.exsio.nestedj.delegate.NestedNodeRetriever;
import pl.exsio.nestedj.delegate.query.NestedNodeRetrievingQueryDelegate;
import pl.exsio.nestedj.model.InMemoryTree;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;
import pl.exsio.nestedj.model.Tree;

import java.io.Serializable;
import java.util.Optional;

public class QueryBasedNestedNodeRetriever<ID extends Serializable, N extends NestedNode<ID>> implements NestedNodeRetriever<ID, N> {

    private final NestedNodeRetrievingQueryDelegate<ID, N> queryDelegate;

    public QueryBasedNestedNodeRetriever(NestedNodeRetrievingQueryDelegate<ID, N> queryDelegate) {
        this.queryDelegate = queryDelegate;
    }

    @Override
    public Tree<ID, N> getTree(N node) {
        Tree<ID, N> tree = new InMemoryTree<>(node);
        for (N n : queryDelegate.getChildren(node)) {
            Tree<ID, N> subtree = this.getTree(n);
            tree.addChild(subtree);
        }
        return tree;
    }

    @Override
    public Iterable<N> getTreeAsList(N node) {
        return queryDelegate.getTreeAsList(node);
    }

    @Override
    public Iterable<N> getChildren(N node) {
        return queryDelegate.getChildren(node);
    }

    @Override
    public Optional<N> getParent(N node) {
        return queryDelegate.getParent(node);
    }

    @Override
    public Iterable<N> getParents(N node) {
        return queryDelegate.getParents(node);
    }

    @Override
    public Optional<N> getPrevSibling(N node) {
        return queryDelegate.getPrevSibling(node);
    }

    @Override
    public Optional<N> getNextSibling(N node) {
        return queryDelegate.getNextSibling(node);
    }

    @Override
    public Optional<NestedNodeInfo<ID>> getNodeInfo(ID nodeId) {
        return queryDelegate.getNodeInfo(nodeId);
    }

}
