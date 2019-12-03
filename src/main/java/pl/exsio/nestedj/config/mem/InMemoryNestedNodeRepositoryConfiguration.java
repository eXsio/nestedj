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

package pl.exsio.nestedj.config.mem;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import pl.exsio.nestedj.config.mem.discriminator.InMemoryTreeDiscriminator;
import pl.exsio.nestedj.config.mem.identity.InMemoryNestedNodeIdentityGenerator;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * Configuration class that serves as a base of creating new instances of InMemory Repository.
 *
 * @param <ID> - Nested Node Identifier Class
 * @param <N> - Nested Node Class
 */
public class InMemoryNestedNodeRepositoryConfiguration<ID extends Serializable, N extends NestedNode<ID>> {

    private final InMemoryTreeDiscriminator<ID, N> treeDiscriminator;

    private final InMemoryNestedNodeIdentityGenerator<ID> identityGenerator;

    private final Set<N> nodes = Sets.newConcurrentHashSet();

    /**
     * Creates new InMemory Repository with empty Tree and no Tree Discriminator.
     *
     * @param identityGenerator - Identity generator used for inserting new Nodes into an InMemory Repository.
     */
    public InMemoryNestedNodeRepositoryConfiguration(InMemoryNestedNodeIdentityGenerator<ID> identityGenerator) {
        this(identityGenerator, Lists.newArrayList(), null);
    }

    /**
     * Creates new InMemory Repository with a collection of Nodes and no Tree Discriminator.
     * If the Nodes do not have proper LEFT/RIGHT/LEVEL values, Tree can be initialized with NestedNodeRepository::rebuildTree() method.
     *
     * @param identityGenerator - Identity generator used for inserting new Nodes into an InMemory Repository.
     * @param nodes - initial collection of Nodes
     */
    public InMemoryNestedNodeRepositoryConfiguration(InMemoryNestedNodeIdentityGenerator<ID> identityGenerator, Collection<N> nodes) {
       this(identityGenerator, nodes, null);
    }

    /**
     * Creates new InMemory Repository with a collection of Nodes and custom Tree Discriminator.
     * If the Nodes do not have proper LEFT/RIGHT/LEVEL values, Tree can be initialized with NestedNodeRepository::rebuildTree() method.
     *
     * @param identityGenerator - Identity generator used for inserting new Nodes into an InMemory Repository.
     * @param nodes - initial collection of Nodes
     * @param treeDiscriminator - custom Tree Discriminator
     */
    public InMemoryNestedNodeRepositoryConfiguration(InMemoryNestedNodeIdentityGenerator<ID> identityGenerator, Collection<N> nodes, InMemoryTreeDiscriminator<ID, N> treeDiscriminator) {
        this.identityGenerator = identityGenerator;
        this.treeDiscriminator = treeDiscriminator;
        this.nodes.addAll(nodes);
    }

    /**
     * @return Tree Discriminator used by this Configuration
     */
    public InMemoryTreeDiscriminator<ID, N> getTreeDiscriminator() {
        return treeDiscriminator;
    }

    /**
     * @return Identiy Generator used by this Configuration
     */
    public InMemoryNestedNodeIdentityGenerator<ID> getIdentityGenerator() {
        return identityGenerator;
    }

    /**
     *
     * This method can be used to retrieve the data structure backing the InMemory Repository.
     * You can store the collection to (no)SQL storage or use it for custom data retrieval logic.
     * It is not recommended to manually modify the LEFT/RIGHT/LEVEL values of the Nodes contained in the returned Set.
     *
     * @return flat Set of Nodes - the data structure backing the InMemory implementation.
     */
    public Set<N> getNodes() {
        return nodes;
    }
}
