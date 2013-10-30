package com.github.steveash.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author Steve Ash
 */
@Lazy
@Import(WiringFactoryImportParent.class)
@Component
public class WiringFactoryImportSimple {

    @Bean
    public WiringFactoryImportBeanFactory beanAFactory() {
        return new WiringFactoryImportBeanFactory();
    }
}
