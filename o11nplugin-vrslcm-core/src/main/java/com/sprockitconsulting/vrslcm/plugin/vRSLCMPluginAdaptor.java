package com.sprockitconsulting.vrslcm.plugin;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.vmware.o11n.plugin.sdk.spring.AbstractSpringPluginAdaptor;

/**
 * The PluginAdaptor is the starting point of loading the plugin at server startup and subsequently the ApplicationContext used by Spring.
 * @author justin
 *
 */
public final class vRSLCMPluginAdaptor extends AbstractSpringPluginAdaptor {
	
	// Enable Logging - not normally in source file when generated from the VMware archetype.
	private static final Logger log = LoggerFactory.getLogger(vRSLCMPluginAdaptor.class);
	
	// This file can be used to configure your Spring beans at startup, and is an optional parameter to the 'createApplicationContext()' method.
	// Generally speaking, use Spring annotations for most cases, this is just here as a reference.
	// A Reference Document (page 8-9) : https://communities.vmware.com/wbsdv95928/attachments/wbsdv95928/4527/14/1/vCO%20Plug-in%20Dev%20Tools%20-%201.0.0%20Documentation.pdf
    private static final String DEFAULT_CONFIG = "com/sprockitconsulting/vrslcm/plugin/pluginConfig.xml";

    /**
     * Creates the plugin Spring ApplicationContext during server startup.
     * The ApplicationContext is used to hold singleton and prototype "beans" ie Classes that can be injected throughout the framework.
     * 
     * From this method, the vRSLCMPluginFactory is instantiated - see that class next.
     */
    @Override
    protected ApplicationContext createApplicationContext(ApplicationContext defaultParent) {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
    		new String[] { DEFAULT_CONFIG }, false, defaultParent
		);
        applicationContext.setClassLoader(getClass().getClassLoader());
        applicationContext.refresh();
 
        // Lists all beans/singletons, useful for debugging.
        log.debug("Looking up registered Singletons at startup");
        String[] singletonNames = applicationContext.getBeanFactory().getSingletonNames();
        log.debug("Found ["+applicationContext.getBeanFactory().getSingletonCount()+"] singletons :: "+Arrays.toString(singletonNames));
        
        log.debug("Looking up other beans at startup");
        String[] beans = applicationContext.getBeanFactory().getBeanDefinitionNames();
        log.debug("Found ["+applicationContext.getBeanFactory().getBeanDefinitionCount()+"] beans :: "+Arrays.toString(beans));
        
        return applicationContext;
    }
}
