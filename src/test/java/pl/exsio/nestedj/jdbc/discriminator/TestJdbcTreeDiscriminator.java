package pl.exsio.nestedj.jdbc.discriminator;

public class TestJdbcTreeDiscriminator implements JdbcTreeDiscriminator {

    @Override
    public String getQueryPart() {
        return "discriminator = 'tree_1'";
    }
}
