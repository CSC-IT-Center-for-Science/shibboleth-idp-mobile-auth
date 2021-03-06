/*
 * The MIT License
 * Copyright (c) 2016 CSC - IT Center for Science, http://www.csc.fi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.csc.shibboleth.mobileauth.impl.authn;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fi.csc.shibboleth.mobileauth.api.authn.StatusResponse;
import fi.csc.shibboleth.mobileauth.api.authn.context.MobileContext;
import fi.csc.shibboleth.mobileauth.api.authn.context.MobileContext.ProcessState;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.idp.profile.ActionSupport;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * This action will send authentication request to the backend service
 * 
 * @author korteke
 *
 */
@SuppressWarnings("rawtypes")
public class AuthenticateMobile extends AbstractProfileAction {
	
	public static final String REST_PARAM_MOBILENUMBER = "mobileNumber";
	
	public static final String REST_PARAM_NO_SPAM_CODE = "noSpamCode";
    
    /** Class logger. */
    @Nonnull
    private final static Logger log = LoggerFactory.getLogger(AuthenticateMobile.class);

    @Nonnull
    private String authServer;

    @Nonnull
    private int authPort;

    @Nonnull
    private String authPath;

    @Nonnull
    private String keystoreType;

    @Nonnull
    private String keystorePath;

    @Nonnull
    private String keystorePasswd;

    @Nonnull
    private String keyPasswd;

    @Nonnull
    private String keyAlias;

    @Nonnull
    private String trustStorePath;

    @Nonnull
    private String trustStoreType;

    @Nonnull
    private String trustStorePassword;

    /* TODO: Create just one constructor to initialize these variables.. */
    public void setAuthServer(@Nonnull @NotEmpty final String fieldName) {
        log.debug("{} Adding authServer to {}", getLogPrefix(), fieldName);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        authServer = Constraint.isNotNull(StringSupport.trimOrNull(fieldName),
                "authServer field name cannot be null or empty.");
    }

    public void setAuthPort(@Nonnull @NotEmpty final int fieldName) {
        log.debug("{} Adding authPort to {}", getLogPrefix(), fieldName);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        authPort = fieldName;
    }

    public void setAuthPath(@Nonnull @NotEmpty final String fieldName) {
        log.debug("{} Adding authPath to {}", getLogPrefix(), fieldName);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        authPath = Constraint.isNotNull(StringSupport.trimOrNull(fieldName),
                "authPath field name cannot be null or empty.");
    }

    public void setKeystoreType(@Nonnull @NotEmpty final String fieldName) {
        log.debug("{} Adding keystoreType to {}", getLogPrefix(), fieldName);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        keystoreType = Constraint.isNotNull(StringSupport.trimOrNull(fieldName),
                "keystoreType field name cannot be null or empty.");
    }

    public void setKeystorePath(@Nonnull @NotEmpty final String fieldName) {
        log.debug("{} Adding keystorePath to {}", getLogPrefix(), fieldName);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        keystorePath = Constraint.isNotNull(StringSupport.trimOrNull(fieldName),
                "keystorePath field name cannot be null or empty.");
    }

    public void setKeystorePasswd(@Nonnull @NotEmpty final String fieldName) {
        log.debug("{} Adding keystorePasswd to {}", getLogPrefix(), fieldName);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        keystorePasswd = Constraint.isNotNull(StringSupport.trimOrNull(fieldName),
                "keystorePasswd field name cannot be null or empty.");
    }

    public void setKeyPasswd(@Nonnull @NotEmpty final String fieldName) {
        log.debug("{} Adding keyPasswd to {}", getLogPrefix(), fieldName);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        keyPasswd = Constraint.isNotNull(StringSupport.trimOrNull(fieldName),
                "keyPasswd field name cannot be null or empty.");
    }

    public void setKeyAlias(@Nonnull @NotEmpty final String fieldName) {
        log.debug("{} Adding keyAlias to {}", getLogPrefix(), fieldName);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        keyAlias = Constraint.isNotNull(StringSupport.trimOrNull(fieldName),
                "keyAlias field name cannot be null or empty.");
    }

    public void setTrustStorePath(@Nonnull @NotEmpty final String fieldName) {
        log.debug("{} Adding trustStorePath to {}", getLogPrefix(), fieldName);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        trustStorePath = Constraint.isNotNull(StringSupport.trimOrNull(fieldName),
                "trustStorePath field name cannot be null or empty.");
    }

    public void setTrustStoreType(@Nonnull @NotEmpty final String fieldName) {
        log.debug("{} Adding trustStoreType to {}", getLogPrefix(), fieldName);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        trustStoreType = Constraint.isNotNull(StringSupport.trimOrNull(fieldName),
                "trustStoreType field name cannot be null or empty.");
    }

    public void setTrustStorePassword(@Nonnull @NotEmpty final String fieldName) {
        log.debug("{} Adding trustStorePassword to {}", getLogPrefix(), fieldName);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        trustStorePassword = Constraint.isNotNull(StringSupport.trimOrNull(fieldName),
                "trustStorePassword field name cannot be null or empty.");
    }

    /** {@inheritDoc} */
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
            @Nonnull final AuthenticationContext authenticationContext) {

        log.debug("Entering doPreExecute - AuthenticateMobile");

        if (authenticationContext.getAttemptedFlow() == null) {
            log.error("{} No attempted flow within authentication context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }

        if (authenticationContext.getSubcontext(MobileContext.class, false) == null) {
            log.error("{} No mobile context in authentication context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    protected Event doExecute(@Nonnull final RequestContext springRequestContext,
            @Nonnull final ProfileRequestContext profileRequestContext) {
        log.debug("{} Entering - doExecute", getLogPrefix());

        final MobileContext mobCtx = profileRequestContext.getSubcontext(AuthenticationContext.class)
                .getSubcontext(MobileContext.class);
        final String mobileNumber = StringSupport.trimOrNull(mobCtx.getMobileNumber());
        final String spamCode = StringSupport.trimOrNull(mobCtx.getSpamCode());
        
        final CloseableHttpClient httpClient;
        try {
            log.debug("{} Trying to create httpClient", getLogPrefix());
            httpClient = createHttpClient();
        } catch (KeyStoreException | RuntimeException e) {
            log.error("{} Cannot create httpClient", getLogPrefix(), e);
            return Events.failure.event(this);
        }

        // TODO: entity initialization
        HttpEntity entity = null;
        try {
            final URIBuilder builder = new URIBuilder();
            // TODO: remove hard-codings
            builder.setScheme("https").setHost(authServer).setPort(authPort).setPath(authPath)
                    .setParameter(REST_PARAM_MOBILENUMBER, mobileNumber);
            if (spamCode != null) {
                builder.setParameter(REST_PARAM_NO_SPAM_CODE, spamCode);
            }

            final URI url = builder.build();

            final HttpGet httpGet = new HttpGet(url);
            final Gson gson = new GsonBuilder().create();

            final CloseableHttpResponse response = httpClient.execute(httpGet);
            log.debug("{} Response: {}", getLogPrefix(), response);

            int statusCode = response.getStatusLine().getStatusCode();
            log.debug("{} HTTPStatusCode {}", getLogPrefix(), statusCode);
            
            entity = response.getEntity();

            final String json = EntityUtils.toString(entity, "UTF-8");

            final StatusResponse status = gson.fromJson(json, StatusResponse.class);
            
            if (status.getErrorMessage() != null) {
                log.debug("{} Setting error message", getLogPrefix());
                mobCtx.setErrorMessage(status.getErrorMessage());
            }
            
            response.close();

            if (statusCode == HttpStatus.SC_OK) {

                log.debug("{} Gson commKey: {}", getLogPrefix(), status.getCommunicationDataKey());
                log.debug("{} Gson EventID: {}", getLogPrefix(), status.getEventId());
                log.debug("{} Gson ErrorMessage: {}", getLogPrefix(), status.getErrorMessage());


                mobCtx.setProcessState(ProcessState.IN_PROCESS);
                mobCtx.setConversationKey(status.getCommunicationDataKey());
                mobCtx.setEventId(status.getEventId());
                mobCtx.setErrorMessage(status.getErrorMessage());
            } else if (statusCode == HttpStatus.SC_METHOD_NOT_ALLOWED) {
                mobCtx.setProcessState(ProcessState.ERROR);
                // TODO: multilingual error message
                log.warn("{} 405 - Status code {} from REST gateway", getLogPrefix(), statusCode);
                return Events.failure.event(this);
            } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                mobCtx.setProcessState(ProcessState.ERROR);
                // TODO: multilingual error message
                log.error("{} 401 - Status code {} from REST gateway - Invalid client configuration?"
                        , getLogPrefix(),statusCode);
                return Events.failure.event(this);
            } else {
                mobCtx.setProcessState(ProcessState.ERROR);
                // TODO: multilingual error message
                log.warn("{} Status code {} from REST gateway", getLogPrefix(), statusCode);
                return Events.gatewayError.event(this);
            }

        } catch (Exception e) {
            // TODO: better exception handling
            log.error("Exception: {}", e);
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.AUTHN_EXCEPTION);
            return Events.failure.event(this);
        } finally {
            EntityUtils.consumeQuietly(entity);
        }

        return Events.success.event(this);
    }

    /**
     * This method will create CloseableHttpClient with client certificate authentication
     * 
     * @return CloseableHttpClient
     * @throws KeyStoreException
     * @throws RuntimeException
     */
    private CloseableHttpClient createHttpClient() throws KeyStoreException, RuntimeException {

        KeyStore trustStore = KeyStore.getInstance(trustStoreType);
        KeyStore clientStore = KeyStore.getInstance(keystoreType);

        try {
            trustStore.load(new FileInputStream(trustStorePath), trustStorePassword.toCharArray());
            clientStore.load(new FileInputStream(keystorePath), keystorePasswd.toCharArray());
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new RuntimeException("Cannot load key/trust stores", e);
        }

        final SSLConnectionSocketFactory socketFactory;

        try {
            final SSLContext sslContext = SSLContexts.custom().useTLS()
                    .loadKeyMaterial(clientStore, keystorePasswd.toCharArray()).loadTrustMaterial(trustStore).build();
            socketFactory = new SSLConnectionSocketFactory(sslContext,
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            throw new RuntimeException("SSL initialization error", e);
        }
        final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("https", socketFactory).build();
        final BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(registry);
        log.debug("Created httpClient");
        return HttpClients.custom().setConnectionManager(connectionManager).build();
    }

}
