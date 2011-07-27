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

package com.sapienter.jbilling.server.mediation;

import java.util.Comparator;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.item.PricingField;

public class GroupRecordComparator implements Comparator<Record> {

    public final Format format;
    
    public GroupRecordComparator(Format format) {
        this.format = format;
    }
    
    public int compare(Record r1, Record r2) {
        // handle the nulls
        if (r1 == null && r2 == null) {
            return 0;
        } else if (r1 == null) {
            return 1;
        } else if (r2 == null) {
            return -1;
        }
        
        // if there aren't any key fields, then assume that all the
        // records are different
        boolean atLeastOne = false;
        
        // so none are null
        for (FormatField field: format.getFields()) {
            if (field.getIsKey()) {
                String pField1 = null, pField2 = null;
                atLeastOne = true;
                // find this field in both records
                // optimize by having the fields as hashmaps, rather than vectors
                for (PricingField pfield: r1.getFields()) {
                    if (pfield.getName().equals(field.getName())) {
                        pField1 = pfield.getValue().toString();
                        break;
                    }
                }
                for (PricingField pfield: r2.getFields()) {
                    if (pfield.getName().equals(field.getName())) {
                        pField2 = pfield.getValue().toString();
                        break;
                    }
                }
                if (pField1 == null || pField2 == null) {
                    throw new SessionInternalError("Can not find field for comparison of:" + r1 
                            + " with " + r2);
                }
                
                if (!pField1.equals(pField2)) {
                    return pField1.compareTo(pField2);
                }
            }
        }
        return atLeastOne ? 0 : -1;
    }

}
