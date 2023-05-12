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
    private static final String DESCRIPTION = "vRealize Suite Lifecycle Manager plug-in for vRealize Orchestrator";
    private static final String DATASOURCE = "main-datasource";

    @Override
    public void configure() {
        module(MODULE_NAME) // Name of the plugin for type prefix. This is prepended onto Scriptable Objects for uniqueness.
        .withDescription(DESCRIPTION) // See the definition above.
        .withImage("images/vrslcm-root.png") // The relative path to the plugin Icon seen in ControlCenter as well as the root of the Plugin Inventory.
        .basePackages(vRSLCMModuleBuilder.class.getPackage().getName()).version(
            "${project.version}")
            .classpath("common", null
        );

        // Specify installation parameters when plugin is uploaded to vRO.
        // You can install workflow packages, execute a script, or a workflow upon install.
        installation(InstallationMode.BUILD).action(ActionType.INSTALL_PACKAGE,
            "packages/${project.artifactId}-package-${project.version}.package" // workflow package
		);

        // Declares the class(es) containing the finders, which by default is the PluginAdaptor
        finderDatasource(vRSLCMPluginAdaptor.class, DATASOURCE).anonymousLogin(LoginMode.INTERNAL);

        // Create an inventory with a default finder. This can be called whatever, like 'Root' or 'Plugin' or 'Bababouie'
        inventory("Plugin");
        
        //TODO: replace these finders with annotations in their respective classes!

        // Create relation for Plugin --> Connections
        this.finder("Plugin", DATASOURCE)
        	.addRelation("Connection", "Connections", true, Cardinality.TO_MANY); // Type, Finder Name, Show in Inventory, and cardinality enum
        
        // Create relation for Connections -> Service Folders
        this.finder("Connection", DATASOURCE)
        	.addRelation("LifecycleOperationsFolder","LifecycleOperationsFolders", true,Cardinality.TO_MANY)
        	.addRelation("LockerFolder","LockerFolders", true, Cardinality.TO_MANY)
        	.addRelation("UserManagementFolder","UserManagementFolders", true, Cardinality.TO_MANY)
        	.addRelation("ContentManagementFolder","ContentManagementFolders", true);
        
        // Create relation for the Lifecycle Operations -> DC/Env/Req Folders.
        this.finder("LifecycleOperationsFolder", DATASOURCE)
        	.addRelation("DatacenterFolder", "DatacenterFolders", true)
        	.addRelation("EnvironmentFolder", "EnvironmentFolders", true)
        	.addRelation("RequestFolder", "RequestFolders", true);
/*     
        // Create relation for the Locker -> Cert/Cred Folders.
        this.finder("LockerFolder", DATASOURCE)
        	.addRelation("CertificateFolder", "CertificateFolders", true)
        	.addRelation("CredentialFolder", "CredentialFolders", true);
*/
/*        
        // Create relation for the CertificateFolder -> Certificates.
        this.finder("CertificateFolder", DATASOURCE)
        	.addRelation("Certificate", "Certificates", true, Cardinality.TO_MANY);
*/
        
/*
        // Create relation for the DatacenterFolder -> Datacenters.
        this.finder("DatacenterFolder", DATASOURCE)
        	.addRelation("Datacenter", "Datacenters", true, Cardinality.TO_MANY);
*/
        
        // Create relation for the Datacenter objects to vCenter Servers.
        this.finder("Datacenter", DATASOURCE)
        	.addRelation("VirtualCenter", "VirtualCenters", true, Cardinality.TO_MANY);

        // Create relation for the EnvironmentFolder -> Environments.
        this.finder("EnvironmentFolder", DATASOURCE)
        	.addRelation("Environment", "Environments", true);

        // Create relation for the RequestFolder -> Requests.
        this.finder("RequestFolder", DATASOURCE)
        	.addRelation("Request", "Requests", true);
        
    }
}
