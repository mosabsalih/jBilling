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
package com.sapienter.jbilling.server;

import junit.framework.TestCase;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Brian Cowdery
 * @since 29-04-2010
 */
public class BigDecimalTestCase extends TestCase { // todo: move base test case so it's available to all testing suites

    public static final Integer COMPARISON_SCALE = 2;
    public static final RoundingMode COMPARISON_ROUNDING_MODE = RoundingMode.HALF_UP;

    public BigDecimalTestCase() {
    }

    public BigDecimalTestCase(String name) {
        super(name);
    }

    /**
     * Asserts that 2 given BigDecimal numbers are equivalent to 2 decimal places.
     * 
     * @param expected expected BigDecimal value
     * @param actual actual BigDecimal value
     */
    public static void assertEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * @see #assertEquals(java.math.BigDecimal, java.math.BigDecimal)
     *
     * @param message error message if assert fails
     * @param expected expected BigDecimal value
     * @param actual actual BigDecimal value
     */
    public static void assertEquals(String message, BigDecimal expected, BigDecimal actual) {
        assertEquals(message,
                     (Object) (expected == null ? null : expected.setScale(COMPARISON_SCALE, COMPARISON_ROUNDING_MODE)),
                     (Object) (actual == null ? null : actual.setScale(COMPARISON_SCALE, COMPARISON_ROUNDING_MODE)));
    }
}
