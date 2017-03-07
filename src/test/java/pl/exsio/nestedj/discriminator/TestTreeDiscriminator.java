package pl.exsio.nestedj.discriminator;

import pl.exsio.nestedj.model.TestNodeImpl;

import java.util.HashMap;
import java.util.Map;

public class TestTreeDiscriminator extends TreeDiscriminatorImpl<TestNodeImpl> {


    public TestTreeDiscriminator() {
        Map<String, ValueProvider> valueProviders = new HashMap<>();
        valueProviders.put("discriminator", new ValueProvider() {
            @Override
            public Object getDiscriminatorValue() {
                return "tree_1";
            }
        });

        setValueProviders(valueProviders);
    }
}
