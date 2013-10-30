package com.github.steveash.spring;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

/**
 * @author Steve Ash
 */
@Component
public class WiringFactoryImportBean {
    @Resource public AtomicReference<String> result;

    public final String value;

    public WiringFactoryImportBean(String value) {
        this.value = value;
    }

    @PostConstruct
    public void setResult() {
        Preconditions.checkNotNull(result);
        Preconditions.checkNotNull(value);
        result.set(value);
    }
}
