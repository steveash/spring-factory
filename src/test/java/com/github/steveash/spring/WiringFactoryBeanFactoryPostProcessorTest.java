package com.github.steveash.spring;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.steveash.spring.WiringFactoryBeanFactoryPostProcessorTest.ParentConfig;
import com.github.steveash.spring.beans.SpringFactoryBeans;
import com.google.common.base.Preconditions;

/**
 * @author Steve Ash
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ParentConfig.class)
public class WiringFactoryBeanFactoryPostProcessorTest {

    @Configuration
    @Import(SpringFactoryBeans.class)
    public static class ParentConfig {

        @Bean
        public AtomicReference<String> result() {
            return new AtomicReference<String>("before");
        }

        @Bean
        public String otherBean() {
            return "something";
        }
    }

    @Configuration
    @Import(ParentConfig.class)
    public static class SimpleConfig {

        @Bean
        public BeanAFactory beanAFactory() {
            return new BeanAFactory();
        }
    }

    @Configuration
    @Import(ParentConfig.class)
    @ImportResource("classpath:sfs-test.appconfig.xml")
    public static class XmlConfig {

    }

    public static class BeanA {

        @Resource public AtomicReference<String> result;

        public final String value;

        public BeanA(String value) {
            this.value = value;
        }

        @PostConstruct
        public void setResult() {
            Preconditions.checkNotNull(result);
            Preconditions.checkNotNull(value);
            result.set(value);
        }
    }

    public static class BeanAFactory extends WiringFactorySupport<BeanA> {
        @Resource private String otherBean;

        public BeanA make(String value) {
            Preconditions.checkState("something".equals(otherBean));
            BeanA beanA = new BeanA(value);

            return wire(beanA);
        }
    }

    @Test
    public void shouldAutowireProtoype() throws Exception {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SimpleConfig.class);
        BeanAFactory factory = ctx.getBean(BeanAFactory.class);

        AtomicReference<String> ref = ctx.getBean(AtomicReference.class);
        assertEquals("before", ref.get());

        BeanA beanA = factory.make("after");
        assertEquals("after", ref.get());
        assertEquals("after", beanA.result.get());

        BeanA beanAA = factory.make("again");
        assertEquals("again", ref.get());
        assertEquals("again", beanA.result.get());
        assertEquals("again", beanAA.result.get());
    }

    @Test
    public void shouldAutowireProtoypeFromXml() throws Exception {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(XmlConfig.class);
        BeanAFactory factory = ctx.getBean(BeanAFactory.class);

        AtomicReference<String> ref = ctx.getBean(AtomicReference.class);
        assertEquals("before", ref.get());

        BeanA beanA = factory.make("after");
        assertEquals("after", ref.get());
        assertEquals("after", beanA.result.get());

        BeanA beanAA = factory.make("again");
        assertEquals("again", ref.get());
        assertEquals("again", beanA.result.get());
        assertEquals("again", beanAA.result.get());
    }
}
