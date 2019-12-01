package pl.exsio.nestedj.config.jdbc.discriminator;

import java.util.List;

public interface JdbcTreeDiscriminator {

    String getQueryPart();

    List<Object> getParameters();
}
