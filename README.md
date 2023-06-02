# Lifecycle Manager plugin for vRealize/Aria Orchestrator

This plugin provides an interface to interact with Lifecycle Manager and its related components.

Features a fully functional plugin inventory for review and executing actions upon.

# Specific Features and Workflows Supported

The ability to deploy/scale products and nodes may come later.

For now, this is what it can do:
- Datacenters : Get, Create, Update, Delete
- vCenters : Get
- Environments : Get
- Certificates : Get, Create, Import, Update, Delete
- Credentials : Get, Create, Update, Delete
- Products: Get
- Product Nodes: Get

# Roadmap

- Complete coverage of Aria/vRealize product Day 2 actions
- Manage RBAC
- Extend Identity Manager management (directory setup, etc)

# Authentication Notes

This plugin supports Basic Authentication for users in the **@local** space, such as **admin@local**.
It also supports authentication with AD integrated domains through VMware Identity Manager.

Identity Manager users in Active Directory domains *cannot* by default communicate with the Lifecycle Manager API and require a specific OAUTH2 client to be created to enable it.

To enable this, you must create an OAUTH2 Client in **Identity Manager** that has the 'password' grant type enabled.
This type of client can only be generated via the API by the **admin** user (note: __*not*__ the configadmin user!) so have the following information handy:

1. Identity Manager FQDN/LB URL
2. Identity Manager local **Admin** account
3. Identity Manager local **Admin** password
4. AD Service Account - User/Password/Domain

# Creating the OAUTH2 Client

You can use any API client you want like Postman, or Powershell, cURL to do these steps.

1. Request Admin Session Token

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

2. Create OAUTH2 Client with Password Grant

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
  "displayUserGrant": true,
}
```

You should receive a HTTP 201 indicating the client was created.

**Any update operation on this client** will revoke the password grant and it will need to be recreated **from scratch** using this process!



3. Verify LCM AD Account can authenticate

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
So, the classes are pretty heavily commented! I hope its helpful for you, since it's tough to find good docs on this topic.

