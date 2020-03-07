package pl.exsio.nestedj;

import com.google.common.collect.Lists;
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
import pl.exsio.nestedj.config.jdbc.discriminator.TestJdbcTreeDiscriminator;
import pl.exsio.nestedj.config.jdbc.factory.JdbcNestedNodeRepositoryFactory;
import pl.exsio.nestedj.config.jpa.JpaNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.config.jpa.discriminator.TestJpaTreeDiscriminator;
import pl.exsio.nestedj.config.jpa.factory.JpaNestedNodeRepositoryFactory;
import pl.exsio.nestedj.config.mem.InMemoryNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.config.mem.discriminator.TestInMemoryTreeDiscriminator;
import pl.exsio.nestedj.config.mem.factory.InMemoryNestedNodeRepositoryFactory;
import pl.exsio.nestedj.config.mem.lock.InMemoryLock;
import pl.exsio.nestedj.delegate.query.jdbc.JdbcKeyHolder;
import pl.exsio.nestedj.model.TestNode;
import pl.exsio.nestedj.qualifier.Jdbc;
import pl.exsio.nestedj.qualifier.Jpa;
import pl.exsio.nestedj.qualifier.Mem;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;

@Configuration
public class TestConfiguration {

    @PersistenceContext
    EntityManager entityManager;
    
    private static final AtomicLong ID = new AtomicLong();

    public static final List<TestNode> IN_MEM_NODES = Lists.newArrayList(
            new TestNode(1000L,1L,0L,16L,"a",null, "tree_1"),
            new TestNode(2000L,2L,1L,7L,"b",1000L, "tree_1"),
            new TestNode(3000L,8L,1L,15L,"c",1000L, "tree_1"),
            new TestNode(4000L,3L,2L,4L,"d",2000L, "tree_1"),
            new TestNode(5000L,5L,2L,6L,"e",2000L, "tree_1"),
            new TestNode(6000L,9L,2L,10L,"f",3000L, "tree_1"),
            new TestNode(7000L,11L,2L,14L,"g",3000L, "tree_1"),
            new TestNode(8000L,12L,3L,13L,"h",7000L, "tree_1"),
            new TestNode(9000L,1L,0L,16L,"a2",null, "tree_2"),
            new TestNode(10000L,2L,1L,7L,"b2",9000L, "tree_2"),
            new TestNode(11000L,8L,1L,15L,"c2",9000L, "tree_2"),
            new TestNode(12000L,3L,2L,4L,"d2",10000L, "tree_2"),
            new TestNode(13000L,5L,2L,6L,"e2",10000L, "tree_2"),
            new TestNode(14000L,9L,2L,10L,"f2",11000L, "tree_2"),
            new TestNode(15000L,11L,2L,14L,"g2",11000L, "tree_2"),
            new TestNode(16000L,12L,3L,13L,"h2",15000L, "tree_2")
    );

    public static final InMemoryNestedNodeRepositoryConfiguration<Long, TestNode> IN_MEM_CONFIG = inMemoryConfiguration();

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
        RowMapper<TestNode> mapper = (resultSet, i) -> TestNode.fromResultSet(resultSet);

        //TABLE NAME
        String tableName = "nested_nodes";

        // QUERY USED FOR INSERTING NEW NODES
        String insertQuery = "insert into nested_nodes(id, tree_left, tree_level, tree_right, node_name, parent_id, discriminator) values(next value for SEQ,?,?,?,?,?,?)";

        // INSERT QUERY VALUES PROVIDER, CONVERTS NODE OBJECT INTO AN OBJECT ARRAY
        Function<TestNode, Object[]> insertValuesProvider = n -> new Object[]{n.getTreeLeft(), n.getTreeLevel(), n.getTreeRight(), n.getName(), n.getParentId(), n.getDiscriminator()};

        // METHOD OF RETRIEVING GENERATED DATABASE PRIMARY KEYS
        BiFunction<TestNode, JdbcKeyHolder, Long> generatedKeyResolver = (node, jdbcKeyHolder) -> jdbcKeyHolder.getKeyValueAs(Long.class);

        JdbcNestedNodeRepositoryConfiguration<Long, TestNode> configuration = new JdbcNestedNodeRepositoryConfiguration<>(
                new JdbcTemplate(dataSource), tableName, mapper, insertQuery, insertValuesProvider, generatedKeyResolver, new TestJdbcTreeDiscriminator()
        );

        configuration.setIdColumnName("id");
        configuration.setParentIdColumnName("parent_id");
        configuration.setLeftColumnName("tree_left");
        configuration.setRightColumnName("tree_right");
        configuration.setLevelColumnName("tree_level");

        return JdbcNestedNodeRepositoryFactory.create(configuration);
    }

    @Bean
    @Mem
    public NestedNodeRepository<Long, TestNode> inMemoryRepository() {
        return InMemoryNestedNodeRepositoryFactory.create(IN_MEM_CONFIG, new InMemoryLock<>(TestNode::getDiscriminator));
    }

    private static InMemoryNestedNodeRepositoryConfiguration<Long, TestNode> inMemoryConfiguration() {
        return new InMemoryNestedNodeRepositoryConfiguration<>(
                ID::incrementAndGet, IN_MEM_NODES, new TestInMemoryTreeDiscriminator()
        );
    }

}
