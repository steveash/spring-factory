package com.github.steveash.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;


/**
 * @author Steve Ash
 */
public class WiringFactorySupportTest {

    public static class BeanA {}

    public static class BeanAFactory1 extends WiringFactorySupport<BeanA> {}

    public static class BeanAFactory2 implements WiringFactory<BeanA> {}

    public static class BeanAFactory3 extends BeanAFactory1 {}

    @Test
    public void shouldGetBeanName() throws Exception {
        assertEquals("beanA", WiringFactoryBeanFactoryPostProcessor.getPrototypeBeanNameFromFactory(BeanAFactory1.class));
        assertEquals("beanA", WiringFactoryBeanFactoryPostProcessor.getPrototypeBeanNameFromFactory(BeanAFactory2.class));
        assertEquals("beanA", WiringFactoryBeanFactoryPostProcessor.getPrototypeBeanNameFromFactory(BeanAFactory3.class));

        assertEquals(BeanA.class, WiringFactoryBeanFactoryPostProcessor.getPrototypeClassFromFactory(BeanAFactory1.class));

        assertNull(WiringFactoryBeanFactoryPostProcessor.getPrototypeClassFromFactory(WiringFactorySupportTest.class));
    }
}
