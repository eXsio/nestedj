package pl.exsio.nestedj.jdbc.discriminator;

import java.util.List;

public interface JdbcTreeDiscriminator {

    String getQueryPart();

    List<Object> getParameters();
}
