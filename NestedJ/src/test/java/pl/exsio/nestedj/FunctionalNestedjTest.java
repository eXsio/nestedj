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

    private final Map<String, Long> nodeMap = new HashMap() {
        {
            put("a", 1l);
            put("b", 2l);
            put("c", 3l);
            put("d", 4l);
            put("e", 5l);
            put("f", 6l);
            put("g", 7l);
            put("h", 8l);
        }
    };

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

        TestNodeImpl n = this.em.find(TestNodeImpl.class, this.nodeMap.get(symbol));
        this.em.refresh(n);
        return n;
    }

}
