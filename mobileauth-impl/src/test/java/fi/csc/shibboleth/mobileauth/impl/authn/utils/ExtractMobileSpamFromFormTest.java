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

import org.opensaml.profile.action.EventIds;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.Event;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import fi.csc.shibboleth.mobileauth.api.authn.context.MobileContext;
import fi.csc.shibboleth.mobileauth.impl.authn.ExtractMobileSpamFromForm;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.impl.PopulateAuthenticationContextTest;
import net.shibboleth.idp.profile.ActionTestingSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

public class ExtractMobileSpamFromFormTest extends PopulateAuthenticationContextTest {

    /** The action to be tested. */
    private ExtractMobileSpamFromForm action;

    /** The Mobile number attribute. */
    private String expectedMobile;

    /** The Spam code attribute. */
    private String expectedSpamCode;

    /** The invalid Mobile number attribute. */
    private String invalidMobile;

    /** Invalid spam code attribute with whitespace. */
    private String invalidSpamCodeWithWhiteSpace;

    /** Invalid spam code attribute that is too long. */
    private String invalidSpamCodeTooLong;

    /** The Mobile number field. */
    private String mobileNumberField;

    /** The Spam code field. */
    private String spamCodeField;

    /** {@inheritDoc} */
    @BeforeMethod
    public void setUp() throws Exception {
        super.setUp();
        action = new ExtractMobileSpamFromForm();
        action.setMobileNumberField(mobileNumberField);
        action.setSpamCodeField(spamCodeField);
    }

    /**
     * Initializes the expected context variables.
     */
    @BeforeTest
    public void initTest() {
        mobileNumberField = "j_mobileNumber";
        spamCodeField = "j_spamcode";
        expectedMobile = "+35840123456";
        expectedSpamCode = "A1234";
        invalidMobile = "1231";
        invalidSpamCodeWithWhiteSpace = "12 34";
        invalidSpamCodeTooLong = "0123456789012345678901234567890123";

    }

    @Test
    public void testNoServlet() throws ComponentInitializationException {
        action.initialize();
        final Event event = action.execute(src);
        ActionTestingSupport.assertEvent(event, EventIds.INVALID_PROFILE_CTX);
    }

    @Test
    public void testSuccess() throws ComponentInitializationException {
        action.setHttpServletRequest(new MockHttpServletRequest());
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter(mobileNumberField, expectedMobile);
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter(spamCodeField, expectedSpamCode);
        action.initialize();
        prc.getSubcontext(AuthenticationContext.class, false).setAttemptedFlow(authenticationFlows.get(0));
        final AuthenticationContext authCtx = prc.getSubcontext(AuthenticationContext.class, false);
        final Event event = action.execute(src);
        Assert.assertNull(event);
        final MobileContext mobCtx = authCtx.getSubcontext(MobileContext.class, false);
        Assert.assertEquals(mobCtx.getMobileNumber(), expectedMobile);
        Assert.assertEquals(mobCtx.getSpamCode(), expectedSpamCode);

    }

    @Test
    public void testFail() throws ComponentInitializationException {
        action.setHttpServletRequest(new MockHttpServletRequest());
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter(mobileNumberField, invalidMobile);
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter(spamCodeField, invalidSpamCodeWithWhiteSpace);
        action.initialize();
        prc.getSubcontext(AuthenticationContext.class, false).setAttemptedFlow(authenticationFlows.get(0));
        final Event event = action.execute(src);
        Assert.assertEquals(AuthnEventIds.INVALID_CREDENTIALS,event.toString());
    }

    @Test
    public void testSpamCodeWithWhiteSpace() throws ComponentInitializationException {
        action.setHttpServletRequest(new MockHttpServletRequest());
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter(mobileNumberField, expectedMobile);
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter(spamCodeField, invalidSpamCodeWithWhiteSpace);
        action.initialize();
        prc.getSubcontext(AuthenticationContext.class, false).setAttemptedFlow(authenticationFlows.get(0));
        final Event event = action.execute(src);
        Assert.assertEquals(AuthnEventIds.INVALID_CREDENTIALS,event.toString());
    }

    @Test
    public void testSpamCodeTooLong() throws ComponentInitializationException {
        action.setHttpServletRequest(new MockHttpServletRequest());
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter(mobileNumberField, expectedMobile);
        ((MockHttpServletRequest) action.getHttpServletRequest()).addParameter(spamCodeField, invalidSpamCodeTooLong);
        action.initialize();
        prc.getSubcontext(AuthenticationContext.class, false).setAttemptedFlow(authenticationFlows.get(0));
        final Event event = action.execute(src);
        Assert.assertEquals(AuthnEventIds.INVALID_CREDENTIALS,event.toString());
    }

}
