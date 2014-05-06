/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.exsio.nestedj;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.exsio.nestedj.inserter.JpaNestedNodeInserter;
import pl.exsio.nestedj.model.TestNode;
import pl.exsio.nestedj.mover.JpaNestedNodeMover;
import pl.exsio.nestedj.remover.JpaNestedNodeRemover;
import pl.exsio.nestedj.repository.NestedNodeRepository;
import pl.exsio.nestedj.retriever.JpaNestedNodeRetriever;
import pl.exsio.nestedj.util.DefaultNestedNodeUtil;

/**
 *
 * @author exsio
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/testContext.xml"})
public class FunctionalNestedjTest {
    
//    protected static EntityManagerFactory emFactory;
//    
//    protected NestedNodeRepository<TestNode> nodeRepository;
//
//    protected EntityManager em;
//   
//    @BeforeClass
//    public static void initClass() {
//        emFactory = Persistence.createEntityManagerFactory("TestPU");
//    }
//   
//    @Before
//    public void init() {
//        
//        em = emFactory.createEntityManager();       
//        NestedNodeUtil util = new DefaultNestedNodeUtil();
//        JpaNestedNodeInserter inserter = new JpaNestedNodeInserter(em);
//        inserter.setNestedNodeUtil(util);
//        JpaNestedNodeMover mover = new JpaNestedNodeMover(em);
//        mover.setNestedNodeUtil(util);
//        JpaNestedNodeRemover remover = new JpaNestedNodeRemover(em);
//        remover.setNestedNodeUtil(util);
//        JpaNestedNodeRetriever retriever = new JpaNestedNodeRetriever(em);
//        retriever.setNestedNodeUtil(util);
//        this.nodeRepository = new NestedNodeRepository(em);
//        this.nodeRepository.setInserter(inserter);
//        this.nodeRepository.setMover(mover);
//        this.nodeRepository.setNestedNodeUtil(util);
//        this.nodeRepository.setRemover(remover);
//        this.nodeRepository.setRetriever(retriever);
//    }
//    
//    @AfterClass
//    public static void cleanupClass() {
//        emFactory.close();
//    }

}
