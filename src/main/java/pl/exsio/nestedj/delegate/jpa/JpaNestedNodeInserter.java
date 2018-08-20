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
package pl.exsio.nestedj.delegate.jpa;

import pl.exsio.nestedj.delegate.NestedNodeInserter;
import pl.exsio.nestedj.discriminator.TreeDiscriminator;
import pl.exsio.nestedj.model.NestedNode;
import pl.exsio.nestedj.model.NestedNodeInfo;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.Optional;

import static pl.exsio.nestedj.model.NestedNode.LEFT;
import static pl.exsio.nestedj.model.NestedNode.RIGHT;


public class JpaNestedNodeInserter<ID extends Serializable, N extends NestedNode<ID>> extends JpaNestedNodeDelegate<ID, N> implements NestedNodeInserter<ID, N> {

    public JpaNestedNodeInserter(EntityManager entityManager, TreeDiscriminator<ID, N> treeDiscriminator) {
        super(entityManager, treeDiscriminator);
    }

    @Override
    public void insert(N node, NestedNodeInfo<ID, N> parentInfo, Mode mode, Class<N> nodeClass) {
        makeSpaceForNewElement(getMoveFrom(parentInfo, mode), mode, nodeClass);
        insertNodeIntoTree(parentInfo, node, mode);
    }

    private void insertNodeIntoTree(NestedNodeInfo<ID, N> parent, N node, Mode mode) {
        Long left = this.getNodeLeft(parent, mode);
        Long right = left + 1;
        Long level = this.getNodeLevel(parent, mode);
        node.setTreeLeft(left);
        node.setTreeRight(right);
        node.setTreeLevel(level);
        node.setParentId(this.getNodeParent(parent, mode).orElse(null));
        entityManager.persist(node);
    }

    private void makeSpaceForNewElement(Long from, Mode mode, Class<N> nodeClass) {
        this.updateFields(from, mode, nodeClass, RIGHT);
        this.updateFields(from, mode, nodeClass, LEFT);
    }

    private void updateFields(Long from, Mode mode, Class<N> nodeClass, String fieldName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<N> update = cb.createCriteriaUpdate(nodeClass);
        Root<N> root = update.from(nodeClass);

        update.set(root.<Long>get(fieldName), cb.sum(root.get(fieldName), 2L));
        if (applyGte(mode)) {
            update.where(getPredicates(cb, root, cb.greaterThanOrEqualTo(root.get(fieldName), from)));
        } else {
            update.where(getPredicates(cb, root, cb.greaterThan(root.get(fieldName), from)));
        }
        entityManager.createQuery(update).executeUpdate();
    }

    private Long getMoveFrom(NestedNodeInfo<ID, N> parent, Mode mode) {
        switch (mode) {
            case PREV_SIBLING:
            case FIRST_CHILD:
                return parent.getLeft();
            case NEXT_SIBLING:
            case LAST_CHILD:
            default:
                return parent.getRight();
        }
    }

    private Long getNodeLevel(NestedNodeInfo<ID, N> parent, Mode mode) {
        switch (mode) {
            case NEXT_SIBLING:
            case PREV_SIBLING:
                return parent.getLevel();
            case LAST_CHILD:
            case FIRST_CHILD:
            default:
                return parent.getLevel() + 1;
        }
    }

    private Optional<ID> getNodeParent(NestedNodeInfo<ID, N> parent, Mode mode) {
        switch (mode) {
            case NEXT_SIBLING:
            case PREV_SIBLING:
                if (parent.getParentId() != null) {
                    return Optional.of(parent.getParentId());
                } else {
                    return Optional.empty();
                }
            case LAST_CHILD:
            case FIRST_CHILD:
            default:
                return Optional.of(parent.getId());
        }
    }

    private Long getNodeLeft(NestedNodeInfo<ID, N> parent, Mode mode) {
        switch (mode) {
            case NEXT_SIBLING:
                return parent.getRight() + 1;
            case PREV_SIBLING:
                return parent.getLeft();
            case FIRST_CHILD:
                return parent.getLeft() + 1;
            case LAST_CHILD:
            default:
                return parent.getRight();
        }
    }

    private boolean applyGte(Mode mode) {
        switch (mode) {
            case NEXT_SIBLING:
            case FIRST_CHILD:
                return false;
            case PREV_SIBLING:
            case LAST_CHILD:
            default:
                return true;
        }
    }

}
