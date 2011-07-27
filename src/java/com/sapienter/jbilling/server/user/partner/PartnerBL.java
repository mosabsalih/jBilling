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

package com.sapienter.jbilling.server.user.partner;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import javax.sql.rowset.CachedRowSet;

import com.sapienter.jbilling.common.CommonConstants;
import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.item.CurrencyBL;
import com.sapienter.jbilling.server.list.ResultList;
import com.sapienter.jbilling.server.notification.INotificationSessionBean;
import com.sapienter.jbilling.server.notification.MessageDTO;
import com.sapienter.jbilling.server.notification.NotificationBL;
import com.sapienter.jbilling.server.notification.NotificationNotFoundException;
import com.sapienter.jbilling.server.payment.PaymentBL;
import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.db.PaymentDAS;
import com.sapienter.jbilling.server.payment.db.PaymentDTO;
import com.sapienter.jbilling.server.payment.db.PaymentResultDAS;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.user.PartnerRangeComparator;
import com.sapienter.jbilling.server.user.PartnerSQL;
import com.sapienter.jbilling.server.user.db.UserDAS;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.user.partner.db.Partner;
import com.sapienter.jbilling.server.user.partner.db.PartnerDAS;
import com.sapienter.jbilling.server.user.partner.db.PartnerPayout;
import com.sapienter.jbilling.server.user.partner.db.PartnerPayoutDAS;
import com.sapienter.jbilling.server.user.partner.db.PartnerRange;
import com.sapienter.jbilling.server.user.partner.db.PartnerRangeDAS;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.Context;
import com.sapienter.jbilling.server.util.MapPeriodToCalendar;
import com.sapienter.jbilling.server.util.audit.EventLogger;
import com.sapienter.jbilling.server.util.db.CurrencyDAS;
import java.util.ArrayList;
import javax.sql.DataSource;

/**
 * @author Emil
 */
public class PartnerBL extends ResultList implements PartnerSQL {
    private static final Logger LOG = Logger.getLogger(PartnerBL.class);

    private PartnerDAS partnerDAS = null;
    private Partner partner = null;
    private PartnerRange partnerRange = null;
    private PartnerPayout payout = null;
    private EventLogger eLogger = null;

    public PartnerBL(Integer partnerId) {
        init();
        set(partnerId);
    }
    
    public PartnerBL() {
        init();
    }
    
    public PartnerBL(Partner entity) {
        partner = entity;
        init();
    }
    
    public void set(Integer partnerId) {
        partner = partnerDAS.find(partnerId);
    }
    
    public void setPayout(Integer payoutId) {
        payout = new PartnerPayoutDAS().find(payoutId);
    }

    private void init() {
        eLogger = EventLogger.getInstance();        
        payout = null;
        partnerRange = null;
        partnerDAS = new PartnerDAS();
    }

    public Partner getEntity() {
        return partner;
    }
    
    public Integer create(Partner dto) throws SessionInternalError {
        LOG.debug("creating partner");
        
        dto.setTotalPayments(BigDecimal.ZERO);
        dto.setTotalPayouts(BigDecimal.ZERO);
        dto.setTotalRefunds(BigDecimal.ZERO);
        dto.setDuePayout(BigDecimal.ZERO);
        partner = partnerDAS.save(dto);

        setRelatedClerk(partner, dto.getRelatedClerkUserId());
        
        LOG.debug("created partner id " + partner.getId());
        
        return partner.getId();
    }
    
    public void update(Integer executorId, Partner dto) {
        eLogger.audit(executorId, dto.getBaseUser().getId(), 
                Constants.TABLE_PARTNER, partner.getId(),
                EventLogger.MODULE_USER_MAINTENANCE, 
                EventLogger.ROW_UPDATED, null, null, 
                null);
        setRelatedClerk(partnerDAS.save(dto), dto.getRelatedClerkUserId());
    }
    
    private void setRelatedClerk(Partner dto, Integer id) {
        UserDTO user = new UserDAS().find(id);
        dto.setBaseUserByRelatedClerk(user);
        user.getPartnersForRelatedClerk().add(dto);
    }
    
    /**
     * This is called from a new transaction
     * @param partnerId
     */
    public void processPayout(Integer partnerId) 
            throws SQLException, SessionInternalError, PluggableTaskException, TaskException, NamingException {
        boolean notPaid;
        partner = partnerDAS.find(partnerId);
        // find out the date ranges for this payout
        Date startDate, endDate, dates[];
        dates = calculatePayoutDates();
        startDate = dates[0];
        endDate = dates[1];
       
        // see if this partner should be paid on-line
        boolean doProcess = partner.getAutomaticProcess() == 1;
        
        // some handy data
        Integer currencyId = partner.getUser().getCurrencyId();
        Integer entityId = partner.getUser().getEntity().getId();
        Integer userId = partner.getUser().getUserId();
        
        if (doProcess) {
            // now creating the row
            payout = new PartnerPayout();
            payout.setStartingDate(startDate);
            payout.setEndingDate(endDate);
            payout.setBalanceLeft(BigDecimal.ZERO);
            payout.setPaymentsAmount(BigDecimal.ZERO);
            payout.setRefundsAmount(BigDecimal.ZERO);
            payout.setPartner(partner);
            payout = new PartnerPayoutDAS().save(payout);
            partner.getPartnerPayouts().add(payout);
        } else {
            payout = null; // to avoid confustion
        }
        
        // get the total for this payout
        PartnerPayout dto = calculatePayout(startDate, endDate, 
                currencyId);
        
        if (doProcess) {
            PaymentDTOEx payment = PaymentBL.findPaymentInstrument(entityId,
                    userId);
            if (payment == null) {
                // this partner doesn't have a way to get paid
                eLogger.warning(entityId, userId, partnerId, 
                        EventLogger.MODULE_USER_MAINTENANCE, 
                        EventLogger.CANT_PAY_PARTNER, 
                        Constants.TABLE_PARTNER);
                notPaid = true;
            } else {
                payment.setAmount(dto.getPayment().getAmount());
                payment.setCurrency(partner.getUser().getCurrency());
                payment.setUserId(userId);
                payment.setPaymentDate(partner.getNextPayoutDate());
                notPaid = !processPayment(payment, entityId, dto, true);
             }
        } else {
            notPaid = true;
            // just notify to the clerk in charge
            notifyPayout(entityId, partner.getBaseUserByRelatedClerk().getLanguageIdField(),
                         dto.getPayment().getAmount(), startDate, endDate, true);
        }
        
        if (notPaid) {
            // let know that this partner should have been paid.
            notifyPayout(entityId, partner.getBaseUserByRelatedClerk().getLanguageIdField(),
                         dto.getPayment().getAmount(), startDate, endDate, true);
            // set the partner due payout
            partner.setDuePayout(dto.getPayment().getAmount());
        }

    }
    
    /**
     * This is to be called from the client, when creating a manual payout
     * @param partnerId
     * @param start
     * @param end
     * @param payment
     * @return
     */
    public Integer processPayout(Integer partnerId, Date start, Date end,
            PaymentDTOEx payment, Boolean process) 
            throws SessionInternalError, SQLException, NamingException {
        
        partner = partnerDAS.find(partnerId);
        payout = new PartnerPayout();
        payout.setStartingDate(start);
        payout.setEndingDate(end);
        payout.setBalanceLeft(BigDecimal.ZERO);
        payout.setPaymentsAmount(BigDecimal.ZERO);
        payout.setRefundsAmount(BigDecimal.ZERO);
        payout.setPartner(partner);
        payout = new PartnerPayoutDAS().save(payout);
        partner.getPartnerPayouts().add(payout);
        
        // get the total for this payout
        PartnerPayout dto = calculatePayout(start, end, 
                payment.getCurrency().getId());
    
        // finish the payment
        payment.setIsRefund(new Integer(1));
        payment.setAttempt(new Integer(1));
        processPayment(payment, partner.getUser().getEntity().getId(), dto,
                process.booleanValue());
        return payment.getPaymentResult().getId();
    }
    
    public Date[] calculatePayoutDates() throws NamingException, SQLException, SessionInternalError{
        Date retValue[] = new Date[2];        
        // for this I have to find the last payout for this partner
        Integer payoutId = getLastPayout(partner.getId());
        Date lastEndDate;
        // the return value of 'empty' from a function (max) could vary from db to db
        if (payoutId != null && payoutId.intValue() != 0) {
            PartnerPayout previousPayout = 
                    new PartnerPayoutDAS().find(payoutId);
            lastEndDate = previousPayout.getEndingDate();
        } else {
            // if this is the first payout, calculate from the creation of the partner
            lastEndDate = partner.getUser().getCreateDatetime();
        }
        retValue[0] = lastEndDate;
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(lastEndDate);
        cal.add(MapPeriodToCalendar.map(partner.getPeriodUnit().getId()), 
                partner.getPeriodValue());
        retValue[1] = cal.getTime();
        LOG.debug("Dates for partner " + partner.getId() + " start= " + retValue[0] +
                " end " + retValue[1]);
        return retValue;
    }
    
    private boolean processPayment(PaymentDTOEx payment, Integer entityId,
            PartnerPayout dto, boolean process) 
            throws NamingException, SessionInternalError {
        PaymentBL paymentBL = new PaymentBL();
        boolean retValue;
        PaymentDTO createdPayment = null;
        // isRefund is not null, so having to decide it is better to use refund.
        payment.setPayoutId(payout.getId());
        payment.setIsRefund(new Integer(1));
        payment.setAttempt(new Integer(1));
        payment.setBalance(BigDecimal.ZERO);
                
        // process the payment realtime
        Integer result = Constants.RESULT_OK;
        if (process) {
            result = paymentBL.processPayment(entityId, payment);
            createdPayment = paymentBL.getEntity();
            if (result == null) { // means no pluggable task config.
                result = Constants.RESULT_UNAVAILABLE;
            }
        } else {
            // create the payment row
            paymentBL.create(payment);
            createdPayment = paymentBL.getEntity();
        }
        // and link it to this payout row
        payout.setPayment(new PaymentDAS().find(paymentBL.getEntity().getId()));
                
        // update this partner fields if the payment went through
        if (result.equals(Constants.RESULT_OK)) {
            applyPayout(dto);

            // this partner just got a full payout
            partner.setDuePayout(BigDecimal.ZERO);

            // if there was something paid, notify
            if (BigDecimal.ZERO.compareTo(dto.getPayment().getAmount()) < 0) {                
                LOG.debug("payout notification partner = " + partner.getId()
                            + " with language = " + partner.getUser().getLanguageIdField());
                notifyPayout(entityId, partner.getUser().getLanguageIdField(), dto.getPayment().getAmount(),
                             dto.getStartingDate(), dto.getEndingDate(), false);
            }
            retValue = true;
        } else {
            retValue = false;
        }
        createdPayment.setPaymentResult(new PaymentResultDAS().find(result));
        payment.setPaymentResult(createdPayment.getPaymentResult());

        return retValue;
    }
    
    /**
     * Goes over the payments/refunds of the current partner for the
     * given period. It will update the records selected linking them to 
     * the new payout record and the totals of the payout record if 
     * such record has been initialized.
     * @param start
     * @param end
     * @return
     */
    public PartnerPayout calculatePayout(Date start, Date end, Integer currencyId) 
            throws NamingException, SQLException, SessionInternalError {
        BigDecimal total = new BigDecimal("0");
        BigDecimal paymentTotal = new BigDecimal("0");
        BigDecimal refundTotal = new BigDecimal("0");
        
        LOG.debug("Calculating payout partner " + partner.getId() + " from " + 
                start + " to " + end);
        Connection conn = ((DataSource) Context.getBean(Context.Name.DATA_SOURCE)).getConnection();
        PreparedStatement stmt = conn.prepareStatement(paymentsInPayout);
        stmt.setInt(1, partner.getId());
        stmt.setDate(2, new java.sql.Date(start.getTime()));
        stmt.setDate(3, new java.sql.Date(end.getTime()));
        ResultSet result = stmt.executeQuery();
        // since esql doesn't support dates, a direct call is necessary
        while (result.next()) {
            PaymentBL payment = new PaymentBL(new Integer(result.getInt(1)));
            Integer paymentCurrencyId = payment.getEntity().getCurrency().getId();
            Integer entityId = partner.getUser().getEntity().getId();
            
            // the amount will have to be in the requested currency
            // convert then the payment amout
            CurrencyBL currency = new CurrencyBL();
            BigDecimal paymentAmount = currency.convert(paymentCurrencyId, currencyId, payment.getEntity().getAmount(), entityId);
            LOG.debug("payment amount = " + paymentAmount);
            BigDecimal amount = calculateCommission(paymentAmount, currencyId, payment.getEntity().getBaseUser(), payout != null); 
            LOG.debug("commission = " + amount);
            
            // payments add, refunds take
            if (payment.getEntity().getIsRefund() == 0) {
                total = total.add(amount);
                paymentTotal = paymentTotal.add(amount);
            } else {
                total = total.subtract(amount);
                refundTotal = refundTotal.add(amount);
            }
            if (payout != null) {
                // update the payment record with the new payout
                payment.getEntity().setPayoutIncludedIn(payout);
            }
        }
        result.close();
        stmt.close();
        conn.close();
        
        if (payout != null) {
            // update the payout row
            payout.setPaymentsAmount(paymentTotal);
            payout.setRefundsAmount(refundTotal);
        }
        
        LOG.debug("total " + total + " currency = " + currencyId);
        PartnerPayout retValue = new PartnerPayout();
        PaymentDTO payment = new PaymentDTO();
        payment.setAmount(total);
        payment.setCurrency(new CurrencyDAS().find(currencyId));
        payment.setBaseUser(partner.getBaseUser());
        retValue.setPayment(payment);
        retValue.setRefundsAmount(refundTotal);
        retValue.setPaymentsAmount(paymentTotal);
        retValue.setStartingDate(start);
        retValue.setEndingDate(end);
        
        return retValue;
    }
    
    /**
     * This will return the id of the lates payout that was successfull
     * @param partnerId
     * @return
     * @throws NamingException
     * @throws SQLException
     */
    private Integer getLastPayout(Integer partnerId) 
            throws NamingException, SQLException {
        Integer retValue = null;
        Connection conn = ((DataSource) Context.getBean(Context.Name.DATA_SOURCE)).getConnection();
        PreparedStatement stmt = conn.prepareStatement(lastPayout);
        stmt.setInt(1, partnerId.intValue());
        ResultSet result = stmt.executeQuery();
        // since esql doesn't support max, a direct call is necessary
        if (result.next()) {
            retValue = new Integer(result.getInt(1));
        }
        result.close();
        stmt.close();
        conn.close();
        LOG.debug("Finding last payout ofr partner " + partnerId + " result = " + retValue);
        return retValue;
    }
    
    /**
     * Will update the partner fields with the total of this payout
     * @param dto
     */
    public void applyPayout(PartnerPayout dto) 
            throws SessionInternalError {

        // the balance goes down with a payout
        BigDecimal balance = partner.getBalance().subtract(dto.getPayment().getAmount());
        partner.setBalance(balance);
        
        // add this payout to her total
        BigDecimal total = partner.getTotalPayouts().add(dto.getPayment().getAmount());
        partner.setTotalPayouts(total);
        
        // the next payout
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(partner.getNextPayoutDate());
        cal.add(MapPeriodToCalendar.map(partner.getPeriodUnit().getId()), partner.getPeriodValue());
        partner.setNextPayoutDate(cal.getTime());
    }
    
    public void notifyPayout(Integer entityId, Integer languageId, BigDecimal total, Date start, Date end,
                             boolean clerk) throws NamingException, SessionInternalError {
        // make the notification
        NotificationBL notification = new NotificationBL();
        try {
            MessageDTO message = notification.getPayoutMessage(entityId,
                    languageId, total, start, end, clerk, partner.getId());
     
            INotificationSessionBean notificationSess = 
                    (INotificationSessionBean) Context.getBean(
                    Context.Name.NOTIFICATION_SESSION);
            if (!clerk) {
                notificationSess.notify(partner.getUser(), message);
            } else {
                notificationSess.notify(partner.getBaseUserByRelatedClerk(), message);

            }
        } catch (NotificationNotFoundException e) {
            //  this entity has not defined
            // a message for the payout
            LOG.warn("A payout message shoule've been sent, but entity " + 
                    entityId + " has not defined a notification");
        }
    }
    
    public BigDecimal calculateCommission(BigDecimal amount, Integer currencyId,
            UserDTO user, boolean update) 
            throws SessionInternalError, NamingException, SQLException {
        LOG.debug("Calculating commision on " + amount); 
        BigDecimal result;
        if (partner.getOneTime() == 1) {
            // this partner gets paid once per customer she brings
            Integer flag = user.getCustomer().getReferralFeePaid();
            if (flag == null || flag.intValue() == 0) {
                if (update) { // otherwise just calculate
                    user.getCustomer().setReferralFeePaid(
                            new Integer(1));
                }
            } else {
                // it got a fee from this guy already
                return BigDecimal.ZERO;
            }
        } 
        
        // find the rate
        BigDecimal rate = null;
        BigDecimal fee = null;
        if (partner.getRanges().size() > 0) {
            getRangedCommission();
            rate = partnerRange.getPercentageRate() == null ? null : 
                    new BigDecimal(partnerRange.getPercentageRate().toString());
            fee = partnerRange.getReferralFee() == null ? null : 
                    new BigDecimal(partnerRange.getReferralFee().toString());
        } else {
            rate = partner.getPercentageRate();
            fee = partner.getReferralFee();
        }

        LOG.debug("using rate " + rate + " fee " + fee);
        // apply the rate to get the commission value
        if (rate != null && (rate.compareTo(BigDecimal.ZERO) != 0)) {
            result = amount.divide(new BigDecimal("100"),
                    CommonConstants.BIGDECIMAL_SCALE,
                    CommonConstants.BIGDECIMAL_ROUND).multiply(rate);
        } else if (fee != null && (fee.compareTo(BigDecimal.ZERO) != 0)) {
            CurrencyBL currency = new CurrencyBL();
            Integer partnerCurrencyId = partner.getFeeCurrency().getId();
            if (partnerCurrencyId == null) {
                LOG.info("Partner without currency, using entity's as default");
                partnerCurrencyId = partner.getUser().getEntity().getCurrencyId();
            }
            result = currency.convert(partnerCurrencyId, currencyId, fee,
                    partner.getUser().getEntity().getId());
        } else {
            throw new SessionInternalError(
                    "Partner without commission configuration");
        }
        LOG.debug("result = " + result);
        return result;
    }
    
    /**
     * Go over the rates for this partner and return the right
     * range for the amount of customers
     * After the call, the variable partnerRange is set to the right range
     */
    private void getRangedCommission() 
            throws NamingException, SQLException {
        int totalCustomers = getCustomersCount();
        // if there were more than just 20 rows, this would have to
        // be done all with plain sql instead of ejbs
        List<PartnerRange> rates = new ArrayList(partner.getRanges());
        Collections.sort(rates, new PartnerRangeComparator());
        partnerRange = null; // to get an exception if there are no ranges
        
        for (int f=0; f < rates.size(); f++) {
            partnerRange = rates.get(f);
            if (partnerRange.getRangeFrom() <= totalCustomers &&
                    partnerRange.getRangeTo() >= totalCustomers) {
                break;
            }
        }
        // we will always return a rate. If none were found, the last one
        // (biggest) is returned
    }
    
    private int getCustomersCount() 
            throws SQLException, NamingException {
        int retValue = 0;
        Connection conn = ((DataSource) Context.getBean(Context.Name.DATA_SOURCE)).getConnection();
        PreparedStatement stmt = conn.prepareStatement(countCustomers);
        stmt.setInt(1, partner.getId());
        ResultSet result = stmt.executeQuery();
        // since esql doesn't support max, a direct call is necessary
        if (result.next()) {
            retValue = result.getInt(1);
        }
        result.close();
        stmt.close();
        conn.close();
        return retValue;
    }
    
    public Partner getDTO() {
        partner.setRelatedClerkUserId(partner.getBaseUserByRelatedClerk().getId());
        return partner;
    }
    
    public PartnerPayout getLastPayoutDTO(Integer partnerId) 
            throws SQLException, NamingException {
        PartnerPayout retValue = null;
        
        Integer payoutId = getLastPayout(partnerId);
        if (payoutId != null && payoutId.intValue() != 0) {
            payout = new PartnerPayoutDAS().find(payoutId);
            retValue = getPayoutDTO();
        }
        return retValue;
    }
    
    public PartnerPayout getPayoutDTO() 
            throws NamingException {
        payout.touch();
        return payout;
    }
    
    public CachedRowSet getList(Integer entityId)
            throws SQLException, Exception{

        prepareStatement(PartnerSQL.list);
        cachedResults.setInt(1,entityId.intValue());
        execute();
        conn.close();
        return cachedResults;
    }

    public CachedRowSet getPayoutList(Integer partnerId)
            throws SQLException, Exception{

        prepareStatement(PartnerSQL.listPayouts);
        cachedResults.setInt(1, partnerId.intValue());
        execute();
        conn.close();
        return cachedResults;
    }
    
    /**
     * Remove the existing ranges and create rows with 
     * the values of the parameter
     * @param ranges
     */
    public void setRanges(Integer executorId, PartnerRange[] ranges) {
        eLogger.audit(executorId, partner.getBaseUser().getId(), 
                Constants.TABLE_PARTNER_RANGE, partner.getId(),
                EventLogger.MODULE_USER_MAINTENANCE, 
                EventLogger.ROW_UPDATED, null, null, null);
        // remove existing ranges (a clear will only set the partner_id = null)
        for (Iterator it = partner.getRanges().iterator(); it.hasNext();) {
            partnerRange = (PartnerRange) it.next();
            it.remove();
            new PartnerRangeDAS().delete(partnerRange);
        }
        
        // may be this is a delete
        if (ranges == null) {
            return;
        }
        // go through the array creating the rows
        for (int f = 0; f < ranges.length; f++) {
            PartnerRange range = new PartnerRange();
            range.setPartner(partner);
            range.setRangeFrom(ranges[f].getRangeFrom());
            range.setRangeTo(ranges[f].getRangeTo());
            range.setPercentageRate(ranges[f].getPercentageRate());
            range.setReferralFee(ranges[f].getReferralFee());
            partnerRange = new PartnerRangeDAS().save(range);
            partner.getRanges().add(partnerRange);
        }
    }

    /**
     * Convert a given Partner into a PartnerWS web-service object.
     *
     * @param dto dto to convert
     * @return converted web-service object
     */
    public static PartnerWS getWS(Partner dto) {
        return dto != null ? new PartnerWS(dto) : null;
    }

}
