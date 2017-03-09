package pl.exsio.nestedj;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import pl.exsio.nestedj.delegate.NestedNodeInserter;
import pl.exsio.nestedj.delegate.NestedNodeInserterImpl;
import pl.exsio.nestedj.delegate.NestedNodeMover;
import pl.exsio.nestedj.delegate.NestedNodeMoverImpl;
import pl.exsio.nestedj.delegate.NestedNodeRebuilder;
import pl.exsio.nestedj.delegate.NestedNodeRebuilderImpl;
import pl.exsio.nestedj.delegate.NestedNodeRemover;
import pl.exsio.nestedj.delegate.NestedNodeRemoverImpl;
import pl.exsio.nestedj.delegate.NestedNodeRetriever;
import pl.exsio.nestedj.delegate.NestedNodeRetrieverImpl;
import pl.exsio.nestedj.discriminator.TestTreeDiscriminator;
import pl.exsio.nestedj.model.TestNodeImpl;
import pl.exsio.nestedj.repository.NestedNodeRepository;
import pl.exsio.nestedj.repository.NestedNodeRepositoryImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.Properties;
import java.util.UUID;

@Configuration
public class TestConfiguration {

    @PersistenceContext
    EntityManager entityManager;

    @Bean
    public DataSource dataSource() {
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName(UUID.randomUUID().toString());
        return builder.build();
    }

    @Bean
    public Properties additionalProperties() {
        Properties properties = new Properties();

        properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.transaction.flush_before_completion", "false");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.setProperty("hibernate.generate_statistics", "false");
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.hbm2ddl.import_files", "/fixtures/test-import.sql");
        return properties;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, Properties additionalProperties) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("pl.exsio");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaProperties(additionalProperties);
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }


    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    public NestedNodeRepository<TestNodeImpl> repository() {
        NestedNodeRepositoryImpl<TestNodeImpl> repository = new NestedNodeRepositoryImpl<>();
        TestTreeDiscriminator treeDiscriminator = new TestTreeDiscriminator();
        NestedNodeInserter<TestNodeImpl> inserter = new NestedNodeInserterImpl<>(entityManager, treeDiscriminator);
        NestedNodeMover<TestNodeImpl> mover = new NestedNodeMoverImpl<>(entityManager, treeDiscriminator);
        NestedNodeRetriever<TestNodeImpl> retriever = new NestedNodeRetrieverImpl<>(entityManager, treeDiscriminator);
        NestedNodeRebuilder<TestNodeImpl> rebuilder = new NestedNodeRebuilderImpl<>(entityManager, treeDiscriminator, inserter, retriever);
        NestedNodeRemover<TestNodeImpl> remover = new NestedNodeRemoverImpl<>(entityManager, treeDiscriminator);

        repository.setInserter(inserter);
        repository.setMover(mover);
        repository.setRebuilder(rebuilder);
        repository.setRetriever(retriever);
        repository.setRemover(remover);

        return repository;
    }

}
