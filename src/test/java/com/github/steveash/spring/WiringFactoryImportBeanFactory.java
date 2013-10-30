package com.github.steveash.spring;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

/**
 * @author Steve Ash
 */
@Component
public class WiringFactoryImportBeanFactory extends WiringFactorySupport<WiringFactoryImportBean> {

    @Resource private String otherBean;

    public WiringFactoryImportBean make(String value) {
        Preconditions.checkState("something".equals(otherBean));
        WiringFactoryImportBean beanA = new WiringFactoryImportBean(value);

        return wire(beanA);
    }
}
