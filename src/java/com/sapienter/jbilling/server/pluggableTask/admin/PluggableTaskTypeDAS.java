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

package com.sapienter.jbilling.server.pluggableTask.admin;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;

import com.sapienter.jbilling.server.util.db.AbstractDAS;

public class PluggableTaskTypeDAS extends AbstractDAS<PluggableTaskTypeDTO> {
	
	private static final Logger LOG =  Logger.getLogger(PluggableTaskTypeDAS.class);
	
    private static final String findByCategorySQL =
	        "SELECT b " +
	        "  FROM PluggableTaskTypeDTO b " + 
	        " WHERE b.category.id = :category" +
	        " ORDER BY b.id";

	public List<PluggableTaskTypeDTO> findAllByCategory(Integer categoryId) {
		LOG.debug("finding types for category " + categoryId);
		Query query = getSession().createQuery(findByCategorySQL);
        query.setParameter("category", categoryId);
        query.setComment("PluggableTaskTypeDAS.findAllByCategory");
        List<PluggableTaskTypeDTO> ret = query.list();
		LOG.debug("found " + ret.size());

        return ret;
	}

}
