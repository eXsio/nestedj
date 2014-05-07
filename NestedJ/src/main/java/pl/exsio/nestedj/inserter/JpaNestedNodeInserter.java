/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.exsio.nestedj.inserter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.NestedNodeInserter;
import pl.exsio.nestedj.NestedNodeUtil;
import pl.exsio.nestedj.config.NestedNodeConfig;
import javax.transaction.Transactional;
/**
 *
 * @author exsio
 * @param <T>
 */
public class JpaNestedNodeInserter<T extends NestedNode> implements NestedNodeInserter<T> {

    /**
     *
     */
    @PersistenceContext
    protected EntityManager em;

    /**
     *
     */
    protected NestedNodeUtil<T> util;

    /**
     *
     */
    public JpaNestedNodeInserter() {
    }

    /**
     *
     * @param em
     */
    public JpaNestedNodeInserter(EntityManager em) {
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
     * @param node
     * @param parent
     * @param mode
     * @return
     */
    @Override
    @Transactional
    public T insert(T node, T parent, int mode) {

        NestedNodeConfig config = this.util.getNodeConfig(node.getClass());
        Long left = this.getNodeLeft(parent, mode);
        Long right = left + 1;
        Long level = parent.getLevel() + 1;
        this.moveTree(node.getClass(), parent.getRight(), this.isGte(mode), config);
        this.em.persist(node);
        this.em.flush();
        this.em.createQuery("update " + config.getEntityName() + " set " + config.getParentFieldName() + " = :parent," + config.getLeftFieldName() + " = :left," + config.getRightFieldName() + " = :right," + config.getLevelFieldName() + " = :level  where id = :id").setParameter("parent", parent).setParameter("left", left).setParameter("right", right).setParameter("level", level).setParameter("id", node.getId()).executeUpdate();
        this.em.refresh(node);
        return node;
    }

    /**
     *
     * @param mode
     * @return
     */
    protected boolean isGte(int mode) {
        switch (mode) {
            case MODE_NEXT_SIBLING:
            case MODE_FIRST_CHILD:
                return false;
            case MODE_PREV_SIBLING:
            case MODE_LAST_CHILD:
            default:
                return true;
        }
    }

    /**
     *
     * @param parent
     * @param mode
     * @return
     */
    protected Long getNodeLeft(NestedNode parent, int mode) {
        switch (mode) {
            case MODE_NEXT_SIBLING:
                return parent.getRight() + 1;
            case MODE_PREV_SIBLING:
                return parent.getLeft();
            case MODE_FIRST_CHILD:
                return parent.getLeft() + 1;
            case MODE_LAST_CHILD:
            default:
                return parent.getRight();
        }
    }

    /**
     *
     * @param nodeClass
     * @param from
     * @param gte
     * @param config
     */
    protected void moveTree(Class<? extends NestedNode> nodeClass, Long from, boolean gte, NestedNodeConfig config) {

        String sign = gte ? " >= " : " > ";
        String leftQuery = "update " + config.getEntityName() + " set " + config.getLeftFieldName() + " = " + config.getLeftFieldName() + "+2 where " + config.getLeftFieldName() + " " + sign + " :from";
        String rightQuery = "update " + config.getEntityName() + " set " + config.getRightFieldName() + " = " + config.getRightFieldName() + "+2 where " + config.getRightFieldName() + " " + sign + " :from";
        this.em.createQuery(leftQuery).setParameter("from", from).executeUpdate();
        this.em.createQuery(rightQuery).setParameter("from", from).executeUpdate();
    }

}
