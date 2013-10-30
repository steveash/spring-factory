package com.github.steveash.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.steveash.spring.WiringFactoryImportSimple;

/**
 * @author Steve Ash
 */
@Import(WiringFactoryImportSimple.class)
@Configuration
public class WiringFactoryImportApp {
}
