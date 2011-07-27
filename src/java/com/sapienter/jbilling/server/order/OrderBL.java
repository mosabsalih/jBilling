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

import com.sapienter.jbilling.common.CommonConstants;
import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.item.ItemBL;
import com.sapienter.jbilling.server.item.ItemDecimalsException;
import com.sapienter.jbilling.server.item.PricingField;
import com.sapienter.jbilling.server.item.db.ItemDAS;
import com.sapienter.jbilling.server.item.tasks.IItemPurchaseManager;
import com.sapienter.jbilling.server.invoice.InvoiceBL;
import com.sapienter.jbilling.server.list.ResultList;
import com.sapienter.jbilling.server.mediation.Record;
import com.sapienter.jbilling.server.notification.INotificationSessionBean;
import com.sapienter.jbilling.server.notification.MessageDTO;
import com.sapienter.jbilling.server.notification.NotificationBL;
import com.sapienter.jbilling.server.notification.NotificationNotFoundException;
import com.sapienter.jbilling.server.order.db.OrderBillingTypeDAS;
import com.sapienter.jbilling.server.order.db.OrderDAS;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderLineDAS;
import com.sapienter.jbilling.server.order.db.OrderLineDTO;
import com.sapienter.jbilling.server.order.db.OrderLineTypeDAS;
import com.sapienter.jbilling.server.order.db.OrderLineTypeDTO;
import com.sapienter.jbilling.server.order.db.OrderPeriodDAS;
import com.sapienter.jbilling.server.order.db.OrderPeriodDTO;
import com.sapienter.jbilling.server.order.db.OrderProcessDTO;
import com.sapienter.jbilling.server.order.db.OrderStatusDAS;
import com.sapienter.jbilling.server.order.event.NewActiveUntilEvent;
import com.sapienter.jbilling.server.order.event.NewOrderEvent;
import com.sapienter.jbilling.server.order.event.NewQuantityEvent;
import com.sapienter.jbilling.server.order.event.NewStatusEvent;
import com.sapienter.jbilling.server.order.event.OrderDeletedEvent;
import com.sapienter.jbilling.server.order.event.PeriodCancelledEvent;
import com.sapienter.jbilling.server.pluggableTask.OrderProcessingTask;
import com.sapienter.jbilling.server.pluggableTask.TaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
import com.sapienter.jbilling.server.process.ConfigurationBL;
import com.sapienter.jbilling.server.process.db.PeriodUnitDAS;
import com.sapienter.jbilling.server.provisioning.db.ProvisioningStatusDAS;
import com.sapienter.jbilling.server.provisioning.event.SubscriptionActiveEvent;
import com.sapienter.jbilling.server.system.event.EventManager;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.user.db.UserDAS;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.Context;
import com.sapienter.jbilling.server.util.PreferenceBL;
import com.sapienter.jbilling.server.util.audit.EventLogger;
import com.sapienter.jbilling.server.util.db.CurrencyDAS;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import javax.sql.rowset.CachedRowSet;

import javax.naming.NamingException;
import javax.sql.DataSource;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Emil
 */
public class OrderBL extends ResultList
        implements OrderSQL {

    private OrderDTO order = null;
    private OrderLineDAS orderLineDAS = null;
    private OrderPeriodDAS orderPeriodDAS = null;
    private OrderDAS orderDas = null;
    private OrderBillingTypeDAS orderBillingTypeDas = null;
    private ProvisioningStatusDAS provisioningStatusDas = null;
    private static final Logger LOG = Logger.getLogger(OrderBL.class);
    private EventLogger eLogger = null;

    public OrderBL(Integer orderId) {
        init();
        set(orderId);
    }

    public OrderBL() {
        init();
    }

    public OrderBL(OrderDTO order) {
        init();
        this.order = order;
    }

    private void init() {
        eLogger = EventLogger.getInstance();
        orderLineDAS = new OrderLineDAS();
        orderPeriodDAS = new OrderPeriodDAS();
        orderDas = new OrderDAS();
        orderBillingTypeDas = new OrderBillingTypeDAS();
        provisioningStatusDas = new ProvisioningStatusDAS();
    }

    public OrderDTO getEntity() {
        return order;
    }

    public OrderPeriodDTO getPeriod(Integer language, Integer id) {
        return (orderPeriodDAS.find(id));
    }

    public void set(Integer id) {
        order = orderDas.find(id);
    }

    public void setForUpdate(Integer id) {
        order = orderDas.findForUpdate(id);
    }

    public void set(OrderDTO newOrder) {
        order = newOrder;
    }

    public OrderWS getWS(Integer languageId) {
        OrderWS retValue = new OrderWS(order.getId(), order.getBillingTypeId(),
                                       order.getNotify(), order.getActiveSince(), order.getActiveUntil(),
                                       order.getCreateDate(), order.getNextBillableDay(),
                                       order.getCreatedBy(), order.getStatusId(), order.getDeleted(),
                                       order.getCurrencyId(), order.getLastNotified(),
                                       order.getNotificationStep(), order.getDueDateUnitId(),
                                       order.getDueDateValue(), order.getAnticipatePeriods(),
                                       order.getDfFm(), order.getIsCurrent(), order.getNotes(),
                                       order.getNotesInInvoice(), order.getOwnInvoice(),
                                       order.getOrderPeriod().getId(),
                                       order.getBaseUserByUserId().getId(),
                                       order.getVersionNum(), order.getCycleStarts());

        retValue.setTotal(order.getTotal());

        retValue.setPeriodStr(order.getOrderPeriod().getDescription(languageId));
        retValue.setStatusStr(order.getOrderStatus().getDescription(languageId));
        retValue.setBillingTypeStr(order.getOrderBillingType().getDescription(languageId));

        List<OrderLineWS> lines = new ArrayList<OrderLineWS>();
        for (Iterator it = order.getLines().iterator(); it.hasNext();) {
            OrderLineDTO line = (OrderLineDTO) it.next();
            if (line.getDeleted() == 0) {
                lines.add(getOrderLineWS(line.getId()));
            }
        }
        //this will initialized Generated Invoices in the OrderDTO instance
        order.addExtraFields(languageId);
        retValue.setGeneratedInvoices(new InvoiceBL().DTOtoWS(new ArrayList(order.getInvoices())));
        retValue.setOrderLines(new OrderLineWS[lines.size()]);
        lines.toArray(retValue.getOrderLines());
        return retValue;
    }

    public OrderDTO getDTO() {
        return order;
    }

    public void addItem(OrderDTO order, Integer itemID, BigDecimal quantity, Integer language, Integer userId,
                        Integer entityId, Integer currencyId, List<Record> records) throws ItemDecimalsException {

        try {
            PluggableTaskManager<IItemPurchaseManager> taskManager =
                    new PluggableTaskManager<IItemPurchaseManager>(entityId,
                                                                   Constants.PLUGGABLE_TASK_ITEM_MANAGER);
            IItemPurchaseManager myTask = taskManager.getNextClass();

            while (myTask != null) {
                myTask.addItem(itemID, quantity, language, userId, entityId, currencyId, order, records);
                myTask = taskManager.getNextClass();
            }

        } catch (PluggableTaskException e) {
            throw new SessionInternalError("Item Manager task error", OrderBL.class, e);
        } catch (TaskException e) {
            if (e.getCause() instanceof ItemDecimalsException) {
                throw (ItemDecimalsException) e.getCause();
            } else {
                // do not change this error text, it is used to identify the error
                throw new SessionInternalError("Item Manager task error", OrderBL.class, e);
            }
        }

    }

    public void addItem(Integer itemID, BigDecimal quantity, Integer language, Integer userId,
                        Integer entityId, Integer currencyId, List<Record> records) {

        addItem(this.order, itemID,  quantity, language, userId, entityId, currencyId, records);
    }

    public void addItem(Integer itemID, BigDecimal quantity, Integer language, Integer userId, Integer entityId,
                        Integer currencyId) throws ItemDecimalsException {
        addItem(itemID, quantity, language, userId, entityId, currencyId, null);
    }

    public void addItem(Integer itemID, Integer quantity, Integer language, Integer userId, Integer entityId,
                        Integer currencyId, List<Record> records) throws ItemDecimalsException {
        addItem(itemID, new BigDecimal(quantity), language, userId, entityId, currencyId, records);
    }

    public void addItem(Integer itemID, Integer quantity, Integer language, Integer userId, Integer entityId,
                        Integer currencyId) throws ItemDecimalsException {
        addItem(itemID, new BigDecimal(quantity), language, userId, entityId, currencyId, null);
    }

    public void deleteItem(Integer itemID) {
        order.removeLine(itemID);
    }

    public void delete(Integer executorId) {
        // the event is needed before the deletion
        EventManager.process(new OrderDeletedEvent(
                order.getBaseUserByUserId().getCompany().getId(), order));

        for (OrderLineDTO line : order.getLines()) {
            line.setDeleted(1);
        }
        order.setDeleted(1);

        eLogger.audit(executorId, order.getBaseUserByUserId().getId(),
                      Constants.TABLE_PUCHASE_ORDER, order.getId(),
                      EventLogger.MODULE_ORDER_MAINTENANCE,
                      EventLogger.ROW_DELETED, null,
                      null, null);
    }

    /**
     * Method recalculate.
     * Goes over the processing tasks configured in the database for this
     * entity. The order entity is then modified.
     */
    public void recalculate(Integer entityId) throws SessionInternalError, ItemDecimalsException {
        LOG.debug("Processing and order for reviewing." + order.getLines().size());
        // make sure the user is there
        UserDAS user = new UserDAS();
        order.setBaseUserByUserId(user.find(order.getBaseUserByUserId().getId()));
        // some things can't be null, otherwise hibernate complains
        order.setDefaults();
        order.touch();

        try {
            PluggableTaskManager taskManager = new PluggableTaskManager(
                    entityId, Constants.PLUGGABLE_TASK_PROCESSING_ORDERS);
            OrderProcessingTask task =
                    (OrderProcessingTask) taskManager.getNextClass();
            while (task != null) {
                task.doProcessing(order);
                task = (OrderProcessingTask) taskManager.getNextClass();
            }

        } catch (PluggableTaskException e) {
            LOG.fatal("Problems handling order processing task.", e);
            throw new SessionInternalError("Problems handling order " +
                                           "processing task.");
        } catch (TaskException e) {
            if (e.getCause() instanceof ItemDecimalsException) {
                throw (ItemDecimalsException) e.getCause();
            }
            LOG.fatal("Problems excecuting order processing task.", e);
            throw new SessionInternalError("Problems executing order processing task.");
        }
    }

    public Integer create(Integer entityId, Integer userAgentId,
                          OrderDTO orderDto) throws SessionInternalError {
        try {
            // if the order is a one-timer, force pre-paid to avoid any
            // confusion
            if (orderDto.getOrderPeriod().getId() == Constants.ORDER_PERIOD_ONCE) {
                orderDto.setOrderBillingType(orderBillingTypeDas.find(Constants.ORDER_BILLING_PRE_PAID));
                // one time orders can not be the main subscription
                orderDto.setIsCurrent(0);
            }
            UserDAS user = new UserDAS();
            if (userAgentId != null) {
                orderDto.setBaseUserByCreatedBy(user.find(userAgentId));
            }

            // create the record
            orderDto.setBaseUserByUserId(user.find(orderDto.getBaseUserByUserId().getId()));
            orderDto.setOrderPeriod(orderPeriodDAS.find(orderDto.getOrderPeriod().getId()));
            // set the provisioning status
            for (OrderLineDTO line : orderDto.getLines()) {
                // set default provisioning status id for order lines
                if (line.getProvisioningStatus() == null) {
                    line.setProvisioningStatus(provisioningStatusDas.find(
                            Constants.PROVISIONING_STATUS_INACTIVE));
                } else {
                    line.setProvisioningStatus(provisioningStatusDas.find(
                            line.getProvisioningStatus().getId()));
                }
            }
            orderDto.setDefaults();

            order = orderDas.save(orderDto);

            // link the lines to the new order
            for (OrderLineDTO line : order.getLines()) {
                line.setPurchaseOrder(order);
            }

            if (order.getIsCurrent() != null && order.getIsCurrent().intValue() == 1) {
                setMainSubscription(userAgentId);
            }
            //check if order is created with activeSince<= now (or null)
            Date now = new Date();
            Date activeSince = order.getActiveSince();
            if (activeSince == null || activeSince.before(now)) {
                // generate SubscriptionActiveEvent for order
                SubscriptionActiveEvent newEvent = new SubscriptionActiveEvent(entityId, order);
                EventManager.process(newEvent);
                LOG.debug("OrderBL.create(): generated SubscriptionActiveEvent for order: " + order.getId());
            }

            // add a log row for convenience
            eLogger.auditBySystem(entityId, order.getBaseUserByUserId().getId(),
                                  Constants.TABLE_PUCHASE_ORDER, order.getId(),
                                  EventLogger.MODULE_ORDER_MAINTENANCE, EventLogger.ROW_CREATED, null, null, null);

            EventManager.process(new NewOrderEvent(entityId, order));
        } catch (Exception e) {
            throw new SessionInternalError("Create exception creating order entity bean", OrderBL.class, e);
        }

        return order.getId();
    }

    public void updateActiveUntil(Integer executorId, Date to, OrderDTO newOrder) {
        audit(executorId, order.getActiveUntil());
        // this needs an event
        NewActiveUntilEvent event = new NewActiveUntilEvent(order.getId(), to, order.getActiveUntil());
        EventManager.process(event);
        // update the period of the latest invoice as well. This is needed
        // because it is the way to extend a subscription when the
        // order status is finished. Then the next_invoice_date is null.
        if (order.getOrderStatus().getId() == CommonConstants.ORDER_STATUS_FINISHED) {
            updateEndOfOrderProcess(to);
        }

        // update it
        order.setActiveUntil(to);

        // if the new active until is earlier than the next invoice date, we have a 
        // period already invoice being cancelled
        if (isDateInvoiced(to)) {
            // pass the new order, rather than the existing one. Otherwise, the exsiting gets
            // and changes overwritten by the data of the new order.
            EventManager.process(new PeriodCancelledEvent(newOrder,
                                                          order.getBaseUserByUserId().getCompany().getId(), executorId));
        }
    }

    /**
     * Method checkOrderLineQuantities.
     * Creates a NewQuantityEvent for each order line that has had
     * its quantity modified (including those added or deleted).
     * @return An array with all the events that should be fiered. This
     * prevents events being fired when the order has not be saved and it is
     * still 'mutating'.
     */
    public List<NewQuantityEvent> checkOrderLineQuantities(List<OrderLineDTO> oldLines,
                                                           List<OrderLineDTO> newLines, Integer entityId, Integer orderId, boolean sendEvents) {

        List<NewQuantityEvent> retValue = new ArrayList<NewQuantityEvent>();
        // NewQuantityEvent is generated when an order line and it's quantity 
        // has changed, including from >0 to 0 (deleted) and 0 to >0 (added).
        // First, copy and sort new and old order lines by order line id.
        List<OrderLineDTO> oldOrderLines = new ArrayList(oldLines);
        List<OrderLineDTO> newOrderLines = new ArrayList(newLines);
        Comparator<OrderLineDTO> sortByOrderLineId = new Comparator<OrderLineDTO>() {

            public int compare(OrderLineDTO ol1, OrderLineDTO ol2) {
                return ol1.getId() - ol2.getId();
            }
        };
        Collections.sort(oldOrderLines, sortByOrderLineId);
        Collections.sort(newOrderLines, sortByOrderLineId);

        // remove any deleted lines
        for (Iterator<OrderLineDTO> it = oldOrderLines.iterator(); it.hasNext();) {
            if (it.next().getDeleted() != 0) {
                it.remove();
            }
        }
        for (Iterator<OrderLineDTO> it = newOrderLines.iterator(); it.hasNext();) {
            if (it.next().getDeleted() != 0) {
                it.remove();
            }
        }

        Iterator<OrderLineDTO> itOldLines = oldOrderLines.iterator();
        Iterator<OrderLineDTO> itNewLines = newOrderLines.iterator();

        // Step through the sorted order lines, checking if it exists only in 
        // one, the other or both. If both, then check if quantity has changed.
        OrderLineDTO currentOldLine = itOldLines.hasNext() ? itOldLines.next() : null;
        OrderLineDTO currentNewLine = itNewLines.hasNext() ? itNewLines.next() : null;
        while (currentOldLine != null && currentNewLine != null) {
            int oldLineId = currentOldLine.getId();
            int newLineId = currentNewLine.getId();
            if (oldLineId < newLineId) {
                // order line has been deleted
                LOG.debug("Deleted order line. Order line Id: " + oldLineId);
                retValue.add(new NewQuantityEvent(entityId, currentOldLine.getQuantity(), BigDecimal.ZERO,
                                                  orderId, currentOldLine, null));
                currentOldLine = itOldLines.hasNext() ? itOldLines.next() : null;
            } else if (oldLineId > newLineId) {
                // order line has been added
                LOG.debug("Added order line. Order line Id: " + newLineId);
                retValue.add(new NewQuantityEvent(entityId, BigDecimal.ZERO, currentNewLine.getQuantity(),
                                                  orderId, currentNewLine, null));
                currentNewLine = itNewLines.hasNext() ? itNewLines.next() : null;
            } else {
                // order line exists in both, so check quantity
                BigDecimal oldLineQuantity = currentOldLine.getQuantity();
                BigDecimal newLineQuantity = currentNewLine.getQuantity();
                if (oldLineQuantity.compareTo(newLineQuantity) != 0) {
                    LOG.debug("Order line quantity changed. Order line Id: " +
                              oldLineId);
                    retValue.add(new NewQuantityEvent(entityId, oldLineQuantity, newLineQuantity, orderId,
                                                      currentOldLine, currentNewLine));
                }
                currentOldLine = itOldLines.hasNext() ? itOldLines.next() : null;
                currentNewLine = itNewLines.hasNext() ? itNewLines.next() : null;
            }
        }
        // check for any remaining item lines that must have been deleted or added
        while (currentOldLine != null) {
            LOG.debug("Deleted order line. Order line id: " + currentOldLine.getId());
            retValue.add(new NewQuantityEvent(entityId, currentOldLine.getQuantity(), BigDecimal.ZERO, orderId,
                                              currentOldLine, null));
            currentOldLine = itOldLines.hasNext() ? itOldLines.next() : null;
        }
        while (currentNewLine != null) {
            LOG.debug("Added order line. Order line id: " + currentNewLine.getId());
            retValue.add(new NewQuantityEvent(entityId, BigDecimal.ZERO, currentNewLine.getQuantity(), orderId,
                                              currentNewLine, null));
            currentNewLine = itNewLines.hasNext() ? itNewLines.next() : null;
        }

        if (sendEvents) {
            for (NewQuantityEvent event: retValue) {
                EventManager.process(event);
            }
        }

        return retValue;
    }

    public void update(Integer executorId, OrderDTO dto) {
        // update first the order own fields
        if (!Util.equal(order.getActiveUntil(), dto.getActiveUntil())) {
            updateActiveUntil(executorId, dto.getActiveUntil(), dto);
        }
        if (!Util.equal(order.getActiveSince(), dto.getActiveSince())) {
            audit(executorId, order.getActiveSince());
            order.setActiveSince(dto.getActiveSince());
        }
        setStatus(executorId, dto.getStatusId());

        if (order.getOrderPeriod().getId() != dto.getOrderPeriod().getId()) {
            audit(executorId, order.getOrderPeriod().getId());
            order.setOrderPeriod(orderPeriodDAS.find(dto.getOrderPeriod().getId()));
        }

        // set the provisioning status
        for (OrderLineDTO line : dto.getLines()) {
            // set default provisioning status id for order lines
            if (line.getProvisioningStatus() == null) {
                line.setProvisioningStatus(provisioningStatusDas.find(
                        Constants.PROVISIONING_STATUS_INACTIVE));
            } else {
                line.setProvisioningStatus(provisioningStatusDas.find(
                        line.getProvisioningStatus().getId()));
            }
        }

        // this should not be necessary any more, since the order is a pojo...
        order.setOrderBillingType(dto.getOrderBillingType());
        order.setNotify(dto.getNotify());
        order.setDueDateUnitId(dto.getDueDateUnitId());
        order.setDueDateValue(dto.getDueDateValue());
        order.setDfFm(dto.getDfFm());
        order.setAnticipatePeriods(dto.getAnticipatePeriods());
        order.setOwnInvoice(dto.getOwnInvoice());
        order.setNotes(dto.getNotes());
        order.setNotesInInvoice(dto.getNotesInInvoice());
        order.setCycleStarts(dto.getCycleStarts());
        if (dto.getIsCurrent() != null && dto.getIsCurrent().intValue() == 1) {
            setMainSubscription(executorId);
        }
        if (dto.getIsCurrent() != null && dto.getIsCurrent().intValue() == 0) {
            unsetMainSubscription(executorId);
        }
        // this one needs more to get updated
        updateNextBillableDay(executorId, dto.getNextBillableDay());

        /*
         *  now proces the order lines
         */

        // get new quantity events as necessary
        List<NewQuantityEvent> events = checkOrderLineQuantities(order.getLines(), dto.getLines(),
                                                                 order.getBaseUserByUserId().getCompany().getId(),
                                                                 order.getId(), false); // do not send them now, it will be done later when the order is saved


        // Determine if the item of the order changes and, if it is,
        // LOG a subscription change event.
        LOG.info("Order lines: " + order.getLines().size() + "  --> new Order: " + dto.getLines().size());

        OrderLineDTO oldLine = null;
        int nonDeletedLines = 0;
        if (dto.getLines().size() == 1 && order.getLines().size() >= 1) {
            // This event needs to LOG the old item id and description, so
            // it can only happen when updating orders with only one line.

            for (Iterator i = order.getLines().iterator(); i.hasNext();) {
                // Check which order is not deleted.
                OrderLineDTO temp = (OrderLineDTO) i.next();
                if (temp.getDeleted() == 0) {
                    oldLine = temp;
                    nonDeletedLines++;
                }
            }
        }

        // now update this order's lines
        order.getLines().clear();
        order.getLines().addAll(dto.getLines());
        for (OrderLineDTO line : order.getLines()) {
            // link them all, just in case there's a new one
            line.setPurchaseOrder(order);
            // new lines need createDatetime set
            line.setDefaults();
        }

        order = orderDas.save(order);

        if (oldLine != null && nonDeletedLines == 1) {
            OrderLineDTO newLine = null;
            for (Iterator i = order.getLines().iterator(); i.hasNext();) {
                OrderLineDTO temp = (OrderLineDTO) i.next();
                if (temp.getDeleted() == 0) {
                    newLine = temp;
                }
            }
            if (newLine != null && !oldLine.getItemId().equals(newLine.getItemId())) {
                if (executorId != null) {
                    eLogger.audit(executorId,
                                  order.getBaseUserByUserId().getId(),
                                  Constants.TABLE_ORDER_LINE,
                                  newLine.getId(), EventLogger.MODULE_ORDER_MAINTENANCE,
                                  EventLogger.ORDER_LINE_UPDATED, oldLine.getId(),
                                  oldLine.getDescription(),
                                  null);
                } else {
                    // it is the mediation process
                    eLogger.auditBySystem(order.getBaseUserByUserId().getCompany().getId(),
                                          order.getBaseUserByUserId().getId(),
                                          Constants.TABLE_ORDER_LINE,
                                          newLine.getId(), EventLogger.MODULE_ORDER_MAINTENANCE,
                                          EventLogger.ORDER_LINE_UPDATED, oldLine.getId(),
                                          oldLine.getDescription(),
                                          null);
                }
            }
        }

        if (executorId != null) {
            eLogger.audit(executorId, order.getBaseUserByUserId().getId(),
                          Constants.TABLE_PUCHASE_ORDER, order.getId(),
                          EventLogger.MODULE_ORDER_MAINTENANCE,
                          EventLogger.ROW_UPDATED, null,
                          null, null);
        } else {
            eLogger.auditBySystem(order.getBaseUserByUserId().getCompany().getId(),
                                  order.getBaseUserByUserId().getId(),
                                  Constants.TABLE_PUCHASE_ORDER,
                                  order.getId(),
                                  EventLogger.MODULE_ORDER_MAINTENANCE,
                                  EventLogger.ROW_UPDATED, null,
                                  null, null);
        }

        // last, once the order is saved and all done, send out the 
        // order modified events
        for (NewQuantityEvent event: events) {
            EventManager.process(event);
        }

    }

    private void updateEndOfOrderProcess(Date newDate) {
        OrderProcessDTO process = null;
        if (newDate == null) {
            LOG.debug("Attempting to update an order process end date to null. Skipping");
            return;
        }
        if (order.getActiveUntil() != null) {
            process = orderDas.findProcessByEndDate(order.getId(),
                                                    order.getActiveUntil());
        }
        if (process != null) {
            LOG.debug("Updating process id " + process.getId());
            process.setPeriodEnd(newDate);

        } else {
            LOG.debug("Did not find any process for order " + order.getId() +
                      " and date " + order.getActiveUntil());
        }
    }

    private void updateNextBillableDay(Integer executorId, Date newDate) {
        if (newDate == null) {
            return;
        }
        // only if the new date is in the future
        if (order.getNextBillableDay() == null ||
            newDate.after(order.getNextBillableDay())) {
            // this audit can be added to the order details screen
            // otherwise the user can't account for the lost time
            eLogger.audit(executorId, order.getBaseUserByUserId().getId(),
                          Constants.TABLE_PUCHASE_ORDER, order.getId(),
                          EventLogger.MODULE_ORDER_MAINTENANCE,
                          EventLogger.ORDER_NEXT_BILL_DATE_UPDATED, null,
                          null, order.getNextBillableDay());
            // update the period of the latest invoice as well
            updateEndOfOrderProcess(newDate);
            // do the actual update
            order.setNextBillableDay(newDate);
        } else {
            LOG.info("order " + order.getId() +
                     " next billable day not updated from " +
                     order.getNextBillableDay() + " to " + newDate);
        }
    }

    /**
     * Method lookUpEditable.
     * Gets the row from order_line_type for the type specifed
     * @param type
     * The order line type to look.
     * @return Boolean
     * If it is editable or not
     * @throws SessionInternalError
     * If there was a problem accessing the entity bean
     */
    static public Boolean lookUpEditable(Integer type)
            throws SessionInternalError {
        Boolean editable = null;

        try {
            OrderLineTypeDAS das = new OrderLineTypeDAS();
            OrderLineTypeDTO typeBean = das.find(type);

            editable = new Boolean(typeBean.getEditable().intValue() == 1);
        } catch (Exception e) {
            LOG.fatal(
                    "Exception looking up the editable flag of an order " + "line type. Type = " + type,
                    e);
            throw new SessionInternalError("Looking up editable flag");
        }

        return editable;
    }

    public CachedRowSet getList(Integer entityID, Integer userRole,
                                Integer userId)
            throws SQLException, Exception {

        if (userRole.equals(Constants.TYPE_INTERNAL) ||
            userRole.equals(Constants.TYPE_ROOT) ||
            userRole.equals(Constants.TYPE_CLERK)) {
            prepareStatement(OrderSQL.listInternal);
            cachedResults.setInt(1, entityID.intValue());
        } else if (userRole.equals(Constants.TYPE_PARTNER)) {
            prepareStatement(OrderSQL.listPartner);
            cachedResults.setInt(1, entityID.intValue());
            cachedResults.setInt(2, userId.intValue());
        } else if (userRole.equals(Constants.TYPE_CUSTOMER)) {
            prepareStatement(OrderSQL.listCustomer);
            cachedResults.setInt(1, userId.intValue());
        } else {
            throw new Exception("The orders list for the type " + userRole +
                                " is not supported");
        }

        execute();
        conn.close();
        return cachedResults;
    }

    public Integer getLatest(Integer userId)
            throws SessionInternalError {
        Integer retValue = null;
        try {
            prepareStatement(OrderSQL.getLatest);
            cachedResults.setInt(1, userId.intValue());
            execute();
            if (cachedResults.next()) {
                int value = cachedResults.getInt(1);
                if (!cachedResults.wasNull()) {
                    retValue = new Integer(value);
                }
            }
            cachedResults.close();
            conn.close();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }

        return retValue;
    }

    public Integer getLatestByItemType(Integer userId, Integer itemTypeId)
            throws SessionInternalError {
        Integer retValue = null;
        try {
            prepareStatement(OrderSQL.getLatestByItemType);
            cachedResults.setInt(1, userId.intValue());
            cachedResults.setInt(2, itemTypeId.intValue());
            execute();
            if (cachedResults.next()) {
                int value = cachedResults.getInt(1);
                if (!cachedResults.wasNull()) {
                    retValue = new Integer(value);
                }
            }
            cachedResults.close();
            conn.close();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }

        return retValue;
    }

    public CachedRowSet getOrdersByProcessId(Integer processId)
            throws SQLException, Exception {

        prepareStatement(OrderSQL.listByProcess);
        cachedResults.setInt(1, processId.intValue());
        execute();
        conn.close();
        return cachedResults;
    }

    public List<Integer> getOrdersByProcess(Integer processId) throws SQLException, Exception {
        conn = ((DataSource) Context.getBean(Context.Name.DATA_SOURCE)).getConnection();
        PreparedStatement stmt = conn.prepareStatement(OrderSQL.listByProcess);
        stmt.setInt(1, processId.intValue());
        ResultSet res = stmt.executeQuery();
        List<Integer> orders=new ArrayList();
        while (res.next()) {
            orders.add(res.getInt(1));
        }
        res.close();
        conn.close();
        return orders;
    }

    public void setStatus(Integer executorId, Integer statusId) {
        if (statusId == null || order.getStatusId().equals(statusId)) {
            return;
        }
        if (executorId != null) {
            eLogger.audit(executorId, order.getBaseUserByUserId().getId(),
                          Constants.TABLE_PUCHASE_ORDER, order.getId(),
                          EventLogger.MODULE_ORDER_MAINTENANCE,
                          EventLogger.ORDER_STATUS_CHANGE,
                          order.getStatusId(), null, null);
        } else {
            eLogger.auditBySystem(order.getBaseUserByUserId().getCompany().getId(),
                                  order.getBaseUserByUserId().getId(),
                                  Constants.TABLE_PUCHASE_ORDER,
                                  order.getId(),
                                  EventLogger.MODULE_ORDER_MAINTENANCE,
                                  EventLogger.ORDER_STATUS_CHANGE,
                                  order.getStatusId(), null, null);

        }
        NewStatusEvent event = new NewStatusEvent(
                order.getId(), order.getStatusId(), statusId);
        EventManager.process(event);
        order.setOrderStatus(new OrderStatusDAS().find(statusId));

    }

    /**
     * To be called from the http api, this simply looks for lines
     * in the order that lack some fields, it finds that info based
     * in the item. 
     *
     * @param dto order with lines to pricess
     * @param entityId entity id
     */
    public void fillInLines(OrderDTO dto, Integer entityId) throws NamingException, SessionInternalError {
        ItemBL itemBl = new ItemBL();

        // iterate over order lines
        for (OrderLineDTO line : dto.getLines()) {
            itemBl.set(line.getItemId());
            Integer languageId = itemBl.getEntity().getEntity().getLanguageId();

            // populate the basic item price and item description
            ItemDAS itemDas = new ItemDAS();
            line.setItem(itemDas.find(line.getItemId()));
            if (line.getPrice() == null) {
                line.setPrice(itemBl.getPrice(dto.getUserId(), dto.getCurrencyId(), line.getQuantity(), entityId));
            }
            if (line.getDescription() == null) {
                line.setDescription(itemBl.getEntity().getDescription(languageId));
            }
        }
    }

    private void audit(Integer executorId, Date date) {
        eLogger.audit(executorId, order.getBaseUserByUserId().getId(),
                      Constants.TABLE_PUCHASE_ORDER, order.getId(),
                      EventLogger.MODULE_ORDER_MAINTENANCE,
                      EventLogger.ROW_UPDATED, null,
                      null, date);
    }

    private void audit(Integer executorId, Integer in) {
        eLogger.audit(executorId, order.getBaseUserByUserId().getId(),
                      Constants.TABLE_PUCHASE_ORDER, order.getId(),
                      EventLogger.MODULE_ORDER_MAINTENANCE,
                      EventLogger.ROW_UPDATED, in,
                      null, null);
    }

    public static boolean validate(OrderWS dto) {
        boolean retValue = true;

        if (dto.getUserId() == null || dto.getPeriod() == null ||
            dto.getBillingTypeId() == null ||
            dto.getOrderLines() == null) {
            retValue = false;
        } else {
            for (int f = 0; f < dto.getOrderLines().length; f++) {
                if (!validate(dto.getOrderLines()[f])) {
                    retValue = false;
                    break;
                }
            }
        }
        return retValue;
    }

    public static boolean validate(OrderLineWS dto) {
        boolean retValue = true;

        if (dto.getTypeId() == null ||
            dto.getDescription() == null || dto.getQuantity() == null) {
            retValue = false;
        }

        return retValue;
    }

    public void reviewNotifications(Date today)
            throws NamingException, SQLException, Exception {
        INotificationSessionBean notificationSess = (INotificationSessionBean) Context.getBean(Context.Name.NOTIFICATION_SESSION);

        for (CompanyDTO ent : new CompanyDAS().findEntities()) {
            // find the orders for this entity

            // SQL args
            Object[] sqlArgs = new Object[4];
            sqlArgs[0] = new java.sql.Date(today.getTime());

            // calculate the until date

            // get the this entity preferences for each of the steps
            PreferenceBL pref = new PreferenceBL();
            int totalSteps = 3;
            int stepDays[] = new int[totalSteps];
            boolean config = false;
            int minStep = -1;
            for (int f = 0; f < totalSteps; f++) {
                try {
                    pref.set(ent.getId(), new Integer(
                            Constants.PREFERENCE_DAYS_ORDER_NOTIFICATION_S1.intValue() +
                            f));
                    if (pref.isNull()) {
                        stepDays[f] = -1;
                    } else {
                        stepDays[f] = pref.getInt();
                        config = true;
                        if (minStep == -1) {
                            minStep = f;
                        }
                    }
                } catch (EmptyResultDataAccessException e) {
                    stepDays[f] = -1;
                }
            }

            if (!config) {
                LOG.warn("Preference missing to send a notification for " +
                         "entity " + ent.getId());
                continue;
            }

            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.setTime(today);
            cal.add(Calendar.DAY_OF_MONTH, stepDays[minStep]);
            sqlArgs[1] = new java.sql.Date(cal.getTime().getTime());

            // the entity
            sqlArgs[2] = ent.getId();
            // the total number of steps
            sqlArgs[3] = totalSteps;

            JdbcTemplate jdbcTemplate = (JdbcTemplate) Context.getBean(
                    Context.Name.JDBC_TEMPLATE);

            SqlRowSet results = jdbcTemplate.queryForRowSet(
                    OrderSQL.getAboutToExpire, sqlArgs);
            while (results.next()) {
                int orderId = results.getInt(1);
                Date activeUntil = results.getDate(2);
                int currentStep = results.getInt(3);
                int days = -1;

                // find out how many days apply for this order step
                for (int f = currentStep; f < totalSteps; f++) {
                    if (stepDays[f] >= 0) {
                        days = stepDays[f];
                        currentStep = f + 1;
                        break;
                    }
                }

                if (days == -1) {
                    throw new SessionInternalError("There are no more steps " +
                                                   "configured, but the order was selected. Order " +
                                                   " id = " + orderId);
                }

                // check that this order requires a notification
                cal.setTime(today);
                cal.add(Calendar.DAY_OF_MONTH, days);
                if (activeUntil.compareTo(today) >= 0 &&
                    activeUntil.compareTo(cal.getTime()) <= 0) {
                    /*/ ok
                    LOG.debug("Selecting order " + orderId + " today = " + 
                    today + " active unitl = " + activeUntil +
                    " days = " + days);
                     */
                } else {
                    /*
                    LOG.debug("Skipping order " + orderId + " today = " + 
                    today + " active unitl = " + activeUntil +
                    " days = " + days);
                     */
                    continue;
                }

                set(new Integer(orderId));
                UserBL user = new UserBL(order.getBaseUserByUserId().getId());
                try {
                    NotificationBL notification = new NotificationBL();
                    ContactBL contact = new ContactBL();
                    contact.set(user.getEntity().getUserId());
                    MessageDTO message = notification.getOrderNotification(
                            ent.getId(),
                            new Integer(currentStep),
                            user.getEntity().getLanguageIdField(),
                            order.getActiveSince(),
                            order.getActiveUntil(),
                            user.getEntity().getUserId(),
                            order.getTotal(), order.getCurrencyId());
                    // update the order record only if the message is sent 
                    if (notificationSess.notify(user.getEntity(), message).
                            booleanValue()) {
                        // if in the last step, turn the notification off, so
                        // it is skiped in the next process
                        if (currentStep >= totalSteps) {
                            order.setNotify(new Integer(0));
                        }
                        order.setNotificationStep(new Integer(currentStep));
                        order.setLastNotified(Calendar.getInstance().getTime());
                    }

                } catch (NotificationNotFoundException e) {
                    LOG.warn("Without a message to send, this entity can't" +
                             " notify about orders. Skipping");
                    break;
                }

            }
        }
    }

    public TimePeriod getDueDate() {
        TimePeriod retValue = new TimePeriod();
        if (order.getDueDateValue() == null) {
            // let's go see the customer

            if (order.getBaseUserByUserId().getCustomer().getDueDateValue() == null) {
                // still unset, let's go to the entity
                ConfigurationBL config = new ConfigurationBL(
                        order.getBaseUserByUserId().getCompany().getId());
                retValue.setUnitId(config.getEntity().getDueDateUnitId());
                retValue.setValue(config.getEntity().getDueDateValue());
            } else {
                retValue.setUnitId(order.getBaseUserByUserId().getCustomer().getDueDateUnitId());
                retValue.setValue(order.getBaseUserByUserId().getCustomer().getDueDateValue());
            }
        } else {
            retValue.setUnitId(order.getDueDateUnitId());
            retValue.setValue(order.getDueDateValue());
        }

        // df fm only applies if the entity uses it
        PreferenceBL preference = new PreferenceBL();
        try {
            preference.set(order.getUser().getEntity().getId(),
                           Constants.PREFERENCE_USE_DF_FM);
        } catch (EmptyResultDataAccessException e) {
            // no problem go ahead use the defualts
        }
        if (preference.getInt() == 1) {
            // now all over again for the Df Fm
            if (order.getDfFm() == null) {
                // let's go see the customer
                if (order.getUser().getCustomer().getDfFm() == null) {
                    // still unset, let's go to the entity
                    ConfigurationBL config = new ConfigurationBL(
                            order.getUser().getEntity().getId());
                    retValue.setDf_fm(config.getEntity().getDfFm());
                } else {
                    retValue.setDf_fm(order.getUser().getCustomer().getDfFm());
                }
            } else {
                retValue.setDf_fm(order.getDfFm());
            }
        } else {
            retValue.setDf_fm((Boolean) null);
        }

        retValue.setOwn_invoice(order.getOwnInvoice());

        return retValue;
    }

    public Date getInvoicingDate() {
        Date retValue;
        if (order.getNextBillableDay() != null) {
            retValue = order.getNextBillableDay();
        } else {
            if (order.getActiveSince() != null) {
                retValue = order.getActiveSince();
            } else {
                retValue = order.getCreateDate();
            }
        }

        return retValue;
    }

    public boolean isDateInvoiced(Date date) {
        return date != null && order.getNextBillableDay() != null &&
               date.before(order.getNextBillableDay());
    }

    public Integer[] getListIds(Integer userId, Integer number, Integer entityId) {

        List<Integer> result = orderDas.findIdsByUserLatestFirst(userId, number);
        return result.toArray(new Integer[result.size()]);
    }

    public Integer[] getListIdsByItemType(Integer userId, Integer itemTypeId, Integer number) {

        List<Integer> result = orderDas.findIdsByUserAndItemTypeLatestFirst(userId, itemTypeId, number);
        return result.toArray(new Integer[result.size()]);
    }

    public Integer[] getByUserAndPeriod(Integer userId, Integer statusId)
            throws SessionInternalError {
        // find the order records first
        try {
            List result = new ArrayList();
            prepareStatement(OrderSQL.getByUserAndPeriod);
            cachedResults.setInt(1, userId.intValue());
            cachedResults.setInt(2, statusId.intValue());
            execute();
            while (cachedResults.next()) {
                result.add(new Integer(cachedResults.getInt(1)));
            }
            cachedResults.close();
            conn.close();
            // now convert the vector to an int array
            Integer retValue[] = new Integer[result.size()];
            result.toArray(retValue);

            return retValue;
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    public Collection<OrderDTO> getActiveRecurringByUser(Integer userId) {
        return orderDas.findByUserSubscriptions(userId);
    }

    public OrderPeriodDTO[] getPeriods(Integer entityId, Integer languageId) {
        OrderPeriodDTO retValue[] = null;
        CompanyDAS companyDas = new CompanyDAS();
        CompanyDTO company = companyDas.find(entityId);

        Set<OrderPeriodDTO> periods = company.getOrderPeriods();
        if (periods == null || periods.size() == 0) {
            return new OrderPeriodDTO[0];
        }

        retValue = new OrderPeriodDTO[periods.size()];
        int i = 0;
        for (OrderPeriodDTO period : periods) {
            period.setDescription(period.getDescription(languageId));
            retValue[i++] = period;
        }
        return retValue;
    }

    public void updatePeriods(Integer languageId, OrderPeriodDTO periods[]) {
        for (OrderPeriodDTO period : periods) {
            orderPeriodDAS.save(period).setDescription(
                    period.getDescription(), languageId);
            period.getCompany().getOrderPeriods().add(period);
        }
    }

    public void addPeriod(Integer entityId, Integer languageId) {
        OrderPeriodDTO newPeriod = new OrderPeriodDTO();
        CompanyDAS companyDas = new CompanyDAS();
        newPeriod.setCompany(companyDas.find(entityId));
        PeriodUnitDAS periodDas = new PeriodUnitDAS();
        newPeriod.setPeriodUnit(periodDas.find(1));
        newPeriod.setValue(1);
        newPeriod = orderPeriodDAS.save(newPeriod);
        newPeriod.setDescription(" ", languageId);
    }

    public boolean deletePeriod(Integer periodId) {
        OrderPeriodDTO period = orderPeriodDAS.find(
                periodId);
        if (period.getPurchaseOrders().size() > 0) {
            return false;
        } else {
            orderPeriodDAS.delete(period);
            return true;
        }
    }

    public OrderLineWS getOrderLineWS(Integer id) {
        OrderLineDTO line = orderLineDAS.findNow(id);
        if (line == null) {
            LOG.warn("Order line " + id + " not found");
            return null;
        }
        OrderLineWS retValue = new OrderLineWS(line.getId(), line.getItem().getId(), line.getDescription(),
                                               line.getAmount(), line.getQuantity(), line.getPrice() == null ? null : line.getPrice(), line.getCreateDatetime(),
                                               line.getDeleted(), line.getOrderLineType().getId(), line.getEditable(),
                                               line.getPurchaseOrder().getId(), line.getUseItem(), line.getVersionNum(), line.getProvisioningStatusId(), line.getProvisioningRequestId());
        return retValue;
    }

    public OrderLineDTO getOrderLine(Integer id) {
        OrderLineDTO line = orderLineDAS.findNow(id);
        if (line == null) {
            throw new SessionInternalError("Order line " + id + " not found");
        }
        return line;
    }

    public OrderLineDTO getOrderLine(OrderLineWS ws) {
        OrderLineDTO dto = new OrderLineDTO();
        dto.setId(ws.getId());
        dto.setAmount(ws.getAmountAsDecimal());
        dto.setCreateDatetime(ws.getCreateDatetime());
        dto.setDeleted(ws.getDeleted());
        dto.setUseItem(ws.getUseItem());
        dto.setDescription(ws.getDescription());
        dto.setEditable(ws.getEditable());
        dto.setItem(new ItemDAS().find(ws.getItemId()));
        dto.setItemId(ws.getItemId());
        dto.setOrderLineType(new OrderLineTypeDAS().find(ws.getTypeId()));
        dto.setPrice(ws.getPriceAsDecimal());
        dto.setPurchaseOrder(orderDas.find(ws.getOrderId()));
        dto.setQuantity(ws.getQuantityAsDecimal());
        dto.setVersionNum(ws.getVersionNum());
        dto.setProvisioningStatus(provisioningStatusDas.find(ws.getProvisioningStatusId()));
        dto.setProvisioningRequestId(ws.getProvisioningRequestId());

        return dto;
    }

    public List<OrderLineDTO> getRecurringOrderLines(Integer userId) {
        return orderLineDAS.findRecurringByUser(userId);
    }

    public OrderLineDTO getRecurringOrderLine(Integer userId, Integer itemId) {
        return orderLineDAS.findRecurringByUserItem(userId, itemId);
    }

    public List<OrderLineDTO> getOnetimeOrderLines(Integer userId, Integer itemId) {
        return orderLineDAS.findOnetimeByUserItem(userId, itemId);
    }

    public List<OrderLineDTO> getOnetimeOrderLines(Integer userId, Integer itemId, Integer months) {
        return orderLineDAS.findOnetimeByUserItem(userId, itemId, months);
    }

    public List<OrderLineDTO> getOnetimeOrderLinesByParent(Integer parentUserId, Integer itemId, Integer months) {
        return orderLineDAS.findOnetimeByParentUserItem(parentUserId, itemId, months);
    }

    public void updateOrderLine(OrderLineWS dto) {
        OrderLineDTO line = getOrderLine(dto.getId());
        if (dto.getQuantity() != null && (BigDecimal.ZERO.compareTo(dto.getQuantityAsDecimal()) == 0)) {
            // deletes the order line if the quantity is 0
            orderLineDAS.delete(line);

        } else {
            line.setAmount(dto.getAmountAsDecimal());
            line.setDeleted(dto.getDeleted());
            line.setDescription(dto.getDescription());
            ItemDAS item = new ItemDAS();
            line.setItem(item.find(dto.getItemId()));
            line.setPrice(dto.getPriceAsDecimal());
            line.setQuantity(dto.getQuantityAsDecimal());
            line.setProvisioningStatus(provisioningStatusDas.find(
                    dto.getProvisioningStatusId()));
            line.setProvisioningRequestId(dto.getProvisioningRequestId());
        }
    }

    /**
     * Returns the current one-time order for this user for the given date.
     */
    public OrderDTO getCurrentOrder(Integer userId, Date date) {
        CurrentOrder co = new CurrentOrder(userId, date);
        set(orderDas.findNow(co.getCurrent()));
        return order;
    }

    /**
     * For the mediation process, get or create a current order. The returned
     * order is not attached to the session.
     *
     * @param userId
     * @param eventDate
     * @param currencyId
     * @return
     */
    public static OrderDTO getOrCreateCurrentOrder(Integer userId, Date eventDate,
                                                   Integer currencyId, boolean persist) {
        CurrentOrder co = new CurrentOrder(userId, eventDate);

        Integer currentOrderId = co.getCurrent();
        if (currentOrderId == null) {
            // this is almost an error, put them in a new order?
            currentOrderId = co.create(eventDate, currencyId, new UserBL().getEntityId(userId));
            LOG.warn("Created current one-time order without a suitable main " +
                     "subscription order:" + currentOrderId);
        }

        OrderDAS orderDas = new OrderDAS();
        OrderDTO order = orderDas.find(currentOrderId);

        if (!persist) {
            order.touch();
            orderDas.detach(order);
        }

        return order;
    }

    /**
     * The order has to be set and made persitent with an ID
     * @param executorId
     */
    public void setMainSubscription(Integer executorId) {
        // set the field
        order.setIsCurrent(1);
        // there can only be one main subscription order
        Integer oldCurrent = order.getUser().getCustomer().getCurrentOrderId();
        if (oldCurrent == null || !oldCurrent.equals(order.getId())) {
            eLogger.audit(executorId, order.getBaseUserByUserId().getId(),
                          Constants.TABLE_PUCHASE_ORDER, order.getId(),
                          EventLogger.MODULE_ORDER_MAINTENANCE,
                          EventLogger.ORDER_MAIN_SUBSCRIPTION_UPDATED, oldCurrent,
                          null, null);
            // this is the new subscription order
            order.getUser().getCustomer().setCurrentOrderId(order.getId());
            // if there was an old one
            if (oldCurrent != null) {
                // update so it does not have the 'is subscription' flag on
                OrderDTO old = orderDas.findNow(oldCurrent);
                if (old == null) {
                    LOG.error("Can not find order to set 'is_current' off " + oldCurrent);
                } else {
                    old.setIsCurrent(new Integer(0));
                }
            }
        }
    }

    private void unsetMainSubscription(Integer executorId) {
        if (order.getIsCurrent() == null) {
            order.setIsCurrent(0);
        }
        // it is removing the main subscription
        if (order.getIsCurrent().intValue() == 1) {
            eLogger.audit(executorId, order.getBaseUserByUserId().getId(),
                          Constants.TABLE_PUCHASE_ORDER, order.getId(),
                          EventLogger.MODULE_ORDER_MAINTENANCE,
                          EventLogger.ORDER_MAIN_SUBSCRIPTION_UPDATED, null,
                          null, null);
            // this is the new subscription order
            order.getUser().getCustomer().setCurrentOrderId(null);
            order.setIsCurrent(0);
        }
    }

    public Integer getMainOrderId(Integer userId) {
        UserDTO user = new UserDAS().find(userId);
        if (user.getCustomer() != null)
            return user.getCustomer().getCurrentOrderId();

        return null;
    }

    public void addRelationships(Integer userId, Integer periodId, Integer currencyId) {
        if (periodId != null) {
            OrderPeriodDTO period = orderPeriodDAS.find(periodId);
            order.setOrderPeriod(period);
        }
        if (userId != null) {
            UserDAS das = new UserDAS();
            order.setBaseUserByUserId(das.find(userId));
        }
        if (currencyId != null) {
            CurrencyDAS das = new CurrencyDAS();
            order.setCurrency(das.find(currencyId));
        }
    }

    public OrderDTO getDTO(OrderWS other) {
        OrderDTO retValue = new OrderDTO();
        retValue.setId(other.getId());

        retValue.setBaseUserByUserId(new UserDAS().find(other.getUserId()));
        retValue.setBaseUserByCreatedBy(new UserDAS().find(other.getCreatedBy()));
        retValue.setCurrency(new CurrencyDAS().find(other.getCurrencyId()));
        retValue.setOrderStatus(new OrderStatusDAS().find(other.getStatusId()));
        retValue.setOrderPeriod(new OrderPeriodDAS().find(other.getPeriod()));
        retValue.setOrderBillingType(new OrderBillingTypeDAS().find(other.getBillingTypeId()));
        retValue.setActiveSince(other.getActiveSince());
        retValue.setActiveUntil(other.getActiveUntil());
        retValue.setCreateDate(other.getCreateDate());
        retValue.setNextBillableDay(other.getNextBillableDay());
        retValue.setDeleted(other.getDeleted());
        retValue.setNotify(other.getNotify());
        retValue.setLastNotified(other.getLastNotified());
        retValue.setNotificationStep(other.getNotificationStep());
        retValue.setDueDateUnitId(other.getDueDateUnitId());
        retValue.setDueDateValue(other.getDueDateValue());
        retValue.setDfFm(other.getDfFm());
        retValue.setAnticipatePeriods(other.getAnticipatePeriods());
        retValue.setOwnInvoice(other.getOwnInvoice());
        retValue.setNotes(other.getNotes());
        retValue.setNotesInInvoice(other.getNotesInInvoice());

        for (OrderLineWS line : other.getOrderLines()) {
            if (line != null) {
                retValue.getLines().add(getOrderLine(line));
            }
        }

        retValue.setIsCurrent(other.getIsCurrent());
        retValue.setCycleStarts(other.getCycleStarts());
        retValue.setVersionNum(other.getVersionNum());

        if (other.getPricingFields() != null) {
            List<PricingField> pf = new ArrayList<PricingField>();
            pf.addAll(Arrays.asList(PricingField.getPricingFieldsValue(other.getPricingFields())));
            retValue.setPricingFields(pf);
        }

        return retValue;
    }

    public void setProvisioningStatus(Integer orderLineId, Integer provisioningStatus) {
        OrderLineDTO line = orderLineDAS.findForUpdate(orderLineId);
        Integer oldStatus = line.getProvisioningStatusId();
        line.setProvisioningStatus(provisioningStatusDas.find(provisioningStatus));
        LOG.debug("order line " + line + ": updated provisioning status :" + line.getProvisioningStatusId());

        // add a log for provisioning module
        eLogger.auditBySystem(order.getBaseUserByUserId().getCompany().getId(),
                              order.getBaseUserByUserId().getId(),
                              Constants.TABLE_ORDER_LINE, orderLineId,
                              EventLogger.MODULE_PROVISIONING, EventLogger.PROVISIONING_STATUS_CHANGE,
                              oldStatus, null, null);
    }

    public static OrderDTO createAsWithLine(OrderDTO order, Integer itemId, Double quantity) {
        return createAsWithLine(order, itemId, new BigDecimal(quantity).setScale(Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND));
    }

    public static OrderDTO createAsWithLine(OrderDTO order, Integer itemId, BigDecimal quantity) {
        // copy the current order
        OrderDTO newOrder = new OrderDTO(order);
        newOrder.setId(0);
        newOrder.setVersionNum(null);
        // the period needs to be in the session
        newOrder.setOrderPeriodId(order.getOrderPeriod().getId());
        // the status should be active
        newOrder.setOrderStatus(new OrderStatusDAS().find(Constants.ORDER_STATUS_ACTIVE));
        // but without the lines
        newOrder.getLines().clear();
        // but do get the new line in
        OrderLineBL.addItem(newOrder, itemId, quantity);

        return new OrderDAS().save(newOrder);
    }
}
