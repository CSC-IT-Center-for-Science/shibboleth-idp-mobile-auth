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

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.csc.shibboleth.mobileauth.api.authn.context.MobileContext;
import fi.csc.shibboleth.mobileauth.impl.authn.utils.MobileAuthenticationUtils;
import net.shibboleth.idp.authn.AbstractExtractionAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * An action that extracts a mobile number and possible spam prevention code
 * from an HTTP form, creates a {@link MobileUserContext}, and attaches it to
 * the {@link AuthenticationContext}.
 */
@SuppressWarnings("rawtypes")
public class ExtractMobileSpamFromForm extends AbstractExtractionAction {

    /** Class logger. */
    @Nonnull
    private final Logger log = LoggerFactory.getLogger(ExtractMobileSpamFromForm.class);

    /** Parameter name for mobileNumber. */
    @Nonnull
    @NotEmpty
    private String mobileNumberField;

    /** Parameter name for spamCode. */
    private String spamCodeField;

    /**
     * Inject mobilenumber to the local variable.
     *
     * @param fieldName
     *            the mobileNumberField variable name
     */
    public void setMobileNumberField(@Nonnull @NotEmpty final String fieldName) {
        log.debug("Injecting mobileNumber");
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        mobileNumberField = Constraint.isNotNull(StringSupport.trimOrNull(fieldName),
                "MobileNumber field name cannot be null or empty.");
    }

    /**
     * Inject spamcode to the local variable.
     *
     * @param fieldName
     *            the spamCodeField variable name
     */
    public void setSpamCodeField(@Nonnull @NotEmpty final String fieldName) {
        log.debug("Injecting SpamCode");
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        spamCodeField = Constraint.isNotNull(StringSupport.trimOrNull(fieldName),
                "Spamcode field name cannot be null or empty.");
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext,
                                   @Nonnull final AuthenticationContext authenticationContext) {

        if (!super.doPreExecute(profileRequestContext, authenticationContext)) {
            return false;
        }

        if (authenticationContext.getAttemptedFlow() == null) {
            log.error("{} No attempted flow within authentication context", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_PROFILE_CTX);
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
                             @Nonnull final AuthenticationContext authenticationContext) {
        log.debug("{} Entering {} - doExecute", getLogPrefix(), this.getClass());

        final HttpServletRequest request = getHttpServletRequest();

        if (request == null) {
            log.error("{} Empty httpRequest", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
            return;
        }

        final MobileContext mobCtx = authenticationContext.getSubcontext(MobileContext.class, true);
        mobCtx.setMobileNumber(null);
        mobCtx.setSpamCode(null);
        mobCtx.setConversationKey(null);

        final String mobileNumber = request.getParameter(StringSupport.trim(mobileNumberField));

        if (mobileNumber == null || !MobileAuthenticationUtils.validatePhoneNumber(mobileNumber)) {
            log.info("{} Empty mobileNumber in request or number is invalid - {}", getLogPrefix(), mobileNumber);
            ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_CREDENTIALS);
            mobCtx.setErrorMessage("InvalidPhoneNumber");
            return;
        }

        mobCtx.setMobileNumber(MobileAuthenticationUtils.fixPhoneNumber(mobileNumber));
        log.debug("{} Added mobileNumber to the context [{}]", getLogPrefix(), mobCtx.getMobileNumber());

        final String spamCode = request.getParameter(spamCodeField);
        if (StringSupport.trimOrNull(spamCode) == null) {
            log.debug("{} No spamCode in the request, which is fine", getLogPrefix());
        } else {
            if (MobileAuthenticationUtils.validateSpamPreventionCode(spamCode)) {
                log.debug("{} Added spamCode to the context [{}]", getLogPrefix(), spamCode);
                mobCtx.setSpamCode(spamCode);
            } else {
                log.info("{} SpamCode is invalid - contains whitespace or is too long", getLogPrefix());
                ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_CREDENTIALS);
                mobCtx.setErrorMessage("InvalidSpamCode");
                return;
            }

        }

    }

}
