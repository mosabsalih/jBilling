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

package com.sapienter.jbilling.server.process.db;

import com.sapienter.jbilling.server.util.db.AbstractDAS;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.criterion.Restrictions;

import java.math.BigDecimal;

/**
 * 
 * @author abimael
 *
 */
public class ProcessRunTotalPmDAS extends AbstractDAS<ProcessRunTotalPmDTO> {

    public ProcessRunTotalPmDTO create(BigDecimal total) {
        ProcessRunTotalPmDTO newEntity = new ProcessRunTotalPmDTO();
        newEntity.setTotal(total);
        return save(newEntity);
    }
    
     /**
     * Returns the locked row, since payment processing updates this in parallel
      *
     * @param methodId payment method id
     * @param total run total
     * @return locked process run total 
     */
    public ProcessRunTotalPmDTO getByMethod(Integer methodId, ProcessRunTotalDTO total) {
        Criteria criteria = getSession().createCriteria(ProcessRunTotalPmDTO.class)
                .createAlias("processRunTotal", "r")
                    .add(Restrictions.eq("r.id", total.getId()))
                .createAlias("paymentMethod", "c")
                    .add(Restrictions.eq("c.id", methodId))
                .setComment("ProcessRunTotalPmDAS.getByMethod");

        return (ProcessRunTotalPmDTO) criteria.uniqueResult();
    }
   

}
