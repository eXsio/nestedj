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
package pl.exsio.nestedj.rebuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import pl.exsio.nestedj.NestedNodeInserter;
import pl.exsio.nestedj.NestedNodeMover;
import pl.exsio.nestedj.NestedNodeRebuilder;
import pl.exsio.nestedj.config.NestedNodeConfig;
import pl.exsio.nestedj.ex.InvalidNodesHierarchyException;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.util.NestedNodeUtil;

/**
 *
 * @author exsio
 * @param <T>
 */
public class NestedNodeRebuilderImpl<T extends NestedNode> implements NestedNodeRebuilder {

    @PersistenceContext
    protected EntityManager em;

    protected NestedNodeInserter inserter;

    public NestedNodeRebuilderImpl() {
    }

    public NestedNodeRebuilderImpl(NestedNodeInserter inserter) {
        this.inserter = inserter;
    }

    public NestedNodeRebuilderImpl(EntityManager em, NestedNodeInserter inserter) {
        this.em = em;
        this.inserter = inserter;
    }

    public NestedNodeRebuilderImpl(EntityManager em) {
        this.em = em;
    }

    public void setInserter(NestedNodeInserter inserter) {
        this.inserter = inserter;
    }

    @Override
    @Transactional
    public void rebuildTree(Class nodeClass) throws InvalidNodesHierarchyException {
        NestedNodeConfig config = NestedNodeUtil.getNodeConfig(nodeClass);

        NestedNode first = this.findFirstNestedNode(config);

        this.resetFirst(first, config);
        this.restoreSiblings(first, config);
        this.rebuildRecursively(first, config);
        for (NestedNode node : this.getSiblings(first, config)) {
            this.rebuildRecursively(node, config);
        }
        this.em.refresh(first);
    }

    private void rebuildRecursively(NestedNode parent, NestedNodeConfig config) throws InvalidNodesHierarchyException {
        for (NestedNode child : this.getChildren(parent, config)) {
            this.inserter.insert(child, parent, NestedNodeMover.MODE_LAST_CHILD);
            this.rebuildRecursively(child, config);
        }
    }

    private NestedNode findFirstNestedNode(NestedNodeConfig config) {

        NestedNode first = (NestedNode) this.em.createQuery("from " + config.getEntityName() + " "
                + "where " + config.getParentFieldName() + " is null")
                .setMaxResults(1)
                .getSingleResult();
        return first;
    }

    private void resetFirst(NestedNode first, NestedNodeConfig config) {
        this.em.createQuery("update  " + config.getEntityName() + " "
                + "set " + config.getLeftFieldName() + " = 1, "
                + "" + config.getRightFieldName() + " = 2 where " + config.getIdFieldName() + " = :id")
                .setParameter("id", first.getId())
                .executeUpdate();
    }

    private Iterable<NestedNode> getSiblings(NestedNode first, NestedNodeConfig config) {
        return this.em.createQuery("from " + config.getEntityName() + " "
                + "where " + config.getParentFieldName() + " is null "
                + "and " + config.getIdFieldName() + " != :id "
                + "order by " + config.getIdFieldName() + " desc")
                .setParameter("id", first.getId())
                .getResultList();
    }

    private void restoreSiblings(NestedNode first, NestedNodeConfig config) throws InvalidNodesHierarchyException {
        for (NestedNode node : this.getSiblings(first, config)) {
            this.inserter.insert(node, first, NestedNodeMover.MODE_NEXT_SIBLING);
        }
    }

    private Iterable<NestedNode> getChildren(NestedNode parent, NestedNodeConfig config) {
        return this.em.createQuery("from " + config.getEntityName() + " "
                + "where " + config.getParentFieldName() + " = :parent "
                + "order by " + config.getIdFieldName() + " desc")
                .setParameter("parent", parent)
                .getResultList();
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

}
