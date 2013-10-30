package com.github.steveash.spring;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Steve Ash
 */
public class WiringFactoryImportTest {

    @Test
    public void shouldAutowireProtoype() throws Exception {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(WiringFactoryImportApp.class);
        WiringFactoryImportBeanFactory factory = ctx.getBean(WiringFactoryImportBeanFactory.class);

        AtomicReference<String> ref = ctx.getBean(AtomicReference.class);
        assertEquals("before", ref.get());

        WiringFactoryImportBean beanA = factory.make("after");
        assertEquals("after", ref.get());
        assertEquals("after", beanA.result.get());

        WiringFactoryImportBean beanAA = factory.make("again");
        assertEquals("again", ref.get());
        assertEquals("again", beanA.result.get());
        assertEquals("again", beanAA.result.get());
    }
}
