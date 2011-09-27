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
package com.sapienter.jbilling.server.order.db;

import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.db.AbstractDAS;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.joda.time.DateMidnight;

import java.util.Collections;
import java.util.List;

public class OrderLineDAS extends AbstractDAS<OrderLineDTO> {
    private static final Logger LOG = Logger.getLogger(OrderLineDAS.class);

    public Long findLinesWithDecimals(Integer itemId) {

        final String hql =
                "select count(*)" +
                        "  from OrderLineDTO ol " +
                        " where ol.deleted = 0 " +
                        "   and ol.item.id= :item and (ol.quantity - cast(ol.quantity as integer)) <> 0";

        Query query = getSession().createQuery(hql);
        query.setParameter("item", itemId);

        return (Long) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<OrderLineDTO> findByUserItem(Integer userId, Integer itemId) {
        final String hql =
                "select ol" +
                        "  from OrderLineDTO ol " +
                        " where ol.deleted = 0 " +
                        "   and ol.item.id = :item " +
                        "   and ol.purchaseOrder.baseUserByUserId.id = :user";

        Query query = getSession().createQuery(hql);
        query.setParameter("item", itemId);
        query.setParameter("user", userId);

        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<OrderLineDTO> findRecurringByUser(Integer userId) {
        final String hql =
                "select line "
                        + "  from OrderLineDTO line "
                        + "where line.deleted = 0 "
                        + "  and line.purchaseOrder.baseUserByUserId.id = :userId "
                        + "  and line.purchaseOrder.orderPeriod.id != :period "
                        + "  and line.purchaseOrder.orderStatus.id = :status "
                        + "  and line.purchaseOrder.deleted = 0 ";

        Query query = getSession().createQuery(hql);
        query.setParameter("userId", userId);
        query.setParameter("period", Constants.ORDER_PERIOD_ONCE);
        query.setParameter("status", Constants.ORDER_STATUS_ACTIVE);

        return query.list();
    }

    /**
     * Returns the first recurring order line found for the given user and item ID.
     *
     * @param userId user id
     * @param itemId item id
     * @return first recurring order line found, null if none found
     */
    @SuppressWarnings("unchecked")
    public OrderLineDTO findRecurringByUserItem(Integer userId, Integer itemId) {
        final String hql =
                "select line "
                        + "  from OrderLineDTO line "
                        + "where line.deleted = 0 "
                        + "  and line.item.id = :itemId "
                        + "  and line.purchaseOrder.baseUserByUserId.id = :userId "
                        + "  and line.purchaseOrder.orderPeriod.id != :period "
                        + "  and line.purchaseOrder.orderStatus.id = :status "
                        + "  and line.purchaseOrder.deleted = 0 ";

        Query query = getSession().createQuery(hql);
        query.setParameter("itemId", itemId);
        query.setParameter("userId", userId);
        query.setParameter("period", Constants.ORDER_PERIOD_ONCE);
        query.setParameter("status", Constants.ORDER_STATUS_ACTIVE);

        List<OrderLineDTO> results = query.list();
        return results.isEmpty() ? null : results.get(0);
    }

    @SuppressWarnings("unchecked")
    public List<OrderLineDTO> findOnetimeByUserItem(Integer userId, Integer itemId) {
        final String hql =
                "select line "
                        + "  from OrderLineDTO line "
                        + "where line.deleted = 0 "
                        + "  and line.item.id = :itemId "
                        + "  and line.purchaseOrder.baseUserByUserId.id = :userId "
                        + "  and line.purchaseOrder.orderPeriod.id = :period "
                        + "  and line.purchaseOrder.orderStatus.id = :status "
                        + "  and line.purchaseOrder.deleted = 0 ";

        Query query = getSession().createQuery(hql);
        query.setParameter("itemId", itemId);
        query.setParameter("userId", userId);
        query.setParameter("period", Constants.ORDER_PERIOD_ONCE);
        query.setParameter("status", Constants.ORDER_STATUS_ACTIVE);

        return query.list();
    }

    /**
     * Returns a list of all active and finished one time orders going back n number
     * of months, containing the given item id for the given user.
     *
     * @param userId user id of orders
     * @param itemId item id of order lines
     * @param months previous number of months to include (1 = this month plus the previous)
     * @return list of found one-time orders, empty list if none found
     */
    @SuppressWarnings("unchecked")
    public List<OrderLineDTO> findOnetimeByUserItem(Integer userId, Integer itemId, Integer months) {
        final String hql =
                "select line "
                        + "  from OrderLineDTO line "
                        + "where line.deleted = 0 "
                        + "  and line.item.id = :itemId "
                        + "  and line.purchaseOrder.baseUserByUserId.id = :userId "
                        + "  and line.purchaseOrder.orderPeriod.id = :period "
                        + "  and (line.purchaseOrder.orderStatus.id = :active_status"
                        + "       or line.purchaseOrder.orderStatus.id = :finished_status)"
                        + "  and line.purchaseOrder.deleted = 0 "
                        + "  and line.purchaseOrder.createDate > :startdate";

        Query query = getSession().createQuery(hql);
        query.setParameter("itemId", itemId);
        query.setParameter("userId", userId);
        query.setParameter("period", Constants.ORDER_PERIOD_ONCE);
        query.setParameter("active_status", Constants.ORDER_STATUS_ACTIVE);
        query.setParameter("finished_status", Constants.ORDER_STATUS_FINISHED);

        DateMidnight startdate = new DateMidnight().minusMonths(months);
        query.setParameter("startdate", startdate.toDate());

        return query.list();
    }

    /**
     * Returns a list of all active and finished one-time orders going back n number
     * of months, for all direct immediate of the given parent user id. This is useful for
     * determining usage across all child users.
     *
     * @param parentUserId parent user id
     * @param itemId item id of order lines
     * @param months previous number of months to include (1 = 1 month period starting from today)
     * @return list of found one-time orders, empty list if none found
     */
    @SuppressWarnings("unchecked")
    public List<OrderLineDTO> findOnetimeByParentUserItem(Integer parentUserId, Integer itemId, Integer months) {
        UserDTO parent = new UserBL(parentUserId).getEntity();
        if (parent == null || parent.getCustomer() == null) {
            LOG.warn("Parent user " + parentUserId + " does not exist or is not a customer!");
            return Collections.emptyList();
        }

        final String hql =
                "select line "
                        + " from OrderLineDTO line "
                        + " where line.deleted = 0 "
                        + "  and line.item.id = :itemId "
                        + "  and line.purchaseOrder.baseUserByUserId.customer.parent.id = :parentId"
                        + "  and line.purchaseOrder.orderPeriod.id = :period "
                        + "  and (line.purchaseOrder.orderStatus.id = :active_status"
                        + "       or line.purchaseOrder.orderStatus.id = :finished_status)"
                        + "  and line.purchaseOrder.deleted = 0 "
                        + "  and line.purchaseOrder.createDate > :startdate ";

        Query query = getSession().createQuery(hql);
        query.setParameter("itemId", itemId);
        query.setParameter("parentId", parent.getCustomer().getId());
        query.setParameter("period", Constants.ORDER_PERIOD_ONCE);
        query.setParameter("active_status", Constants.ORDER_STATUS_ACTIVE);
        query.setParameter("finished_status", Constants.ORDER_STATUS_FINISHED);

        DateMidnight startdate = new DateMidnight().minusMonths(months);
        query.setParameter("startdate", startdate.toDate());

        return query.list();
    }



}
