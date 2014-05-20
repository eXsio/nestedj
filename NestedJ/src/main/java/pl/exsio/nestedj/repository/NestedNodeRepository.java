/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.exsio.nestedj.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.exsio.nestedj.NestedNodeInserter;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.NestedNodeUtil;
import pl.exsio.nestedj.NestedNodeHierarchyManipulator;
import pl.exsio.nestedj.NestedNodeMover;
import pl.exsio.nestedj.NestedNodeRebuilder;
import pl.exsio.nestedj.NestedNodeRemover;
import pl.exsio.nestedj.NestedNodeRetriever;
import pl.exsio.nestedj.dao.NestedNodeDao;
import pl.exsio.nestedj.ex.InvalidNodesHierarchyException;
import pl.exsio.nestedj.model.Tree;

/**
 *
 * @author exsio
 * @param <T>
 */
public class NestedNodeRepository<T extends NestedNode> implements NestedNodeDao<T> {

    /**
     *
     */
    @PersistenceContext
    private EntityManager em;

    /**
     *
     */
    private NestedNodeUtil<T> util;

    /**
     *
     */
    private NestedNodeInserter<T> inserter;

    /**
     *
     */
    private NestedNodeMover<T> mover;

    /**
     *
     */
    private NestedNodeRemover<T> remover;

    /**
     *
     */
    private NestedNodeRetriever<T> retriever;

    /**
     *
     */
    private NestedNodeRebuilder<T> rebuilder;

    /**
     *
     */
    public NestedNodeRepository() {
    }

    /**
     *
     * @param em
     */
    public NestedNodeRepository(EntityManager em) {
        this.em = em;
    }

    /**
     *
     * @param util
     */
    public void setNestedNodeUtil(NestedNodeUtil<T> util) {
        this.util = util;
    }

    /**
     *
     * @param inserter
     */
    public void setInserter(NestedNodeInserter<T> inserter) {
        this.inserter = inserter;
    }

    /**
     *
     * @param mover
     */
    public void setMover(NestedNodeMover<T> mover) {
        this.mover = mover;
    }

    /**
     *
     * @param remover
     */
    public void setRemover(NestedNodeRemover<T> remover) {
        this.remover = remover;
    }

    /**
     *
     * @param retriever
     */
    public void setRetriever(NestedNodeRetriever<T> retriever) {
        this.retriever = retriever;
    }

    /**
     *
     * @param rebuilder
     */
    public void setRebuilder(NestedNodeRebuilder<T> rebuilder) {
        this.rebuilder = rebuilder;
    }

    /**
     *
     * @param node
     * @param parent
     * @return
     */
    @Override
    public T insertAsFirstChildOf(T node, T parent) throws InvalidNodesHierarchyException {
        return this.insertOrMove(node, parent, NestedNodeHierarchyManipulator.MODE_FIRST_CHILD);
    }

    /**
     *
     * @param node
     * @param parent
     * @return
     */
    @Override
    public T insertAsLastChildOf(T node, T parent) throws InvalidNodesHierarchyException {
        return this.insertOrMove(node, parent, NestedNodeHierarchyManipulator.MODE_LAST_CHILD);
    }

    /**
     *
     * @param node
     * @param parent
     * @return
     */
    @Override
    public T insertAsNextSiblingOf(T node, T parent) throws InvalidNodesHierarchyException {
        return this.insertOrMove(node, parent, NestedNodeHierarchyManipulator.MODE_NEXT_SIBLING);
    }

    /**
     *
     * @param node
     * @param parent
     * @return
     */
    @Override
    public T insertAsPrevSiblingOf(T node, T parent) throws InvalidNodesHierarchyException {
        return this.insertOrMove(node, parent, NestedNodeHierarchyManipulator.MODE_PREV_SIBLING);
    }

    /**
     *
     * @param node
     * @param parent
     * @param mode
     * @return
     */
    private T insertOrMove(T node, T parent, int mode) throws InvalidNodesHierarchyException {
        if (this.util.isNodeNew(node)) {
            return this.inserter.insert(node, parent, mode);
        } else {
            return this.mover.move(node, parent, mode);
        }
    }

    /**
     *
     * @param node
     */
    @Override
    public void removeSingle(T node) {
        this.remover.removeSingle(node);
    }

    /**
     *
     * @param node
     */
    @Override
    public void removeSubtree(T node) {
        this.remover.removeSubtree(node);
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public Iterable<T> getTreeAsList(T node) {
        return this.retriever.getTreeAsList(node);
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public Iterable<T> getChildren(T node) {
        return this.retriever.getChildren(node);
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public T getParent(T node) {
        return this.retriever.getParent(node);
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public Tree<T> getTree(T node) {
        return this.retriever.getTree(node);
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public Iterable<T> getParents(T node) {
        return this.retriever.getParents(node);
    }

    /**
     * 
     * @param node 
     */
    public void rebuildTree(T node) {
        this.rebuilder.rebuildTree(node);
    }
}
