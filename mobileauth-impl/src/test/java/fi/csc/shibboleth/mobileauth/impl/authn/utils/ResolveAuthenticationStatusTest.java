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

package fi.csc.shibboleth.mobileauth.impl.authn.utils;

import javax.annotation.Nonnull;

import org.springframework.webflow.execution.Event;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import fi.csc.shibboleth.mobileauth.impl.authn.ResolveAuthenticationStatus;
import net.shibboleth.idp.authn.impl.PopulateAuthenticationContextTest;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

public class ResolveAuthenticationStatusTest extends PopulateAuthenticationContextTest {

    /** The action to be tested. */
    private ResolveAuthenticationStatus action;
    
    @Nonnull
    private String authServer;

    @Nonnull
    private int authPort;

    @Nonnull
    private String statusPath;

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

    /** {@inheritDoc} */
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        action = new ResolveAuthenticationStatus();
        action.setauthServer(authServer);
        action.setauthPort(authPort);
        action.setstatusPath(statusPath);
        action.setkeystoreType(keystoreType);
        action.setkeystorePath(keystorePath);
        action.setkeystorePasswd(keystorePasswd);
        action.setkeyPasswd(keyPasswd);
        action.setkeyAlias(keyAlias);
        action.settrustStorePath(trustStorePath);
        action.setkeystoreType(trustStoreType);
        action.settrustStorePassword(trustStorePassword);
        
    }

    /**
     * Initialize variables.
     */
    @BeforeTest
    public void initTest() {
        authServer = "http://localhost";
        authPort = 8443;
        statusPath = "/status";
        keystoreType = "JKS";
        keystorePath = "/opt/blop.jks";
        keystorePasswd = "password";
        keyPasswd = "password";
        keyAlias = "privateKey";
        trustStorePath = "/opt/blop.jks";
        trustStoreType = "JKS";
        trustStorePassword = "password";
        
    }

    @Test
    public void testNoContext() throws ComponentInitializationException {
        action.initialize();
        Event result = action.execute(src);
        //ActionTestingSupport.assertEvent(result, EventIds.INVALID_PROFILE_CTX);
    }

}
