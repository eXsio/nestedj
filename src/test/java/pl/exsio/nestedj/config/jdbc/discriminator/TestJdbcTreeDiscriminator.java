package pl.exsio.nestedj.config.jdbc.discriminator;

import java.util.ArrayList;
import java.util.List;

public class TestJdbcTreeDiscriminator implements JdbcTreeDiscriminator {

    @Override
    public String getQueryPart() {
        return "discriminator = ?";
    }

    @Override
    public List<Object> getParameters() {
        ArrayList<Object> result = new ArrayList<>();
        result.add("tree_1");
        return result;
    }
}
