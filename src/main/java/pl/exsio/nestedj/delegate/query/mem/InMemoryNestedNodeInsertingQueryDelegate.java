/*
 *  The MIT License
 *
 *  Copyright (c) 2019 eXsio.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 *  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 *  the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 *  BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package pl.exsio.nestedj.delegate.query.mem;

import pl.exsio.nestedj.config.mem.InMemoryNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.query.NestedNodeInsertingQueryDelegate;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;


public class InMemoryNestedNodeInsertingQueryDelegate<ID extends Serializable, N extends NestedNode<ID>>
        extends InMemoryNestedNodeQueryDelegate<ID, N>
        implements NestedNodeInsertingQueryDelegate<ID, N> {


    public InMemoryNestedNodeInsertingQueryDelegate(InMemoryNestedNodeRepositoryConfiguration<ID, N> configuration) {
        super(configuration);
    }

    @Override
    public void insert(N node) {
        if (node.getId() == null) {
            doInsert(node);
        } else {
            update(node);
        }
    }

    private void update(N node) {
        nodesStream()
                .filter(n -> getSerializable(NestedNode.ID, n).equals(node.getId()))
                .forEach(n -> {
                    n.setTreeLevel(node.getTreeLevel());
                    n.setTreeLeft(node.getTreeLeft());
                    n.setTreeRight(node.getTreeRight());
                    n.setParentId(node.getParentId());
                });
    }

    private void doInsert(N node) {
        ID newId = generateIdentity();
        node.setId(newId);
        nodes.add(node);
    }

    @Override
    public void incrementSideFieldsGreaterThan(Long from, String fieldName) {
        nodesStream()
                .filter(n -> getLong(fieldName, n) > from)
                .forEach(n -> setLong(fieldName, n, getLong(fieldName, n) + INCREMENT_BY));
    }

    @Override
    public void incermentSideFieldsGreaterThanOrEqualTo(Long from, String fieldName) {
        nodesStream()
                .filter(n -> getLong(fieldName, n) >= from)
                .forEach(n -> setLong(fieldName, n, getLong(fieldName, n) + INCREMENT_BY));
    }

}
