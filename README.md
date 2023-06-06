Lifecycle Manager plugin for vRealize/Aria Orchestrator

This plugin provides an interface to interact with Lifecycle Manager and its related components.

Features a fully functional plugin inventory for review and executing actions upon.

# Installation

See the [Releases](releases/) folder for the built VMOAPP and accompanying Workflow Package.

To install the VMOAPP file, login to your Orchestrator appliance Control Center (https://orchestrator.local/vco-controlcenter/config) as root.

Select *Manage Plugins* and upload the VMOAPP to install it. This will restart Orchestrator services in 2 minutes.

Once Orchestrator is back up, login and using the client, go to Packages and Import the .package file from the release.

For some reason the SDK Mojo Maven plugin will not import workflows directly into the VMOAPP, so until I get that resolved, it's a two-step process.

# Specific Features and Workflows Supported

This plugin should install and function identically on Orchestrator **8.4.2** and higher.

The ability to deploy/scale products and nodes may come later.

For now, this is what it can do:
- Datacenters : Get, Create, Update, Delete
- vCenters : Get
- Environments : Get
- Certificates : Get, Create, Import, Delete
- Credentials : Get, Create, Update, Delete
- Products: Get
- Product Nodes: Get

# Semi-Supported Resources

Currently, the following products are not guaranteed to work with the plugin but they *should*. Open an issue if you see anything amiss.

- VMware Cloud Services Data Collector
- vROPS Cloud Proxy
- vRNI Cloud Proxy
- Cloud Extensibility Proxy

# Roadmap

- Complete coverage of Aria/vRealize product Day 2 actions
- Manage RBAC
- Extend Identity Manager management (directory setup, etc)

# Authentication Notes

This plugin supports Basic Authentication and vIDM-integrated user access setups.

By default, only the user account **admin@local** can do everything in the plugin without errors.

If you want to make this possible with a vIDM (aka provider) user or service account, you will need to add the user to the hidden **LCM_ADMIN** role, only doable from the internal/private API using the **admin@local** credentials.

Here are the steps to do this at a high level:

* Perform a **GET /lcm/authzn/api/roles/extended** to get the 'vmid' of the LCM_ADMIN role.
* Perform a **GET /lcm/authzn/api/users/fromprovider** to find the 'vmid' of your Domain User account in the system.
* Perform a **POST /lcm/authzn/api/userrolemapping** with a JSON string containing 'rolevmid' and 'uservmid' with the appropriate values.

After you reload, login with that user, and you should see everything.

# Notes on Roles

The roles in LCM are as follows with notes on plugin usage.

* LCM_ADMIN - This role is essentially the 'admin@local' user. Accounts in this role have full rights into the system.
* LCM_CLOUD_ADMIN - This role is for external/AD users that are Administrators. Has full Lifecycle Operations and Locker access, but not User or Content Management.
* CONTENT_DEVELOPER - Developer access in Content Management only. From a plugin point of view, this role can't do anything at this time.
* RELEASE_MANAGER - Release Manager access in Content Management only. From a plugin point of view, this role can't do anything at this time.
* LOCKER_CERTIFICATE_ADMIN - Can only do Certificate-related operations across the platform. Can do anything with Certificate objects and do Day 2 operations for updating Environment Products and Node certificates.

# Active Directory User Notes

Identity Manager users in Active Directory domains *cannot* by default communicate with the Lifecycle Manager API and require a specific OAUTH2 client to be created to enable it.

To enable this, you must create an OAUTH2 Client in **Identity Manager** that has the 'password' grant type enabled.
This type of client can only be generated via the API by the **admin** user (note: __*not*__ the configadmin user!) so have the following information handy:

1. Identity Manager FQDN/LB URL
2. Identity Manager local **Admin** account
3. Identity Manager local **Admin** password
4. AD Service Account - User/Password/Domain

# Creating the OAUTH2 Client

You can use any API client you want like Postman, or Powershell, cURL to do these steps.

### Step 1 - Request Admin Session Token

```
POST https://{IDM URL}/SAAS/API/1.0/REST/auth/system/login

Content-Type: application/json
Accept: application/json

{
	"username": "admin",
	"password": "local_admin_password",
	"issueToken": true
}
```

The response will return a **.sessionToken** string for the next call.

### Step 2 - Create OAUTH2 Client with Password Grant

For the **secret** in this payload, make sure you do a Base64 encode of the value first.

```
POST https://{IDM URL}/SAAS/jersey/manager/api/oauth2clients

Accept: application/vnd.vmware.horizon.manager.oauth2client+json
Content-Type: application/vnd.vmware.horizon.manager.oauth2client+json
Authorization:HZN {sessionToken}

{
  "clientId": "ActiveDirectoryAuthClient",    // Name this whatever you want.
  "secret": "c3VwZXJkdXBlcnNlY3JldA==",       // Base64 Encoded value of 'superdupersecret'
  "authGrantTypes": "password",               // Must be password
  "tokenType": "Bearer",                      // Must be Bearer
  "accessTokenTTL": 3600,                     // Number of MINUTES the issued Access Token will be valid in the Identity Manager database
  "scope": "user admin",
  "displayUserGrant": true
}
```

You should receive a HTTP 201 indicating the client was created.

**Any update operation on this client** will revoke the password grant and it will need to be recreated **from scratch** using this process!



### Step 3 - Verify LCM AD Account can authenticate

Using the **clientId** and **secret** used earlier, you can get a Bearer token to pass to subsequent requests.

```
POST https://{IDM URL}/SAAS/auth/oauthtoken?grant_type=password

Authorization: Basic <Base64 encoded clientId:secret>

Body (form-data):
username=<AD Username>
password=<AD Password>
domain=<AD Domain>
```

Assuming everything lines up, you'll receive a response containing your Bearer Token you can then use in LCM API calls in the Authorization header.

You should now be able to use the **Create vRSLCM Connection** or **Update vRSLCM Connection** workflows to utilize service accounts.

# Notes

This is a pet project of mine mainly to learn how to build a plugin and what I've learned.

I hope its helpful for you, since it's tough to find good docs on this topic.

