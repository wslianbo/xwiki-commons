/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.configuration.internal.MemoryConfigurationSource;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.internal.ComponentRegistrator;

/**
 * Initialize a Component Manager and uses the {@link org.xwiki.test.annotation.AllComponents} and
 * {@link org.xwiki.test.annotation.ComponentList} annotations to decide what components to discover and register.
 *
 * Also offers helper APIs to register components and in-memory configuration sources.
 *
 * @version $Id$
 * @since 4.3.1
 */
public class ComponentManagerRule extends EmbeddableComponentManager implements MethodRule
{
    /**
     * Used to register components.
     */
    private ComponentRegistrator componentRegistrator = new ComponentRegistrator();

    /**
     * Register in-memory data source for the default and "xwikiproperties" configuration sources.
     *
     * @return the in-memory configuration source used for both default and "xwikiproperties" component hints
     * @throws Exception in case the registration fails
     */
    public MemoryConfigurationSource registerMemoryConfigurationSource() throws Exception
    {
        return this.componentRegistrator.registerMemoryConfigurationSource(this);
    }

    /**
     * Registers a component.
     *
     * @param roleType the type of the component role to register
     * @param roleHint the role hint of the component to register
     * @param instance the instance to register
     * @throws Exception in case of an error during registration
     */
    public void registerComponent(Type roleType, String roleHint, Object instance) throws Exception
    {
        this.componentRegistrator.registerComponent(roleType, roleHint, instance, this);
    }

    /**
     * Registers a component (with a default role hint).
     *
     * @param roleType the type of the component role to register
     * @param instance the instance to register
     * @throws Exception in case of an error during registration
     */
    public void registerComponent(Type roleType, Object instance) throws Exception
    {
        this.componentRegistrator.registerComponent(roleType, instance, this);
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target)
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                // If there are methods annotation with the BeforeComponent annotation then call them. This gives an
                // opportunity for the test to register some components *before* we register the other components below.
                Class<?> testClass = target.getClass();
                for (Method method : testClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(BeforeComponent.class)) {
                        method.invoke(target);
                    }
                }

                componentRegistrator.registerComponents(testClass, ComponentManagerRule.this);
                base.evaluate();
            }
        };
    }
}
