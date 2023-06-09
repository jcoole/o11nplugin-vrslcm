package com.sprockitconsulting.vrslcm.plugin;

import com.vmware.o11n.plugin.sdk.annotation.Cardinality;
import com.vmware.o11n.plugin.sdk.module.ModuleBuilder;

/**
 * The ModuleBuilder is used to generate the data around the plugin itself, as well as instructions for building VSO.xml.
 * VSO.xml is a file that contains all of the mapping for object properties, methods, and finders, built from annotations.
 * @author VMware SDK Archetype
 *
 */
public final class vRSLCMModuleBuilder extends ModuleBuilder {
	private static final String MODULE_NAME = "vRSLCM";
    private static final String DESCRIPTION = "vRealize/Aria Lifecycle Manager plug-in for Orchestrator";
    private static final String DATASOURCE = "main-datasource";

    @Override
    public void configure() {
        module(MODULE_NAME) // Name of the plugin for type prefix. This is prepended onto Scriptable Objects for uniqueness.
        .withDescription(DESCRIPTION) // See the definition above.
        .withImage("images/vrslcm-conn.png") // The relative path to the plugin Icon seen in ControlCenter as well as the root of the Plugin Inventory.
        .basePackages(vRSLCMModuleBuilder.class.getPackage().getName()).version(
            "${project.version}")
            .classpath("common", null
        );

        // Specify installation parameters when plugin is uploaded to Orchestrator.
        // You can install workflow packages, execute a script, or a workflow upon install.
        installation(InstallationMode.BUILD).action(ActionType.INSTALL_PACKAGE,
            "packages/${project.artifactId}-package-${project.version}.package" // default workflow package
		);

        // Declares the class(es) containing the finders, which by default is the PluginAdaptor
        finderDatasource(vRSLCMPluginAdaptor.class, DATASOURCE).anonymousLogin(LoginMode.INTERNAL);

        // Create an inventory with a default finder. This can be called whatever, like 'Root' or 'Plugin' or 'Bababouie'
        inventory("Plugin");
        
        /*
         * For expanding the inventory tree, you can proceed in one of two ways:
         * 
         *   - Use the below template to create additional relationships between classes using the builder.
         *     You can just keep invoking the following in this class:
         *     	'this.finder(<ParentTypeClassName>, DATASOURCE).addRelation(<ChildTypeClassName>, <RelationName>, Show in Inventory, Cardinality)'
         *     And when Maven builds the plugin, it will create the necessary annotations.
         *     
         *   - Add the VsoFinder annotation to the parent classes, and define one or more relations there.
         *   
         *   In this plugin, I opted to annotate the classes that require API use, but I left the folders in the builder here as an example of how to potentially use it.
         *   If both are utilized for one class, the ModuleBuilder wins out!
         */

        // Create relation for Plugin (Root Level) --> Connections
        this.finder("Plugin", DATASOURCE)
        	.addRelation("Connection", "Connections", true, Cardinality.TO_MANY);
    
        // Create relation for Connections -> Service Folders
        this.finder("Connection", DATASOURCE)
        	.addRelation("LifecycleOperationsFolder","LifecycleOperationsFolders", true,Cardinality.TO_MANY)
        	.addRelation("LockerFolder","LockerFolders", true, Cardinality.TO_MANY);
        	//.addRelation("UserManagementFolder","UserManagementFolders", true, Cardinality.TO_MANY)
        	//.addRelation("ContentManagementFolder","ContentManagementFolders", true);
        
        // Create relation for the Lifecycle Operations -> DC/Env/Req Folders.
        this.finder("LifecycleOperationsFolder", DATASOURCE)
        	.addRelation("DatacenterFolder", "DatacenterFolders", true)
        	.addRelation("EnvironmentFolder", "EnvironmentFolders", true)
        	.addRelation("RequestFolder", "RequestFolders", true);

        // And so on...
    }
}
