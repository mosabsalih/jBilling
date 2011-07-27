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

package com.sapienter.jbilling.server.util.api.validation;

import com.sapienter.jbilling.common.SessionInternalError;
import org.apache.log4j.Logger;
import org.springframework.aop.MethodBeforeAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 *
 * @author emilc
 */
public class APIValidator implements MethodBeforeAdvice {

    private static final Logger LOG = Logger.getLogger(APIValidator.class);
    
    private Validator validator;
    private Set<String> objectsToTest = null;

	public void setValidator(Validator validator) {
		this.validator = validator;
	}

    public Validator getValidator() {
		return validator;
	}

	public void setObjectsToTest(Set<String> objectsToTest) {
		this.objectsToTest = objectsToTest;
	}

	public Set<String> getObjectsToTest() {
		return objectsToTest;
	}

	public void before(Method method, Object[] args, Object target) throws Throwable {
		ArrayList<String> errors = new ArrayList<String>();
		
        for (Object arg: args) {
            if (arg != null) {

                String objectName = getObjectName(arg);
            	if (arg.getClass().isArray() && ((Object[])arg).length > 0) {
            		objectName= (((Object[]) arg) [0]).getClass().getName();
            	}

                boolean testThisObject = false;
                for (String test: objectsToTest) {
                    if (objectName.endsWith(test)) {
                        testThisObject = true;
                        break;
                    }
                }

                if (testThisObject) {
                	if (arg.getClass().isArray()) { 
                		Object[] objArr = (Object[]) arg;
                		for (Object o : objArr) {
                			errors.addAll(validateObject(method, objectName, o));
                		}
                		
                	} else {
                		errors.addAll(validateObject(method, objectName, arg));
                	}
                }
            }
        }
        
        if (!errors.isEmpty()) {
        	throw new SessionInternalError("Validation of '" + method.getName() + "()' arguments failed.",
                                           errors.toArray(new String[errors.size()]));
        }
    }

    private String getObjectName(Object object) {
        return object.getClass().getSimpleName();
    }


    private List<String> getErrorMessages(Set<ConstraintViolation<Object>> constraintViolations, String objectName) {
        List<String> errors = new ArrayList<String>(constraintViolations.size());

        if (!constraintViolations.isEmpty())
            for (ConstraintViolation<Object> violation: constraintViolations)
                errors.add(objectName + "," + violation.getPropertyPath().toString() + "," + violation.getMessage());

        return errors;
    }

    /**
     * Validates a method call argument, returning a list of error messages to be thrown
     * as part of a SessionInternalError.
     *
     * @param method method to validate
     * @param objectName object name of method argument to validate
     * @param arg method argument to validate
     * @return error messages
     */
	private List<String> validateObject(Method method, String objectName, Object arg) {
        // validate all common validations
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(arg);

        // validate "create" or "update" group validations
        if (method.getName().startsWith("create")) {
            constraintViolations.addAll(validator.validate(arg, CreateValidationGroup.class));
        } else if (method.getName().startsWith("update")) {
            constraintViolations.addAll(validator.validate(arg, UpdateValidationGroup.class));
        }

        // build error messages
        return getErrorMessages(constraintViolations, objectName);
	}

    /**
     * Run validations for the given object.
     *
     * If a group is specified then only the validations for the given group will be run.
     *
     * @param object object to validate
     * @param validationGroups groups to run
     * @throws SessionInternalError if validation failed
     */
    public void validateObject(Object object, Class... validationGroups) throws SessionInternalError{
       validateObjects(Arrays.asList(object), validationGroups);
    }

    /**
     * Validate all objects in the given list and throw a SessionInternalError if any
     * constraints have been violated.
     *
     * If a group is specified then only the validations for the given group will be run.
     *
     * @param objects objects to validate
     * @param validationGroups groups to run
     * @throws SessionInternalError if validation failed
     */
    public void validateObjects(List<Object> objects, Class... validationGroups) throws SessionInternalError {
        List<String> errors = new ArrayList<String>();

        for (Object object : objects) {
            // run validations
            Set<ConstraintViolation<Object>> constraintViolations;
            if (validationGroups != null && validationGroups.length > 0) {
                constraintViolations = getValidator().validate(object, validationGroups);
            } else {
                constraintViolations = getValidator().validate(object);
            }

            // build error messages
            String objectName = getObjectName(object);
            errors.addAll(getErrorMessages(constraintViolations, objectName));
        }

        // throw exception if error messages returned
        if (!errors.isEmpty()) {
            throw new SessionInternalError("Validations failed.", errors.toArray(new String[errors.size()]));
        }
    }
}
