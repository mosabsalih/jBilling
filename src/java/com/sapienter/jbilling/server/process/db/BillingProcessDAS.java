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

import java.util.Date;
import java.util.Iterator;

import com.sapienter.jbilling.server.util.Constants;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.sapienter.jbilling.server.user.UserDTOEx;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.util.db.AbstractDAS;

public class BillingProcessDAS extends AbstractDAS<BillingProcessDTO> {

    public BillingProcessDTO create(CompanyDTO entity, Date billingProcess,
            Integer periodId, Integer periodValue, Integer retries) {

        PeriodUnitDTO period = new PeriodUnitDAS().find(periodId);
        BillingProcessDTO dto = new BillingProcessDTO();
        dto.setEntity(entity);
        dto.setBillingDate(billingProcess);
        dto.setPeriodUnit(period);
        dto.setPeriodValue(periodValue);
        dto.setIsReview(0);
        dto.setRetriesToDo(retries);

        return save(dto);
    }

    public BillingProcessDTO findReview(Integer entityId) {
        Criteria criteria = getSession().createCriteria(BillingProcessDTO.class);
        criteria.createAlias("entity", "ent").add(Restrictions.eq("ent.id", entityId));
        criteria.add(Restrictions.eq("isReview", 1));

        return (BillingProcessDTO) criteria.uniqueResult();
    }

    public BillingProcessDTO isPresent(Integer entityId, Integer isReview, Date billingDate) {
        Criteria criteria = getSession().createCriteria(BillingProcessDTO.class);
        criteria.createAlias("entity", "ent").add(Restrictions.eq("ent.id", entityId));
        criteria.add(Restrictions.eq("isReview", isReview));
        criteria.add(Restrictions.eq("billingDate", billingDate));

        return (BillingProcessDTO) criteria.uniqueResult();
    }

    public ScrollableResults findUsersToProcess(int entityId) {
        Criteria criteria = getSession().createCriteria(UserDTO.class)
                .add(Restrictions.eq("deleted", 0))
                .createAlias("company", "c")
                    .add(Restrictions.eq("c.id", entityId))
                .createAlias("userStatus", "us")
                    .add(Restrictions.lt("us.id", UserDTOEx.STATUS_SUSPENDED))
                .setProjection(Projections.id())
                .setComment("BillingProcessDAS.findUsersToProcess " + entityId);
        return criteria.scroll();
    }

    public void reset() {
        getSession().flush();
        getSession().clear();
    }
    
    public Iterator getCountAndSum(Integer processId) {
        final String hql =
                "select count(id), sum(total), currency.id " +
                "  from InvoiceDTO " +
                " where billingProcess.id = :processId " +
                " group by currency.id";

        Query query = getSession().createQuery(hql);
        query.setParameter("processId", processId);
        return query.iterate();
    }

    /**
     * Search succesfull payments in Payment_Invoice map (with quantity > 0)
     * and returns result, groupped by currency
     *
     * @param processId
     * @return Iterator with currency, method and sum of amount fields of query
     */
    public Iterator getSuccessfulProcessCurrencyMethodAndSum(Integer processId) {
        final String hql =
                "select invoice.currency.id, method.id, sum(invoice.total) " +
                "  from InvoiceDTO invoice inner join invoice.paymentMap paymentMap " +
                " join paymentMap.payment payment join payment.paymentMethod method " +
                " where invoice.billingProcess.id = :processId and paymentMap.amount > 0" +
                " group by invoice.currency.id, method.id " +
                " having sum(invoice.total) > 0";

        Query query = getSession().createQuery(hql);
        query.setParameter("processId", processId);
        return query.iterate();
    }

    /**
     * Selection records from Invoice table without payment records or
     * with payments of 0 amount. Result groupped by currency
     * @param processId
     * @return Iterator with currency and amount value
     */
    public Iterator getFailedProcessCurrencyAndSum(Integer processId) {
        final String hql =
                "select invoice.currency.id, sum(invoice.total) " +
                "  from InvoiceDTO invoice left join invoice.paymentMap paymentMap" +
                " where invoice.billingProcess.id = :processId and (paymentMap is NULL or paymentMap.amount = 0) " +
                " group by invoice.currency.id";


        Query query = getSession().createQuery(hql);
        query.setParameter("processId", processId);
        return query.iterate();
    }

    private static final String BILLABLE_USERS_TO_PROCESS =
            "SELECT a.id "
            + " FROM UserDTO a, OrderDTO o"
            + " WHERE a.id = o.baseUserByUserId.id"
            + " AND ( "
            + "     o.nextBillableDay is null "
            + "     or cast(o.nextBillableDay as date) <= :dueDate "
            + " ) "
            + " AND o.deleted = 0 "
            + " AND a.company.id = :entity ";

    public ScrollableResults findBillableUsersToProcess(int entityId, Date processDate) {
        Query query = getSession().createQuery(BILLABLE_USERS_TO_PROCESS);
        query.setParameter("dueDate", processDate);
        query.setParameter("entity", entityId);

        return query.scroll();
    }

    private static final String BILLABLE_USERS_WITH_ORDER_HQL =
            "select user.id "
            + " from OrderDTO purchaseOrder "
            + "     join purchaseOrder.baseUserByUserId as user "
            + " where "
            + "     user.deleted = 0 "
            + "     and user.company.id = :entity_id "
            + "     and purchaseOrder.orderStatus.id = :active_status_id "
            + "     and ( "
            + "         purchaseOrder.nextBillableDay is null"
            + "         or cast(purchaseOrder.nextBillableDay as date) <= :process_date "
            + "     )";

    /**
     * Returns all billable users with an order to process. This can be either any open one-time
     * order or an active recurring order with a valid next billable date.
     *
     * @param entityId entity id to find orders for
     * @param processDate billing process run date
     * @return billable users
     */
    public ScrollableResults findBillableUsersWithOrdersToProcess(int entityId, Date processDate) {
        Query query = getSession().createQuery(BILLABLE_USERS_WITH_ORDER_HQL);
        query.setParameter("entity_id", entityId);
        query.setParameter("active_status_id", Constants.ORDER_STATUS_ACTIVE);
        query.setParameter("process_date", processDate);
        
        return query.scroll();
    }
}
