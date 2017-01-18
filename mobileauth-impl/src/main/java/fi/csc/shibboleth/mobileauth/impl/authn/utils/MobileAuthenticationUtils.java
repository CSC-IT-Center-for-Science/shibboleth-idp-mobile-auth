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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * Supporting static methods for Mobile authentication
 * 
 * @author korteke
 *
 */
public class MobileAuthenticationUtils {

    private final static String NUM_PREFIX = "+358";
    private final static List<String> LANGS = Arrays.asList("fi", "sv", "en");
    private final static String DEFAULT_LANG = "fi";
    // starts with one "0", seven digits
    private static Pattern phoneNumberPattern = Pattern.compile("^0[1-9](?:[0-9] ?){5,13}$");
    private static Pattern internationalPhoneNumberPattern = Pattern.compile("^\\+(?:[0-9]){9,17}$");

    /**
     * Validate phoneNumber against regular expression
     * according to https://en.wikipedia.org/wiki/E.164
     *
     * @param number
     *            String phoneNumber that user has inputted to the form
     * @return true if phone number is valid
     */
    public static boolean validatePhoneNumber(String number) {
        Matcher phoneNumberMatcher = phoneNumberPattern.matcher(number);
        Matcher internationalPhoneNumberMatcher = internationalPhoneNumberPattern.matcher(number);

        if (phoneNumberMatcher.find() || internationalPhoneNumberMatcher.find()) {
            return true;
        }
        return false;
    }

    /**
     * Fixes users phoneNumber to the correct, international form
     * 
     * @param number
     *            String phoneNumber that user has inputted to the form
     * @return Fixed phoneNumber or null
     */
    public static String fixPhoneNumber(String number) {

        if (number.startsWith(NUM_PREFIX)) {
            return number.trim();

        } else if (number.startsWith("0")) {
            StringBuilder sb = new StringBuilder("");
            number = number.substring(1);
            sb.append(NUM_PREFIX);
            sb.append(number);
            return sb.toString().trim();
        }
        return null;
    }

    /**
     * Validates spam prevention code that user has provided.
     * This is a general sanity check: no whitespace, 1-30 word characters.
     * Further checking done by ETSI-provider.
     * 
     * @param spam
     *            String spam prevention code that user has inputted to the form
     * @return true if spam code is valid
     */
    public static boolean validateSpamPreventionCode(String spam) {
        String pattern = "^\\w{1,30}$";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(spam);

        if (m.find()) {
            return true;
        }
        return false;
    }

    /** 
     * Extracts language code from the AuthnRequest extension element
     * 
     * @param xmlElement AuthnRequest
     * @param ns XML Namespace where language code lives
     * @param tag XML element where language code lives
     * @return lang 
     */
    public static String unMarshallLanguage(Element xmlElement, String ns, String tag) {
        //TODO: Replace this with the "real" Marshalers & Unmarhalers

        // Defaulting to finnish language
        String lang = null;
        
        if (xmlElement.getElementsByTagNameNS(ns, tag).getLength() > 0) {

            lang = StringSupport.trimOrNull(xmlElement.getElementsByTagNameNS(ns, tag).item(0).getTextContent());
        }

        if (lang != null && LANGS.contains(lang)) {
            return lang;
        }
        return DEFAULT_LANG;
    }
    
}
