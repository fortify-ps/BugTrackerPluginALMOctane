/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
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
package com.fortify.pub.bugtracker.plugin.alm.octane.client;

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
public class OctaneHttpClient implements Closeable {
    private final Client client;
	private OctaneConfig octaneConfig;

    public OctaneHttpClient(OctaneConfig octaneConfig, UserAuthenticationStore authStore, ProxyConfig proxyConfig) {
    	Validate.notNull(octaneConfig, "Octane configuration must be specified");
    	Validate.notNull(authStore, "Octane credentials must be specified");
        this.octaneConfig = octaneConfig;
        this.client = createClient(proxyConfig, authStore);
    }

    @Override
    public void close() {
        this.client.close();
    }

    private Client createClient(ProxyConfig proxyConfig, UserAuthenticationStore authStore) {
        ClientConfig clientConfig = createClientConfig(proxyConfig, authStore);
        return ClientBuilder.newClient(clientConfig);
    }

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

	private CredentialsProvider createCredentialsProvider(UserAuthenticationStore authStore) {
		CredentialsProvider result = new BasicCredentialsProvider();
        if (authStore != null) {
            result.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(authStore.getUserName(), authStore.getPassword()));
        }
        return result;
	}

	private void setProxyConfiguration(ClientConfig clientConfig, ProxyConfig proxyConfig) {
		if ( proxyConfig!=null ) {
        	clientConfig
        		.property(ClientProperties.PROXY_URI, new HttpHost(proxyConfig.getHost(), proxyConfig.getPort()))
        		.property(ClientProperties.PROXY_USERNAME, proxyConfig.getUserName())
        		.property(ClientProperties.PROXY_PASSWORD, proxyConfig.getPassword());
        	
        }
	}

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

	private PoolingHttpClientConnectionManager createConnectionManager() {
		// Set up connection manager for ApacheConnectionProvider:
        PoolingHttpClientConnectionManager connMan = new PoolingHttpClientConnectionManager();
        connMan.setMaxTotal(5);// Max total connections maintained by pooling manager
                                // Could be probably even less then 5 but let's stay at the safe side
        connMan.setDefaultMaxPerRoute(5); // In our case route is defined by Octane host URL and optional proxy
                                          // and for one API call remains persistent
		return connMan;
	}

    public <T> T httpGetRequest(WebTarget webTarget, Class<T> returnType) {
        Invocation invocation = webTarget.request(MediaType.APPLICATION_JSON)
            .header("Connection","keep-alive")
            .header("ALM_OCTANE_TECH_PREVIEW", "true") // Required for basic authentication
            .buildGet();

        try {
            return invocation.invoke(returnType);
        } catch (WebApplicationException e) {
            int status = e.getResponse().getStatus();
            switch (status) {
                case HttpStatus.SC_UNAUTHORIZED:
                    throw new BugTrackerAuthenticationException("Octane authentication credentials are invalid");
                case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
                    throw new BugTrackerAuthenticationException("Http(s) proxy authentication credentials are invalid");
                default:
                    throw new BugTrackerException(String.format(
                            "Unsuccessful response %d returned from Octane (2xx is expected) with message: %s"
                            , status, e.getMessage()));
            }
        } catch (ResponseProcessingException e) {
            e.getResponse().close();
            throw new BugTrackerException("Failure while processing response received from Octane", e);
        } catch (Exception e) {
            throw new BugTrackerException("Unexpected error when requesting Octane", e);
        }
    }


    public <T> T httpPostRequest(WebTarget webTarget, JsonObject data, Class<T> returnType){
        return webTarget.request(MediaType.APPLICATION_JSON).post(Entity.json(data), returnType);
    }
    
    public WebTarget getBaseTarget() {
    	return client.target(getBaseUrlAsString());
    }
    
    public WebTarget getApiWorkspaceTarget() {
    	return getBaseTarget()
    				.path("/api/shared_spaces/")
    				.path(octaneConfig.getSharedSpaceId())
    				.path("/workspaces/")
    				.path(octaneConfig.getWorkspaceId());
    }

	public final String getBaseUrlAsString() {
		return getBaseUrl().toString();
	}

	public final URL getBaseUrl() {
		return octaneConfig.getBaseUrl();
	}
}
