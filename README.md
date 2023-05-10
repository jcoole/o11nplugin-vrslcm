# Lifecycle Manager plugin for vRealize/Aria Orchestrator
This plugin provides an interface to interact with Lifecycle Manager and its related components.

Features a fully functional plugin inventory for review and executing actions upon.

With the included workflows and actions, you can use this plugin to: 
- Integrate with external systems related to managing credentials and certificate lifecycle
- Perform synchronization of Identity Manager directories and tenants


## Specific Features and Workflows Supported
The plugin currently is designed to perform Day 2 Actions on managed resources.
The ability to deploy/scale products and nodes may come later.

For now, this is what it can do:
- Datacenters : Create, Update, Delete
- vCenters : Create, Update, Delete, Inventory Sync
- Environments : Inventory Sync
- Products
    - Common Actions: Power Cycle, Snapshot, Inventory Sync
	- Automation / Orchestrator : Reconfigure K8S IP
	- Identity Manager : Remediate
	
- Locker Certificates : Create, Import, Assign, Delete
- Locker Credentials : Create, Assign, Delete

## Roadmap
- Complete coverage of Aria product Day 2 actions
- Authentication support using Identity Manager OAUTH2 and Active Directory (service accounts)
- Manage RBAC
- Extend Identity Manager management

## Notes
This is a pet project of mine mainly to learn how to build a plugin and what I've learned.
So, the classes are pretty heavily commented! I hope its helpful for you, since it's tough to find good docs on this topic.

