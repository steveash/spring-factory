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

package com.github.steveash.spring.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.steveash.spring.WiringFactoryBeanFactoryPostProcessor;

/**
 * Defines the beans and bean factories that enable the extended behavior of the generated proxy factories
 * @author Steve Ash
 */
@Configuration
public class SpringFactoryBeans {

    @Bean
    public static WiringFactoryBeanFactoryPostProcessor wiringFactoryBeanFactoryPostProcessor() {
        return new WiringFactoryBeanFactoryPostProcessor();
    }
}
