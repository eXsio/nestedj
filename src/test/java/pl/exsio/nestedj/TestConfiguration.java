package pl.exsio.nestedj;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import pl.exsio.nestedj.config.jdbc.JdbcNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.config.jpa.JpaNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.jdbc.discriminator.TestJdbcTreeDiscriminator;
import pl.exsio.nestedj.jdbc.repository.factory.JdbcNestedNodeRepositoryFactory;
import pl.exsio.nestedj.jpa.discriminator.TestJpaTreeDiscriminator;
import pl.exsio.nestedj.jpa.repository.factory.JpaNestedNodeRepositoryFactory;
import pl.exsio.nestedj.model.TestNode;
import pl.exsio.nestedj.qualifier.Jdbc;
import pl.exsio.nestedj.qualifier.Jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;

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
    @Jpa
    public NestedNodeRepository<Long, TestNode> jpaRepository() {
        JpaNestedNodeRepositoryConfiguration<Long, TestNode> configuration = new JpaNestedNodeRepositoryConfiguration<>(
                entityManager, TestNode.class, Long.class, new TestJpaTreeDiscriminator()
        );
        return JpaNestedNodeRepositoryFactory.create(configuration);
    }

    @Bean
    @Jdbc
    public NestedNodeRepository<Long, TestNode> jdbcRepository(DataSource dataSource) {
        //ROW MAPPER FOR CREATING INSTANCES OF THE NODE OBJECT
        RowMapper<TestNode> mapper = (resultSet, i) -> {
            return TestNode.fromResultSet(resultSet);
        };

        //TABLE NAME
        String tableName = "nested_nodes";

        // QUERY USED FOR INSERTING NEW NODES
        String insertQuery = "insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(next value for SEQ,?,?,?,?,?,?)";

        // INSERT QUERY VALUES PROVIDER, CONVERTS NODE OBJECT INTO AN OBJECT ARRAY
        Function<TestNode, Object[]> insertValuesProvider = n -> new Object[]{n.getTreeLeft(), n.getTreeLevel(), n.getTreeRight(), n.getName(), n.getParentId(), n.getDiscriminator()};

        JdbcNestedNodeRepositoryConfiguration<Long, TestNode> configuration = new JdbcNestedNodeRepositoryConfiguration<>(
                new JdbcTemplate(dataSource), tableName, mapper, insertQuery, insertValuesProvider, new TestJdbcTreeDiscriminator()
        );

        configuration.setIdColumnName("id");
        configuration.setParentIdColumnName("parent_id");
        configuration.setLeftColumnName("tree_left");
        configuration.setRighColumnName("tree_right");
        configuration.setLevelColumnName("tree_level");

        return JdbcNestedNodeRepositoryFactory.create(configuration);
    }

}
