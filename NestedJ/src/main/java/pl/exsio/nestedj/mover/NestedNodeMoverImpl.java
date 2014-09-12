package pl.exsio.nestedj.mover;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.NestedNodeUtil;
import pl.exsio.nestedj.NestedNodeMover;
import pl.exsio.nestedj.config.NestedNodeConfig;
import pl.exsio.nestedj.ex.InvalidNodesHierarchyException;

/**
 *
 * @author exsio
 */
public class NestedNodeMoverImpl implements NestedNodeMover {

    private final static String SIGN_PLUS = "+";
    private final static String SIGN_MINUS = "-";

    @PersistenceContext
    protected EntityManager em;

    protected NestedNodeUtil util;

    public NestedNodeMoverImpl() {
    }

    public NestedNodeMoverImpl(EntityManager em) {
        this.em = em;
    }

    public void setNestedNodeUtil(NestedNodeUtil util) {
        this.util = util;
    }

    @Override
    @Transactional
    public NestedNode move(NestedNode node, NestedNode parent, int mode) throws InvalidNodesHierarchyException {
        
        this.em.refresh(node);
        this.em.refresh(parent);
        if (!this.canMoveNodeToSelectedParent(node, parent)) {
            throw new InvalidNodesHierarchyException("You cannot move a parent node to it's child or move a node to itself");
        }
        NestedNodeConfig config = this.util.getNodeConfig(node.getClass());
        String sign = this.getSign(node, parent, mode);
        Long start = this.getStart(node, parent, mode, sign);
        Long stop = this.getStop(node, parent, mode, sign);
        List nodeIds = this.getNodeIds(node, config);
        Long delta = this.getDelta(nodeIds);
        Long nodeDelta = this.getNodeDelta(start, stop);
        String nodeSign = this.getNodeSign(sign);
        Long levelModificator = this.getLevelModificator(node, parent, mode);
        NestedNode newParent = this.getNewParent(parent, mode);

        this.makeSpaceForMovedElement(config, sign, delta, start, stop);
        this.performMove(config, nodeSign, nodeDelta, nodeIds, levelModificator);
        this.updateParentField(config, newParent, node);

        this.em.refresh(parent);
        this.em.refresh(node);

        return node;
    }

    private void makeSpaceForMovedElement(NestedNodeConfig config, String sign, Long delta, Long start, Long stop) {
        this.updateLeftFields(config, sign, delta, start, stop);
        this.updateRightFields(config, sign, delta, start, stop);
    }

    private void updateParentField(NestedNodeConfig config, NestedNode newParent, NestedNode node) {
        this.em.createQuery("update " + config.getEntityName()+ " "
                + "set " + config.getParentFieldName() + " = :parent "
                + "where id = :id").setParameter("parent", newParent)
                .setParameter("id", node.getId())
                .executeUpdate();
    }

    private void performMove(NestedNodeConfig config, String nodeSign, Long nodeDelta, List nodeIds, Long levelModificator) {
        if(!nodeIds.isEmpty()) {
            this.em.createQuery("update " + config.getEntityName()+ " "
                    + "set " + config.getLevelFieldName() + " = " + config.getLevelFieldName() + " + :levelModificator, "
                    + config.getRightFieldName() + " = " + config.getRightFieldName() + " " + nodeSign + ":nodeDelta, "
                    + config.getLeftFieldName() + " = " + config.getLeftFieldName() + " " + nodeSign + ":nodeDelta "
                    + "where id in :ids")
                    .setParameter("nodeDelta", nodeDelta)
                    .setParameter("ids", nodeIds)
                    .setParameter("levelModificator", levelModificator)
                    .executeUpdate();
        }
    }

    private void updateRightFields(NestedNodeConfig config, String sign, Long delta, Long start, Long stop) {
        this.em.createQuery("update " + config.getEntityName()+ " "
                + "set " + config.getRightFieldName() + " = " + config.getRightFieldName() + " " + sign + ":delta "
                + "where " + config.getRightFieldName() + " > :start "
                + "and " + config.getRightFieldName() + " < :stop")
                .setParameter("delta", delta)
                .setParameter("start", start)
                .setParameter("stop", stop)
                .executeUpdate();
    }

    private void updateLeftFields(NestedNodeConfig config, String sign, Long delta, Long start, Long stop) {
        this.em.createQuery("update " + config.getEntityName()+ " "
                + "set " + config.getLeftFieldName() + " = " + config.getLeftFieldName() + " " + sign + ":delta where "
                + config.getLeftFieldName() + " > :start "
                + "and " + config.getLeftFieldName() + " < :stop")
                .setParameter("delta", delta)
                .setParameter("start", start)
                .setParameter("stop", stop)
                .executeUpdate();
    }

    private boolean canMoveNodeToSelectedParent(NestedNode node, NestedNode parent) {
        return !node.getId().equals(parent.getId()) && (node.getLeft() >= parent.getLeft() || node.getRight() <= parent.getRight());
    }

    private NestedNode getNewParent(NestedNode parent, int mode) {
        switch (mode) {
            case MODE_NEXT_SIBLING:
            case MODE_PREV_SIBLING:
                return parent.getParent();
            case MODE_FIRST_CHILD:
            case MODE_LAST_CHILD:
            default:
                return parent;
        }
    }

    private Long getLevelModificator(NestedNode node, NestedNode parent, int mode) {
        switch (mode) {
            case MODE_NEXT_SIBLING:
            case MODE_PREV_SIBLING:
                return parent.getLevel() - node.getLevel();
            case MODE_FIRST_CHILD:
            case MODE_LAST_CHILD:
            default:
                return parent.getLevel() + 1 - node.getLevel();
        }
    }

    private List<Long> getNodeIds(NestedNode node, NestedNodeConfig config) {
        List result = this.em.createQuery("select id from " + config.getEntityName() + " "
                + "where " + config.getLeftFieldName() + ">=:lft "
                + "and " + config.getRightFieldName() + " <=:rgt ")
                .setParameter("lft", node.getLeft())
                .setParameter("rgt", node.getRight())
                .getResultList();
        return result;
    }

    private Long getNodeDelta(Long start, Long stop) {
        return stop - start - 1;
    }

    private Long getDelta(List<Long> nodeIds) {
        return new Long(nodeIds.size() * 2);
    }

    private String getNodeSign(String sign) {
        return (sign.equals(SIGN_PLUS)) ? SIGN_MINUS : SIGN_PLUS;
    }

    private String getSign(NestedNode node, NestedNode parent, int mode) {
        switch (mode) {
            case MODE_PREV_SIBLING:
            case MODE_FIRST_CHILD:
                return (node.getRight() - parent.getLeft()) > 0 ? SIGN_PLUS : SIGN_MINUS;
            case MODE_NEXT_SIBLING:
            case MODE_LAST_CHILD:
            default:
                return (node.getLeft() - parent.getRight()) > 0 ? SIGN_PLUS : SIGN_MINUS;
        }
    }

    private Long getStart(NestedNode node, NestedNode parent, int mode, String sign) {
        switch (mode) {
            case MODE_PREV_SIBLING:
                return sign.equals(SIGN_PLUS) ? parent.getLeft() - 1 : node.getRight();
            case MODE_FIRST_CHILD:
                return sign.equals(SIGN_PLUS) ? parent.getLeft() : node.getRight();
            case MODE_NEXT_SIBLING:
                return sign.equals(SIGN_PLUS) ? parent.getRight() : node.getRight();
            case MODE_LAST_CHILD:
            default:
                return sign.equals(SIGN_PLUS) ? parent.getRight() - 1 : node.getRight();

        }
    }

    private Long getStop(NestedNode node, NestedNode parent, int mode, String sign) {
        switch (mode) {
            case MODE_PREV_SIBLING:
                return sign.equals(SIGN_PLUS) ? node.getLeft() : parent.getLeft();
            case MODE_FIRST_CHILD:
                return sign.equals(SIGN_PLUS) ? node.getLeft() : parent.getLeft() + 1;
            case MODE_NEXT_SIBLING:
                return sign.equals(SIGN_PLUS) ? node.getLeft() : parent.getRight() + 1;
            case MODE_LAST_CHILD:
            default:
                return sign.equals(SIGN_PLUS) ? node.getLeft() : parent.getRight();
        }
    }

}
