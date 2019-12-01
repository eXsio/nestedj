package pl.exsio.nestedj.config.jdbc.discriminator;

import com.google.common.collect.Lists;

import java.util.List;

public class TestJdbcTreeDiscriminator implements JdbcTreeDiscriminator {

    @Override
    public String getQueryPart() {
        return "discriminator = ?";
    }

    @Override
    public List<Object> getParameters() {
        return Lists.newArrayList("tree_1");
    }
}
