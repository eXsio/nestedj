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
package pl.exsio.nestedj;

import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.Tree;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 *  Primary NestedJ API. Serves as an entry point to all Tree manipulation and some common data retrieval actions.
 *  It does not cover every possible data retrieval scenarios - especially when joining the NestedNode Entity/Table/Object
 *  with other objects. If you have a requirement that is not covered by the Repository, feel free to experiment and use
 *  the Nested Set Model features to retrieve the data the way you want.
 *
 *  As for Tree manipulation, the Repository covers all scenarios and should be the only entry point. I you're missing a feature
 *  in that area, please consider raising a request here: https://github.com/eXsio/nestedj
 *
 * @param <ID> - Nested Node Identifier Class
 * @param <N> - Nested Node Class
 */
public interface NestedNodeRepository<ID extends Serializable, N extends NestedNode<ID>> {

    /**
     * Inserts or Updates (depending if id is null or not) the node as a first child of given parent.
     * If Repository nas a Tree Discriminator defined, only the Nodes belonging to that Tree wil be affected.
     *
     * @param node - target Node
     * @param parent - parent Node
     */
    void insertAsFirstChildOf(N node, N parent);

    /**
     * Inserts or Updates (depending if id is null or not) the node as a last child of given parent.
     * If Repository nas a Tree Discriminator defined, only the Nodes belonging to that Tree wil be affected.
     *
     * @param node - target Node
     * @param parent - parent Node
     */
    void insertAsLastChildOf(N node, N parent);

    /**
     * Inserts or Updates (depending if id is null or not) the node as a next sibling of given parent.
     * If Repository nas a Tree Discriminator defined, only the Nodes belonging to that Tree wil be affected.
     *
     * @param node - target Node
     * @param parent - parent Node
     */
    void insertAsNextSiblingOf(N node, N parent);

    /**
     * Inserts or Updates (depending if id is null or not) the node as a previous sibling of given parent.
     * If Repository nas a Tree Discriminator defined, only the Nodes belonging to that Tree wil be affected.
     *
     * @param node - target Node
     * @param parent - parent Node
     */
    void insertAsPrevSiblingOf(N node, N parent);

    /**
     * Removes Single Node. All Children/Descendants of the removed Node are assigned to the parent of the removed Node.
     * If Repository nas a Tree Discriminator defined, only the Nodes belonging to that Tree wil be affected.
     *
     * @param node - target Node
     */
    void removeSingle(N node);

    /**
     * Removes Node and cascades the operation to all Children/Descendants of the Node.
     * If Repository nas a Tree Discriminator defined, only the Nodes belonging to that Tree wil be affected.
     *
     * @param node - target Node
     */
    void removeSubtree(N node);

    /**
     * Returns a flat list of all Node's direct Children/Sescendants sorted by the LEFT asc.
     * If Repository nas a Tree Discriminator defined, only the Nodes belonging to that Tree wil be returned.
     *
     * @param node - parent Node
     * @return List of Child Nodes
     */
    List<N> getChildren(N node);

    /**
     * Returns a Parent Node. If the Node is a Root Node, returns empty.
     * If Repository nas a Tree Discriminator defined, only the Nodes belonging to that Tree wil be returned.
     *
     * @param node - target Node
     * @return Parent Node or empty if the Node is a Root Node
     */
    Optional<N> getParent(N node);

    /**
     * Returns previous Sibling or empty if the target Node is a first Child.
     * If Repository nas a Tree Discriminator defined, only the Nodes belonging to that Tree wil be returned.
     *
     * @param node - target Node
     * @return previous Sibling or empty if the target Node is a first Child
     */
    Optional<N> getPrevSibling(N node);

    /**
     * Returns next Sibling or empty if the target Node is a last Child.
     * If Repository nas a Tree Discriminator defined, only the Nodes belonging to that Tree wil be returned.
     *
     * @param node - target Node
     * @return next Sibling or empty if the target Node is a last Child
     */
    Optional<N> getNextSibling(N node);

    /**
     *  Returns a flat List of target Node's parents sorted from the deepest to the Root node, asc.
     *  If the target Node is a Root Node, returns empty List.
     *  If Repository nas a Tree Discriminator defined, only the Nodes belonging to that Tree wil be returned.
     *
     * @param node - target Node
     * @return list of target Node's parents
     */
    List<N> getParents(N node);

    /**
     * Returns a flat list of all Node's direct and indirect Children/Sescendants sorted by the LEFT asc.
     * If Repository nas a Tree Discriminator defined, only the Nodes belonging to that Tree wil be returned.
     *
     * @param node - parent Node
     * @return List of Child Nodes
     */
    List<N> getTreeAsList(N node);

    /**
     * Returns a recursive structure of all Node's direct and indirect Children/Sescendants.
     * Each level contains Nodes sorted by the LEFT asc.
     * If Repository nas a Tree Discriminator defined, only the Nodes belonging to that Tree wil be returned.
     *
     * @param node - parent Node
     * @return Recursive Tree of Child Nodes
     */
    Tree<ID, N> getTree(N node);

    /**
     * Rebuilds entire Tree based on parentId - id relationship. Useful when Tree was destroyed by an action .
     * performed outside of the Repository or if you want to initialize a new tree with Nodes previously created
     * by a batch operation outside of the Repository (like batch DB Insets).
     * All LEFT/RIGHT/LEVEL values will be properly assigned and the Tree will be sorted based on ID values.
     * If Repository nas a Tree Discriminator defined, only the Nodes belonging to that Tree wil be affected.
     */
    void rebuildTree();

    /**
     * Destroys entire Tree structure by setting LEFT/RIGHT/LEVEL values of all Nodes to 0.
     * May cause SQL Exception if there are unique indexes created on the corresponding columns.
     * If Repository nas a Tree Discriminator defined, only the Nodes belonging to that Tree wil be affected.
     */
    void destroyTree();

    /**
     * Inserts or Updates (depending if id is null or not) the node as a first Root Node in the Tree.
     * This is a good wntry point of initializing an empty Tree.
     * If the Target Node is already a first Root, returns without any actions.
     * If Repository nas a Tree Discriminator defined, Node will be only set as a first Root of that Tree.
     *
     * @param node - target Node
     */
    void insertAsFirstRoot(N node);

    /**
     * Inserts or Updates (depending if id is null or not) the node as a last Root Node in the Tree.
     * This is a good wntry point of initializing an empty Tree.
     * If the Target Node is already a last Root, returns without any actions.
     * If Repository nas a Tree Discriminator defined, Node will be only set as a last Root of that Tree.
     *
     * @param node - target Node
     */
    void insertAsLastRoot(N node);

    /**
     * Lock interface that serves as a Tree/Repository locking source.
     * Contains a default implementation of NoLock class used when no locking is required.
     *
     *
     * @param <ID> - Nested Node Identifier Class
     * @param <N> - Nested Node Class
     */
    interface Lock<ID extends Serializable, N extends NestedNode<ID>> {

        /**
         * Lock one or more Tree Nodes based on state of the target Node.
         * Can be used to Lock entire Repository or a Tree defined by a Discriminator value held by the target Node.
         * Should always fail if the entire Repository is already locked.
         *
         * @param node - target Node
         * @return - true if lock was successfull, false if unable to lock
         */
        boolean lockNode(N node);

        /**
         * Unlock one or more Tree Nodes based on state of the target Node.
         * Implementation should allow to unlock the same scope/amount of Nodes as the corresponding lock() method.
         * Node's internal state should allow to compute the exact same Handle that served as a locking point in the lock() method.
         *
         * @param node - target Node
         */
        void unlockNode(N node);

        /**
         * Lock entire Repository with regargless of any Tree Discriminators defined. Used when performing Tree rebuild.
         * Should always take precedense before locking Nodes.
         *
         * @return - true if lock was successfull, false if unable to lock
         */
        boolean lockRepository();

        /**
         * Unlocks entire Repository locked by tbe lockRepository method.
         */
        void unlockRepository();
    }


}
