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

package pl.exsio.nestedj.config.mem.discriminator;

import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;

/**
 * Tree Discriminator for use with InMemory Repository implementation.
 * Allows to store multiple intependant Trees in one Repository/Collection.
 *
 * @param <ID> - Nested Node Identifier Class
 * @param <N> - Nested Node Class
 */
public interface InMemoryTreeDiscriminator<ID extends Serializable, N extends NestedNode<ID>> {

    /**
     * Method that decides whether a target Node belongs to the Tree or not.
     *
     * @param node - target Node
     * @return true if Node belongs to the Tree, false if Node does not belong to the Tree
     */
    boolean applies(N node);
}
