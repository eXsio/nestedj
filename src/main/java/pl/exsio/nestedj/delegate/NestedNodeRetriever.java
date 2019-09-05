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
package pl.exsio.nestedj.delegate;

import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;
import pl.exsio.nestedj.model.Tree;

import java.io.Serializable;
import java.util.Optional;

public interface NestedNodeRetriever<ID extends Serializable, N extends NestedNode<ID>> {

    Iterable<N> getTreeAsList(N node, Class<N> nodeClass);

    Iterable<N> getChildren(N node, Class<N> nodeClass);

    Optional<N> getParent(N node, Class<N> nodeClass);

    Optional<N> getPrevSibling(N node, Class<N> nodeClass);

    Optional<N> getNextSibling(N node, Class<N> nodeClass);

    Tree<ID, N> getTree(N node, Class<N> nodeClass);

    Iterable<N> getParents(N node, Class<N> nodeClass);

    Optional<NestedNodeInfo<ID, N>> getNodeInfo(ID nodeId, Class<N> nodeClass, Class<ID> idClass);
}
