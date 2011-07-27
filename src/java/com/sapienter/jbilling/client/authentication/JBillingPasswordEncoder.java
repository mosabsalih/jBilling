/*
 jBilling - The Enterprise Open Source Billing System
 Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde

 This file is part of jbilling.

 jbilling is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 jbilling is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sapienter.jbilling.client.authentication;

import com.sapienter.jbilling.common.Constants;
import com.sapienter.jbilling.common.JBCrypto;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.encoding.PasswordEncoder;

/**
 * Implementation of the Spring Security {@link PasswordEncoder} using jBilling's own
 * cryptology algorithm.
 *
 * @author Brian Cowdery
 * @since 07-10-2010
 */
public class JBillingPasswordEncoder implements PasswordEncoder {

    public JBillingPasswordEncoder() {
    }

    /**
     * Encodes a password using jBillings own cryptology algorithm. This implementation does
     * not support the use of a salt source. Given salt values will be ignored and will not
     * change the outcome of the encoded password.
     *
     * @param password password to encode
     * @param saltSource not supported
     * @return encoded password
     * @throws DataAccessException
     */
    public String encodePassword(String password, Object saltSource) throws DataAccessException {
        JBCrypto cipher = JBCrypto.getPasswordCrypto(Constants.TYPE_ROOT);
        return cipher.encrypt(password);
    }

    /**
     * Returns true if the 2 given encoded passwords match.
     *
     * @param encPass encoded password from stored user
     * @param rawPass plain-text password from authentication form
     * @param saltSource not supported
     * @return true if passwords match, false if not
     * @throws DataAccessException
     */
    public boolean isPasswordValid(String encPass, String rawPass, Object saltSource) throws DataAccessException {        
        return encPass.equals(encodePassword(rawPass, saltSource));
    }
}
