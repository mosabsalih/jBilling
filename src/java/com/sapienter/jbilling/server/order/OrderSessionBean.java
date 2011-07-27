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

package com.sapienter.jbilling.server.order;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

import org.apache.log4j.Logger;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.item.ItemDecimalsException;
import com.sapienter.jbilling.server.order.db.OrderDAS;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderLineDAS;
import com.sapienter.jbilling.server.order.db.OrderPeriodDTO;

/**
 *
 * This is the session facade for the orders in general. It is a statless
 * bean that provides services not directly linked to a particular operation
 *
 * @author emilc
 **/
@Transactional( propagation = Propagation.REQUIRED )
public class OrderSessionBean implements IOrderSessionBean {
    
    private static final Logger LOG = Logger.getLogger(OrderSessionBean.class);

    @Transactional( propagation = Propagation.REQUIRES_NEW )
    public void reviewNotifications(Date today) 
            throws SessionInternalError {
        
        try {
            OrderBL order = new OrderBL();
            order.reviewNotifications(today);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public OrderDTO getOrder(Integer orderId) throws SessionInternalError {
        try {
            OrderDAS das = new OrderDAS();
            OrderDTO order = das.find(orderId);
            order.touch();
            return order;
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public OrderDTO getOrderEx(Integer orderId, Integer languageId) 
            throws SessionInternalError {
        try {
            OrderDAS das = new OrderDAS();
            OrderDTO order = das.find(orderId);
            order.addExtraFields(languageId);
            order.touch();
            das.detach(order);
            Collections.sort(order.getLines(), new OrderLineComparator());
            //LOG.debug("returning order " + order);
            return order;
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public OrderDTO setStatus(Integer orderId, Integer statusId, 
            Integer executorId, Integer languageId) 
            throws SessionInternalError {
        try {
            OrderBL order = new OrderBL(orderId);
            order.setStatus(executorId, statusId);
            OrderDTO dto = order.getDTO();
            dto.addExtraFields(languageId);
            dto.touch();
            return dto;
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    /**
     * This is a version used by the http api, should be
     * the same as the web service but without the 
     * security check
    public Integer create(OrderWS order, Integer entityId,
            String rootUser, boolean process) 
            throws SessionInternalError {
        try {
            LOG.debug("bkp 1");
            // get the info from the caller
            UserBL bl = new UserBL();
            bl.setRoot(rootUser);
            Integer executorId = bl.getEntity().getUserId();
            LOG.debug("bkp 2");
            // make a dto out of the ws
            NewOrderDTO dto = new NewOrderDTO(order);
            
            // call the creation
            OrderBL orderBL = new OrderBL(dto);
            if (process) {
                orderBL.fillInLines(dto, entityId);
                orderBL.recalculate(entityId);
            }
            LOG.debug("bkp 4");
            return orderBL.create(entityId, executorId, dto);
            
        } catch(Exception e) {
            LOG.debug("Exception:", e);
            throw new SessionInternalError(e);
        }

    }
     */


    public void delete(Integer id, Integer executorId) 
            throws SessionInternalError {
        try {
            // now get the order
            OrderBL bl = new OrderBL(id);
            bl.delete(executorId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }

    }
 
    public OrderPeriodDTO[] getPeriods(Integer entityId, Integer languageId) 
            throws SessionInternalError {
        try {
            // now get the order
            OrderBL bl = new OrderBL();
            return bl.getPeriods(entityId, languageId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public OrderPeriodDTO getPeriod(Integer languageId, Integer id) 
            throws SessionInternalError {
        try {
            // now get the order
            OrderBL bl = new OrderBL();
            OrderPeriodDTO dto =  bl.getPeriod(languageId, id);
            dto.touch();
            
            return dto;
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public void setPeriods(Integer languageId, OrderPeriodDTO[] periods)
            throws SessionInternalError {
        OrderBL bl = new OrderBL();
        bl.updatePeriods(languageId, periods);
    }

    public void addPeriod(Integer entityId, Integer languageId) 
            throws SessionInternalError {
        try {
            // now get the order
            OrderBL bl = new OrderBL();
            bl.addPeriod(entityId, languageId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public Boolean deletePeriod(Integer periodId) 
            throws SessionInternalError {
        try {
            // now get the order
            OrderBL bl = new OrderBL();
            return new Boolean(bl.deletePeriod(periodId));
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public OrderDTO getMainOrder(Integer userId)
            throws SessionInternalError {
        try {
            OrderBL bl = new OrderBL();
            Integer orderId = bl.getMainOrderId(userId);
            if (orderId == null) {
                // there was no main order
                return null;
            }
            return getOrder(orderId);
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public OrderDTO addItem(Integer itemID, BigDecimal quantity, OrderDTO order, Integer languageId, Integer userId,
                            Integer entityId) throws SessionInternalError, ItemDecimalsException {

        LOG.debug("Adding item " + itemID + " q:" + quantity);

        OrderBL bl = new OrderBL(order);
        bl.addItem(itemID, quantity, languageId, userId, entityId, order.getCurrencyId());
        return order;
    }
    
    public OrderDTO addItem(Integer itemID, Integer quantity, OrderDTO order, Integer languageId, Integer userId,
                            Integer entityId) throws SessionInternalError, ItemDecimalsException {

        return addItem(itemID, new BigDecimal(quantity), order, languageId, userId, entityId);
    }

    public OrderDTO recalculate(OrderDTO modifiedOrder, Integer entityId) 
            throws ItemDecimalsException {
        
        OrderBL bl = new OrderBL();
        bl.set(modifiedOrder);
        bl.recalculate(entityId);
        return bl.getDTO();
    }

    public Integer createUpdate(Integer entityId, Integer executorId, 
            OrderDTO order, Integer languageId) throws SessionInternalError {
        Integer retValue = null;
        try {
            OrderBL bl = new OrderBL();
            if (order.getId() == null) {
                retValue = bl.create(entityId, executorId, order);
            } else {
                bl.set(order.getId());
                bl.update(executorId, order);
            }
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
        
        return retValue;
    }

     public Long getCountWithDecimals(Integer itemId) 
             throws SessionInternalError {
         try {
            return new OrderLineDAS().findLinesWithDecimals(itemId);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
    
}
