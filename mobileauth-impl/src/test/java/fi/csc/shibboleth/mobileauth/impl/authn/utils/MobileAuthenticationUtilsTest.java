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

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MobileAuthenticationUtilsTest {

	private String validNumberMinimumLength = "0502000";
	private String invalidNumberTooShort = "050200";
	private String validNumberMaxLength = "050123456789012";
	private String invalidNumberTooLong = "0501234567890123";

	// same length range in international format
	private String internationalNumberMinimumLength = "+358502000";
	private String internationalNumberTooShort = "+35850200";
	private String internationalNumberMaximumLength = "+35850123456789012";
	private String internationalNumberTooLong = "+358501234567890123";
	private String originalNumber = "040123123";

	@Test
	public void validNumberShortTest() {
		assertTrue(MobileAuthenticationUtils.validatePhoneNumber(validNumberMinimumLength));
	}

	@Test
	public void invalidNumberTooShortTest() {
		assertFalse(MobileAuthenticationUtils.validatePhoneNumber(invalidNumberTooShort));
	}

	@Test
	public void validNumberLongTest() {
		assertTrue(MobileAuthenticationUtils.validatePhoneNumber(validNumberMaxLength));
	}

	@Test
	public void invalidNumberTooLongTest() {
		assertFalse(MobileAuthenticationUtils.validatePhoneNumber(invalidNumberTooLong));
	}

	@Test
	public void validInternationalFormShortNumberTest() {
		assertTrue(MobileAuthenticationUtils.validatePhoneNumber(internationalNumberMinimumLength));
	}

	@Test
	public void invalidInternationalFormTooShortNumberTest() {
		assertFalse(MobileAuthenticationUtils.validatePhoneNumber(internationalNumberTooShort));
	}

	@Test
	public void validInternationalFormLongNumberTest() {
		assertTrue(MobileAuthenticationUtils.validatePhoneNumber(internationalNumberMaximumLength));
	}

	@Test
	public void invalidInternationalFormTooLongNumberTest() {
		assertFalse(MobileAuthenticationUtils.validatePhoneNumber(internationalNumberTooLong));
	}

	@Test
	public void fixPhoneNumberTest() {
		assertEquals(MobileAuthenticationUtils.fixPhoneNumber(originalNumber), "+35840123123");

	}

}
