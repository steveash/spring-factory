/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.steveash.spring;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.stereotype.Component;

import com.google.common.base.CaseFormat;
import com.google.common.reflect.TypeToken;

/**
 * Bean Factory post processor to dynamically generate prototype definitions in the container for any
 * WiringFactory objects
 * @author Steve Ash
 */
@Component
public class WiringFactoryBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(WiringFactoryBeanFactoryPostProcessor.class);

    @Nullable
    static java.lang.Class<?> getPrototypeClassFromFactory(Class<?> factoryClass) {
        if (!WiringFactory.class.isAssignableFrom(factoryClass))
            return null;

        TypeToken<?> tok = TypeToken.of(factoryClass);
        TypeToken<?> prototypeTok = tok.resolveType(WiringFactory.class.getTypeParameters()[0]);
        return prototypeTok.getRawType();
    }

    @Nullable
    static java.lang.String getPrototypeBeanNameFromBeanClass(Class<?> prototypeBeanClass) {
        String prototypeBeanName = prototypeBeanClass.getSimpleName();
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, prototypeBeanName);
    }

    @Nullable
    static java.lang.String getPrototypeBeanNameFromFactory(Class<?> factoryClass) {
        Class<?> protoClass = getPrototypeClassFromFactory(factoryClass);
        if (protoClass == null)
            return null;

        return getPrototypeBeanNameFromBeanClass(protoClass);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.debug("Post processing the bean factory");
        String[] wiringFactories = beanFactory.getBeanNamesForType(WiringFactory.class, false, false);
        for (String beanDefName : wiringFactories) {
            Class<?> factoryType = checkNotNull(beanFactory.getType(beanDefName), "cant get type for bean");
            Class<?> factoryReturnType = checkNotNull(getPrototypeClassFromFactory(factoryType), "cant get return");

            addPrototypeDef(beanFactory, beanDefName, factoryReturnType);
        }
    }

    private void addPrototypeDef(ConfigurableListableBeanFactory beanFactory, String beanDefName, Class<?> protoBeanClass) {
        String beanName = getPrototypeBeanNameFromBeanClass(protoBeanClass);
        if (beanFactory.containsBeanDefinition(beanName)) {
            throw new BeanDefinitionValidationException("Trying to register a bean definition for a synthetic " +
                    "prototype bean with name " + beanName + " due to the bean factory of name " + beanDefName +
                    " but a bean with this name already exists!");
        }

        GenericBeanDefinition protoBean = new GenericBeanDefinition();
        protoBean.setLazyInit(true);
        protoBean.setBeanClass(protoBeanClass);
        protoBean.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        protoBean.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
        protoBean.setBeanClassName(protoBeanClass.getName());

        log.debug("Dynamically adding prototype bean {} from factory {}", beanName, beanDefName);
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        registry.registerBeanDefinition(beanName, protoBean);
    }
}
