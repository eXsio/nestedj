package pl.exsio.nestedj.config.mem.discriminator;

import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;

public interface InMemoryTreeDiscriminator<ID extends Serializable, N extends NestedNode<ID>> {

    boolean applies(N node);
}
