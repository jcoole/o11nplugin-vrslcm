package com.sprockitconsulting.vrslcm.plugin.endpoints;

import java.util.Base64;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprockitconsulting.vrslcm.plugin.scriptable.ConnectionInfo;

/**
 * This class contains the logic to generate Authorization headers that authenticate to LCM.
 * Depending on the ConnectionInfo, it will generate a Basic or Bearer token.
 * @author justin
 *
 */
@Component
@Qualifier(value = "connectionAuthentication")
@Scope(value = "prototype")
@DependsOn({"connectionRepository", "vroRestTemplate", "vroObjectMapper"})
public class ConnectionAuthentication {
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(ConnectionAuthentication.class);
	
	private String token;
	private String tokenType;
	private String tokenMessage = "Token not acquired.";
	private boolean tokenIsValid = false;
	private ConnectionInfo connectionInfo;
	
	@Autowired
	private RestTemplate vroRestTemplate;
	@Autowired
	private ObjectMapper vroObjectMapper;
	
	public ConnectionAuthentication() {}

	public ConnectionAuthentication(ConnectionInfo info) throws JsonMappingException, JsonProcessingException {
		log.debug("Initializing ConnectionAuthentication - for Connection ID ["+info.getId()+"]");
		this.connectionInfo = info;
	}

	public String getToken() {
		return token;
	}

	public String getTokenType() {
		return tokenType;
	}
	
	public String getTokenMessage() {
		return tokenMessage;
	}

	public boolean isTokenValid() {
		return tokenIsValid;
	}

	public ConnectionInfo getConnectionInfo() {
		return connectionInfo;
	}
	
	public void setConnectionInfo(ConnectionInfo connectionInfo) throws JsonMappingException, JsonProcessingException {
		// Check to see if credentials have changed for vIDM use case only, if so re-run the acquireToken() method.
		if(this.connectionInfo.getUserDomain() == "local" && connectionInfo.getUserDomain() == "local") {
			log.debug("The updated connection is still using local credentials, no token changes needed.");
			this.connectionInfo = connectionInfo;
		} else {
			// Some sort of external domain in use for vIDM, changes may be needed.
			if( (this.connectionInfo.getIdentityManagerHost() != connectionInfo.getIdentityManagerHost() ) ||
			    (this.connectionInfo.getIdentityManagerClientId() != connectionInfo.getIdentityManagerClientId() ) ||
			    (this.connectionInfo.getIdentityManagerClientSecret() != connectionInfo.getIdentityManagerClientSecret() ) ||
			    (this.connectionInfo.getUserDomain() != connectionInfo.getUserDomain() ) ||
			    (this.connectionInfo.getUserName() != connectionInfo.getUserName() ) ||
			    (this.connectionInfo.getUserPassword() != connectionInfo.getUserPassword() )
			  ) {
				log.debug("Updated ConnectionInfo differs from current configuration, using new values to re-acquire Bearer token.");
				this.connectionInfo = connectionInfo;
				acquireToken();
				log.debug("Updated Bearer Token acquired");
			} else {
				log.debug("Updated ConnectionInfo value is the same as current configuration, nothing to do.");
				this.connectionInfo = connectionInfo;
			}
		}
	}

	// Helper for Base64 encode
	private String encodeBase64(String str) {
		return Base64.getEncoder().encodeToString((str).getBytes());
	}


	/**
	 * This method is used to generate the Authorization header.
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	public void acquireToken() throws JsonMappingException, JsonProcessingException {
		log.debug("Begin acquireToken()");

		if(
			this.connectionInfo.getIdentityManagerHost() != null &&
			this.connectionInfo.getIdentityManagerClientId() != null &&
			this.connectionInfo.getIdentityManagerClientSecret() != null
		) {
			// If the vIDM host is set, vIDM is to be used.
			log.debug("vIDM required values found, proceeding with Bearer auth API call");
			
			// Setup headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			headers.setBasicAuth(this.connectionInfo.getIdentityManagerClientId(), this.connectionInfo.getIdentityManagerClientSecret());
			
			// Setup the form-data
			MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
			formData.add("username", this.connectionInfo.getUserName() );
			formData.add("password", this.connectionInfo.getUserPassword() );
			formData.add("domain", this.connectionInfo.getUserDomain() );
			
			HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(formData, headers);
			
			String fullUrl = "https://"+this.connectionInfo.getIdentityManagerHost()+"/SAAS/auth/oauthtoken?grant_type=password";
			
			String response = null;
			// Using the Orchestrator Custom RestTemplate (auto-wired), execute the call and return based on 'responseType'
			try {
				log.debug("Begin Bearer Token Request to "+fullUrl);
				ResponseEntity<String> responseEntity = vroRestTemplate.exchange(fullUrl, HttpMethod.resolve("POST"), req, String.class);
				log.debug("Bearer Token response code "+responseEntity.getStatusCodeValue()+", HTTP status "+responseEntity.getStatusCode().getReasonPhrase()+", content: **REDACTED**");
				// Check the status code.
				if(responseEntity.getStatusCodeValue() >= 200 && responseEntity.getStatusCodeValue() < 400) {
					response = responseEntity.getBody();
					log.debug("Bearer Token Request successful!");
				} else if(responseEntity.getStatusCodeValue() == 400) {
					// 400 is bad request to the vIDM provider (usually Active Directory).
					String msg = "The AD User ["+this.connectionInfo.getUserName()+"@"+this.connectionInfo.getUserDomain()+"] did not successfully authenticate through Identity Manager. Verify user, password, and domain are correct and that the account is not expired.";
					this.tokenMessage = msg;
				} else if(responseEntity.getStatusCodeValue() == 401) {
					// 401 is invalid client used to communicate with vIDM.
					String msg = "The Client ID ["+this.connectionInfo.getIdentityManagerClientId()+"] used in the request returned 401 (Unauthorized). Check the ID and secret values, and ensure the OAUTH2 client is correctly configured. For reference you can look at the README on the main Github site: https://github.com/jcoole/o11nplugin-vrslcm";
					this.tokenMessage = msg;
				} else {
					log.error(this.getClass().getEnclosingMethod().getName()+" unhandled error, status code ["+responseEntity.getStatusCodeValue()+"], body ["+responseEntity.getBody()+"]");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			JsonNode bearerTokenObject = null;
			try {
				// Convert response to generic object
				bearerTokenObject = vroObjectMapper.readTree(response);
			} catch (RuntimeException e) {
				log.error("Error reading token into object : "+e.getMessage());
			}
			
			// Get the access token and assign
			if(bearerTokenObject != null) {
				this.token = bearerTokenObject.get("access_token").asText();
				this.tokenType = bearerTokenObject.get("token_type").asText();
				this.tokenIsValid = true;
				this.tokenMessage = this.tokenType+" Token Acquired, expires in "+bearerTokenObject.get("expires_in").asText()+"] seconds";
			} else {
				this.tokenType = "Error";
				this.token = "Error";
			}
		} else {
			// Default path, setup Basic Auth.
			log.debug("vIDM required values not present, returning Basic auth");
			String authToEncode = this.connectionInfo.getUserName()+"@"+this.connectionInfo.getUserDomain()+":"+this.connectionInfo.getUserPassword();
			this.tokenType = "Basic";
			this.token = encodeBase64(authToEncode);
			this.tokenIsValid = true;
			this.tokenMessage = this.tokenType+" Token created";
		}
		log.debug("acquireToken() complete, resulting Type: "+this.tokenType);
	}
	
	@Override
	public String toString() {
		return String.format("ConnectionAuthentication [token=%s, tokenType=%s]", token, tokenType);
	}
	
}
