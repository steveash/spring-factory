package com.github.steveash.spring;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import com.github.steveash.spring.WiringFactoryBeanFactoryPostProcessor;
import com.github.steveash.spring.beans.SpringFactoryBeans;

/**
 * @author Steve Ash
 */
@Lazy
@Configuration
@Import(SpringFactoryBeans.class)
public class WiringFactoryImportParent {

    @Bean
    public AtomicReference<String> result() {
        return new AtomicReference<String>("before");
    }

    @Bean
    public String otherBean() {
        return "something";
    }
}
