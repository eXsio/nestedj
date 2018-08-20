package pl.exsio.nestedj.discriminator;

import pl.exsio.nestedj.model.TestNode;

import java.util.HashMap;
import java.util.Map;

public class TestTreeDiscriminator extends MapTreeDiscriminator<Long, TestNode> {


    public TestTreeDiscriminator() {
        Map<String, ValueProvider> valueProviders = new HashMap<>();
        valueProviders.put("discriminator", () -> "tree_1");

        setValueProviders(valueProviders);
    }
}
