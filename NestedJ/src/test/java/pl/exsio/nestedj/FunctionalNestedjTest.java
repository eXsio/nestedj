package pl.exsio.nestedj;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.exsio.nestedj.model.TestNodeImpl;
import pl.exsio.nestedj.repository.NestedNodeRepository;

/**
 *
 * @author exsio
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/testContext.xml"})
public abstract class FunctionalNestedjTest {

    @Autowired
    protected NestedNodeRepository<TestNodeImpl> nodeRepository;

    @PersistenceContext
    protected EntityManager em;
    
    
    
    /**
     * 
     *          STARTING NESTED TREE CONDITIONS
     * 
     *                      1 A 16
     *                       / \                    IDS:
     *                      /   \                   A: 1
     *                     /     \                  B: 2
     *                  2 B 7   8 C 15              C: 3
     *                   /         \                D: 4
     *                  /\         /\               E: 5
     *                 /  \       /  \              F: 6
     *                /    \     /    \             G: 7
     *               /   5 E 6  9 F 10 \            H: 8
     *             3 D 4             11 G 14
     *                                   \
     *                                    \
     *                                  12 H 13 
     */

    protected TestNodeImpl findNode(String symbol) {

        Map<String, Long> nodeMap = new HashMap() {
            {
                put("a", new Long(1));
                put("b", new Long(2));
                put("c", new Long(3));
                put("d", new Long(4));
                put("e", new Long(5));
                put("f", new Long(6));
                put("g", new Long(7));
                put("h", new Long(8));
            }
        };

        TestNodeImpl n = this.em.find(TestNodeImpl.class, nodeMap.get(symbol));
        this.em.refresh(n);
        return n;
    }

}
