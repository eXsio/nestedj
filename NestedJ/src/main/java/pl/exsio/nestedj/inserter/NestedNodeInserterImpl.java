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
public class NestedNodeInserterImpl<T extends NestedNode> implements NestedNodeInserter<T> {

    @PersistenceContext
    protected EntityManager em;

    protected NestedNodeUtil<T> util;

    public NestedNodeInserterImpl() {
    }

    public NestedNodeInserterImpl(EntityManager em) {
        this.em = em;
    }

    public void setNestedNodeUtil(NestedNodeUtil<T> util) {
        this.util = util;
    }

    @Override
    @Transactional
    public T insert(T node, T parent, int mode) {
        this.em.refresh(parent);
        NestedNodeConfig config = this.util.getNodeConfig(node.getClass());
        Long left = this.getNodeLeft(parent, mode);
        Long right = left + 1;
        Long level = parent.getLevel() + 1;
        this.makeSpaceForNewElement(node.getClass(), parent.getRight(), this.isGte(mode), config);
        this.em.persist(node);
        this.em.flush();
        this.performInsertion(config, parent, left, right, level, node);
        this.em.refresh(node);
        return node;
    }

    private void performInsertion(NestedNodeConfig config, T parent, Long left, Long right, Long level, T node) {
        this.em.createQuery(
                "update " + config.getEntityName()+ " "
                        + "set " + config.getParentFieldName() + " = :parent,"
                        + config.getLeftFieldName() + " = :left,"
                        + config.getRightFieldName() + " = :right," 
                        + config.getLevelFieldName() + " = :level "
                        + "where id = :id").setParameter("parent", parent)
                .setParameter("left", left)
                .setParameter("right", right)
                .setParameter("level", level)
                .setParameter("id", node.getId())
                .executeUpdate();
    }

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

    protected void makeSpaceForNewElement(Class<? extends NestedNode> nodeClass, Long from, boolean gte, NestedNodeConfig config) {

        String sign = gte ? " >= " : " > ";
        this.updateLeftFields(config, sign, from);
        this.updateRightFields(config, sign, from);
    }

    private void updateRightFields(NestedNodeConfig config, String sign, Long from) { 
        String rightQuery = "update " + config.getEntityName()+ " "
                + "set " + config.getRightFieldName() + " = " + config.getRightFieldName() + "+2 "
                + "where " + config.getRightFieldName() + " " + sign + " :from";
        this.em.createQuery(rightQuery)
                .setParameter("from", from)
                .executeUpdate();
    }

    private void updateLeftFields(NestedNodeConfig config, String sign, Long from) {
        String leftQuery = "update " + config.getEntityName()+ " "
                + "set " + config.getLeftFieldName() + " = " + config.getLeftFieldName() + "+2 "
                + "where " + config.getLeftFieldName() + " " + sign + " :from";
        this.em.createQuery(leftQuery)
                .setParameter("from", from)
                .executeUpdate();
    }

}
