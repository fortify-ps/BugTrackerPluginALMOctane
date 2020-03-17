/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.pub.bugtracker.plugin.alm.octane.client.http;

import java.io.Closeable;
import java.net.URL;
import java.util.Collections;

import javax.json.JsonObject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;

import com.fortify.pub.bugtracker.plugin.alm.octane.config.OctaneConfig;
import com.fortify.pub.bugtracker.plugin.proxy.ProxyConfig;
import com.fortify.pub.bugtracker.support.BugTrackerAuthenticationException;
import com.fortify.pub.bugtracker.support.BugTrackerException;
import com.fortify.pub.bugtracker.support.UserAuthenticationStore;

/**
 * This class provides low-level functionality for communicating with an ALM Octane instance
 * over HTTP(S). This includes the following functionality:
 * <ul>
 *  <li>Construct an Apache HTTP Client based JAX-RS client</li>
 *  <li>Handle ALM Octane authentication. At the moment only Basic Authentication is
 *      supported in order to minimize the number of requests (not having to request
 *      a new authentication cookie upon every bug tracker interaction). Note that
 *      Basic authentication must be explicitly enabled in ALM Octane.</li>
 *  <li>Basic functionality for constructing {@link WebTarget} instances based on
 *      the provided {@link OctaneConfig} configuration.</li>
 * </ul>
 */
public class OctaneHttpClient implements Closeable, AutoCloseable {
    private final Client client;
	private final OctaneConfig octaneConfig;

	/**
	 * Initialize this instance based on the given {@link OctaneConfig}, {@link UserAuthenticationStore}
	 * and {@link ProxyConfig}. 
	 * 
	 * @param octaneConfig
	 * @param authStore
	 * @param proxyConfig
	 */
    public OctaneHttpClient(OctaneConfig octaneConfig, UserAuthenticationStore authStore, ProxyConfig proxyConfig) {
    	Validate.notNull(octaneConfig, "Octane configuration must be specified");
    	Validate.notNull(authStore, "Octane credentials must be specified");
        this.octaneConfig = octaneConfig;
        this.client = createClient(proxyConfig, authStore);
    }

    /**
     * Close the underlying {@link Client}.
     */
    @Override
    public void close() {
        this.client.close();
    }

    /**
     * This method calls {@link #createClientConfig(ProxyConfig, UserAuthenticationStore)}
     * with the given {@link ProxyConfig} and {@link UserAuthenticationStore} to create
     * a {@link ClientConfig} instance, then uses this {@link ClientConfig} instance to
     * build a new {@link Client} instance.
     * 
     * @param proxyConfig
     * @param authStore
     * @return
     */
    private Client createClient(ProxyConfig proxyConfig, UserAuthenticationStore authStore) {
        ClientConfig clientConfig = createClientConfig(proxyConfig, authStore);
        return ClientBuilder.newClient(clientConfig);
    }

    /**
     * Create a {@link ClientConfig} instance based on the given {@link ProxyConfig} and
     * {@link UserAuthenticationStore}. This configures various HTTP client properties.
     * 
     * @param proxyConfig
     * @param authStore
     * @return
     */
	private ClientConfig createClientConfig(ProxyConfig proxyConfig, UserAuthenticationStore authStore) {
		PoolingHttpClientConnectionManager connMan = createConnectionManager();
        RequestConfig httpRequestConfig = createRequestConfig();

        ClientConfig clientConfig = new ClientConfig();
        clientConfig
        		.connectorProvider(new ApacheConnectorProvider())
                .property(ApacheClientProperties.CONNECTION_MANAGER, connMan)
                .property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED)
                        // ..disables chunking, hence sends Content-Length, hence enables basic authentication for some proxies (e.g. Squid)
                .property(ApacheClientProperties.PREEMPTIVE_BASIC_AUTHENTICATION, true)
                .property(ApacheClientProperties.CREDENTIALS_PROVIDER, createCredentialsProvider(authStore))
                .property(ApacheClientProperties.REQUEST_CONFIG, httpRequestConfig)
                ;
        
        setProxyConfiguration(clientConfig, proxyConfig);
		return clientConfig;
	}

	/**
	 * Create a {@link CredentialsProvider} instance for the given {@link UserAuthenticationStore}.
	 * 
	 * @param authStore
	 * @return
	 */
	private CredentialsProvider createCredentialsProvider(UserAuthenticationStore authStore) {
		CredentialsProvider result = new BasicCredentialsProvider();
        if (authStore != null) {
            result.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(authStore.getUserName(), authStore.getPassword()));
        }
        return result;
	}

	/**
	 * Update the given {@link ClientConfig} based on the given {@link ProxyConfig}. If the given
	 * {@link ProxyConfig} is null, this method has no effect.
	 * 
	 * @param clientConfig
	 * @param proxyConfig
	 */
	private void setProxyConfiguration(ClientConfig clientConfig, ProxyConfig proxyConfig) {
		if ( proxyConfig!=null ) {
        	clientConfig
        		.property(ClientProperties.PROXY_URI, new HttpHost(proxyConfig.getHost(), proxyConfig.getPort()))
        		.property(ClientProperties.PROXY_USERNAME, proxyConfig.getUserName())
        		.property(ClientProperties.PROXY_PASSWORD, proxyConfig.getPassword());
        	
        }
	}

	/**
	 * Create a {@link RequestConfig} instance to be used for HTTP requests to our
	 * target system.
	 * @return
	 */
	private RequestConfig createRequestConfig() {
		// Set up reasonable/acceptable values for request timeouts:
        RequestConfig httpRequestConfig = RequestConfig.custom()
                .setConnectTimeout(5 * 1000)    // Timeout for receiving a free connection from pooling connection manager.
                                                // As we are using a dedicated connection manager per API call
                                                //   there should be always free connections available.
                .setConnectionRequestTimeout(5 * 1000)  // Timeout for waiting for an answer to the http(s) request.
                .setSocketTimeout(10 * 1000)    // Timeout for waiting for data to be sent back from target.
                .setProxyPreferredAuthSchemes(Collections.singletonList(AuthSchemes.BASIC))
                .setTargetPreferredAuthSchemes(Collections.singletonList(AuthSchemes.BASIC))
                .build();
		return httpRequestConfig;
	}

	/**
	 * Create a {@link PoolingHttpClientConnectionManager} instance to be used for
	 * handling HTTP connections to our target system. 
	 * @return
	 */
	private PoolingHttpClientConnectionManager createConnectionManager() {
		// Set up connection manager for ApacheConnectionProvider:
        PoolingHttpClientConnectionManager connMan = new PoolingHttpClientConnectionManager();
        connMan.setMaxTotal(5);// Max total connections maintained by pooling manager
                                // Could be probably even less then 5 but let's stay at the safe side
        connMan.setDefaultMaxPerRoute(5); // In our case route is defined by Octane host URL and optional proxy
                                          // and for one API call remains persistent
		return connMan;
	}

	/**
	 * Make an HTTP request to the given target using the given HTTP method, and return the
	 * results as the given return type.
	 * 
	 * @param method
	 * @param target
	 * @param returnType
	 * @return
	 */
    public <T> T httpRequest(String method, WebTarget target, Class<T> returnType) {
        Invocation invocation = target.request(MediaType.APPLICATION_JSON)
            .header("Connection","keep-alive")
            .header("ALM_OCTANE_TECH_PREVIEW", "true") // Required for basic authentication
            .build(method);

        return invoke(invocation, returnType);
    }
    
    /**
     * Make an HTTP request to the given target using the given HTTP method, passing the
     * given JSON data as the request body, and return the results as the given return type.
	 * 
     * @param method
     * @param target
     * @param data
     * @param returnType
     * @return
     */
    public <T> T httpRequest(String method, WebTarget target, JsonObject data, Class<T> returnType){
        Invocation invocation = target.request(MediaType.APPLICATION_JSON)
        		.header("ALM_OCTANE_TECH_PREVIEW", "true") // Required for basic authentication
        		.build(method, Entity.json(data));
        
        return invoke(invocation, returnType);
    }

    /**
     * Invoke the given {@link Invocation} and return the results as the given return type.
     * The main objective of this method is to handle potential exceptions thrown by 
     * {@link Invocation#invoke(Class)}.
     * 
     * @param invocation
     * @param returnType
     * @return
     */
	private <T> T invoke(Invocation invocation, Class<T> returnType) {
		try {
            return invocation.invoke(returnType);
        } catch (WebApplicationException e) {
            int status = e.getResponse().getStatus();
            String contents = e.getResponse().readEntity(String.class);
            switch (status) {
                case HttpStatus.SC_UNAUTHORIZED:
                    throw new BugTrackerAuthenticationException("Octane authentication credentials are invalid:\n"+contents);
                case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
                    throw new BugTrackerAuthenticationException("Http(s) proxy authentication credentials are invalid:\n"+contents);
                default:
                    throw new BugTrackerException(String.format(
                            "Unsuccessful response %d returned from Octane (2xx is expected) with message: %s\nContents:\n%s"
                            , status, e.getMessage(), contents));
            }
        } catch (ResponseProcessingException e) {
            e.getResponse().close();
            throw new BugTrackerException("Failure while processing response received from Octane", e);
        } catch (Exception e) {
            throw new BugTrackerException("Unexpected error when requesting Octane", e);
        }
	}
    
	/**
	 * Get the {@link OctaneConfig} instance that was passed to our constructor.
	 * @return
	 */
    public OctaneConfig getOctaneConfig() {
		return octaneConfig;
	}

    /**
     * Get the base {@link WebTarget} as specified in the {@link OctaneConfig}
     * that was passed to our constructor.
     * @return
     */
	public WebTarget getBaseTarget() {
    	return client.target(getBaseUrlAsString());
    }
    
	/**
     * Get the base {@link WebTarget} for making REST API requests to the
     * shared space and workspace as specified in our {@link OctaneConfig}. 
     * @return
     */
    public WebTarget getApiWorkspaceTarget() {
    	return getBaseTarget()
    				.path("/api/shared_spaces/")
    				.path(getOctaneConfig().getSharedSpaceId())
    				.path("/workspaces/")
    				.path(getOctaneConfig().getWorkspaceId());
    }

    /**
     * Get the base URL as specified in the {@link OctaneConfig}
     * that was passed to our constructor, and return this URL as
     * a {@link String}.
     * @return
     */
	public final String getBaseUrlAsString() {
		return getBaseUrl().toString();
	}

	/**
     * Get the base URL as specified in the {@link OctaneConfig}
     * that was passed to our constructor.
     * @return
     */
	public final URL getBaseUrl() {
		return getOctaneConfig().getBaseUrl();
	}
}
