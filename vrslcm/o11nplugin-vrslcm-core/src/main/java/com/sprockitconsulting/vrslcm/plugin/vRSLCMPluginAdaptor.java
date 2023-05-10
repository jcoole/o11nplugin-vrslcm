package com.sprockitconsulting.vrslcm.plugin;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.vmware.o11n.plugin.sdk.spring.AbstractSpringPluginAdaptor;

// Step 1 - The Plugin Adaptor is what is loaded during server startup.
public final class vRSLCMPluginAdaptor extends AbstractSpringPluginAdaptor {
	// Enable Logging - not normally in source file.
	private static final Logger log = LoggerFactory.getLogger(vRSLCMPluginAdaptor.class);
	
	// This file is not created as part of the archetype, so you have to make it yourself.
	// Reference Document (page 8-9) : https://communities.vmware.com/wbsdv95928/attachments/wbsdv95928/4527/14/1/vCO%20Plug-in%20Dev%20Tools%20-%201.0.0%20Documentation.pdf
    private static final String DEFAULT_CONFIG = "com/sprockitconsulting/vrslcm/plugin/pluginConfig.xml";

    // This value is obvious, but unused here. The ModuleBuilder class actually does the work.
    public static final String PLUGIN_NAME = "vRealize Suite Lifecycle Manager";

    // This value by default contains "${rootElement}" which is an archetype parameter.
    // For the 'o11n-archetype-spring' you can pass it, but it won't properly replace it.
    // Update it to point to your root object (such as, "Connection" in most other docs)
    static final String ROOT = "ConnectionFinder";
    static final String REL_ROOTS = "roots";

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
 
        // Just for fun - try listing all beans/singletons
        log.debug("Looking up registered Singletons at startup");
        String[] singletonNames = applicationContext.getBeanFactory().getSingletonNames();
        log.debug("Found ["+applicationContext.getBeanFactory().getSingletonCount()+"] singletons :: "+Arrays.toString(singletonNames));
        
        log.debug("Looking up other beans at startup");
        String[] beans = applicationContext.getBeanFactory().getBeanDefinitionNames();
        log.debug("Found ["+applicationContext.getBeanFactory().getBeanDefinitionCount()+"] beans :: "+Arrays.toString(beans));
        
        return applicationContext;
    }
}
