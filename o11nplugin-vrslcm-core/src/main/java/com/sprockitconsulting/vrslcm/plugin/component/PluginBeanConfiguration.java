package com.sprockitconsulting.vrslcm.plugin.component;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import ch.dunes.vso.sdk.ssl.ISslService;

/**
 * This class is used to generate various Spring Beans needed for plugin usage.
 * @author justin
 *
 */
@Configuration
@DependsOn("connectionRepository")
public class PluginBeanConfiguration {
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(PluginBeanConfiguration.class);
	
	@Autowired
	private ISslService sslService;
	
	public PluginBeanConfiguration() {
		log.debug("Constructor initialized");
	}
	
	/**
	 * The vroRestTemplate bean is a customized RestTemplate object that is integrated with the Orchestrator keystore and SSL services.
	 * It is initialized once at plugin startup and is available for any service to use.
	 * @return Customized RestTemplate used for REST API calls.
	 */
	@Bean
	public RestTemplate vroRestTemplate() {
		log.debug("Initializing Orchestrator RestTemplate implementation");
		
		// Establish the SSL Context through Orchestrator
		SSLContext context = null;
		try {
			context = sslService.newSslContext("SSL");
			log.debug("SSL Context created : ["+context.getProvider()+" - "+context.getProtocol()+"]");

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		// Next the SSL Connection Socket Factory, implementing the Orchestrator SSL context
		SSLConnectionSocketFactory sslConnectionFactory = null;
		//log.debug("Attempting SSL Factory with context ["+context.getProvider()+" - "+context.getProtocol()+"]");
		try {
			sslConnectionFactory = new SSLConnectionSocketFactory(context);
			log.debug("SSL Factory created : "+sslConnectionFactory);
		} catch (RuntimeException e) {
			throw new RuntimeException("Error during SSL Connection Socket Factory :: "+e);
		}

		// Setup headers collection
		List<Header> defaultHeaders = new ArrayList<Header>();
		Header accept = new BasicHeader(HttpHeaders.ACCEPT, "application/json");
		defaultHeaders.add(accept);
		
		// Next create the HTTP Client, using the SSL Socket Factory in the build, alongside the default headers
		CloseableHttpClient httpClient = null;
		//log.debug("Attempting to create HTTP Client with SSL Factory, Creds, and Headers");
		try {
			httpClient = HttpClients.custom()
					.setSSLSocketFactory(sslConnectionFactory)
					.setDefaultHeaders(defaultHeaders)
					.build();
			log.debug("HTTP Client created : "+httpClient.getClass().getSimpleName());

		} catch (RuntimeException e) {
			throw new RuntimeException("Error during HTTP Client creation :: "+e);
		}

		// Next create the Client's Request Factory, which is passed to the new RestTemplate object.
		ClientHttpRequestFactory requestFactory = null;
		try {
			requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
	        log.debug("Request Factory : "+requestFactory.getClass().getSimpleName());
		} catch (RuntimeException e) {
			throw new RuntimeException("Error during Request Factory creation :: "+e);
		}

		// Create and return the customized RestTemplate.
		RestTemplate template = new RestTemplate(requestFactory);
		log.debug("Orchestrator RestTemplate initialized : "+template.toString());
		return template;
	}

	/**
	 * The vroObjectMapper bean is just an accessible ObjectMapper, used to convert POJOs into JSON Strings for API calls.
	 * It is initialized once at plugin startup and is available for any service to use.
	 * @return Object Mapper instance
	 */
	@Bean
	public ObjectMapper vroObjectMapper() {
		ObjectMapper om = new ObjectMapper();
		log.debug("ObjectMapper bean instance initialized.");
		return om;
	}

}
