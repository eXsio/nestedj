package pl.exsio.nestedj.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import pl.exsio.nestedj.base.TestHelper;
import pl.exsio.nestedj.model.TestNode;

public class JpaTestHelper implements TestHelper {

    private final EntityManager em;

    JpaTestHelper(EntityManager em) {
        this.em = em;
    }

    @Override
    public TestNode findNode(String symbol) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TestNode> select = cb.createQuery(TestNode.class);
        Root<TestNode> root = select.from(TestNode.class);
        select.where(cb.equal(root.get("name"), symbol));
        TestNode n = em.createQuery(select).getSingleResult();
        TestHelper.printNode(symbol, n);
        this.em.refresh(n);
        return n;
    }

    @Override
    public TestNode getParent(TestNode f) {
        this.em.refresh(f);
        TestNode parent = null;
        Long parentId = f.getParentId();
        if (parentId != null) {
            parent = em.find(TestNode.class, parentId);
        }
        System.out.println(String.format("Parent of %s is %s", f.getName(), parent != null ? parent.getName() : "null"));
        return parent;
    }

    @Override
    public void breakTree() {
        this.em.createQuery("update TestNode set treeLeft = 0, treeRight = 0, treeLevel = 0 where discriminator = 'tree_1'").executeUpdate();
    }

    @Override
    public void resetParent(String symbol) {
        this.em.createQuery("update TestNode set parentId = null where name='"+symbol+"' and discriminator = 'tree_1'").executeUpdate();
    }

    @Override
    public void removeTree() {
        this.em.createQuery("delete from TestNode where discriminator = 'tree_1'").executeUpdate();
    }

    public void flushAndClear() {
        em.flush();
        em.clear();
    }

    public void flush() {
        em.flush();
    }

    public void refresh(TestNode node) {
        em.refresh(node);
    }

    public void save(TestNode node) {
        em.persist(node);
    }
}
