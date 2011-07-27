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

package com.sapienter.jbilling.server.user.validator;

import org.hibernate.validator.constraints.impl.LuhnValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.Serializable;

/**
 * CreditCardNumberValidator
 *
 * @author Brian Cowdery
 * @since 07/06/11
 */
public class CreditCardNumberValidator implements ConstraintValidator<CreditCardNumber, String>, Serializable {

    private static final long serialVersionUID = 1L;

    private LuhnValidator luhnValidator;

    public CreditCardNumberValidator() {
        this.luhnValidator = new LuhnValidator(2);
    }

    public void initialize(CreditCardNumber annotation) {
    }

    public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
        if (value == null || value.startsWith("*")) {
            return true;
        }

        return luhnValidator.passesLuhnTest(value);
    }
}
