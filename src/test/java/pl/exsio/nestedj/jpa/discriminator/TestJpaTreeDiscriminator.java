package pl.exsio.nestedj.jpa.discriminator;

import pl.exsio.nestedj.model.TestNode;

import java.util.Collections;

public class TestJpaTreeDiscriminator extends MapJpaTreeDiscriminator<Long, TestNode> {

    public TestJpaTreeDiscriminator() {
        super(Collections.singletonMap("discriminator", () -> "tree_1"));
    }
}
