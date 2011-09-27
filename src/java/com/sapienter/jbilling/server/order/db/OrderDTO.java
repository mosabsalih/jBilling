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


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.sapienter.jbilling.server.invoice.db.InvoiceLineDTO;
import com.sapienter.jbilling.server.util.csv.Exportable;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OrderBy;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.invoice.InvoiceBL;
import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
import com.sapienter.jbilling.server.item.PricingField;
import com.sapienter.jbilling.server.process.db.BillingProcessDTO;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.Util;
import com.sapienter.jbilling.server.util.db.CurrencyDTO;
import java.util.ArrayList;

@Entity
@TableGenerator(
        name="purchase_order_GEN",
        table="jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue="purchase_order",
        allocationSize = 100
        )
@Table(name="purchase_order")
// No cache, mutable and critical
public class OrderDTO implements Serializable, Exportable {

    private static Logger LOG = Logger.getLogger(OrderDTO.class);

     private Integer id;
     private UserDTO baseUserByUserId;
     private UserDTO baseUserByCreatedBy;
     private CurrencyDTO currencyDTO;
     private OrderStatusDTO orderStatusDTO;
     private OrderPeriodDTO orderPeriodDTO;
     private OrderBillingTypeDTO orderBillingTypeDTO;
     private Date activeSince;
     private Date activeUntil;
     private Date cycleStarts;
     private Date createDate;
     private Date nextBillableDay;
     private int deleted;
     private Integer notify;
     private Date lastNotified;
     private Integer notificationStep;
     private Integer dueDateUnitId;
     private Integer dueDateValue;
     private Integer dfFm;
     private Integer anticipatePeriods;
     private Integer ownInvoice;
     private String notes;
     private Integer notesInInvoice;
     private Set<OrderProcessDTO> orderProcesses = new HashSet<OrderProcessDTO>(0);
     private List<OrderLineDTO> lines = new ArrayList<OrderLineDTO>(0);
     private Integer isCurrent;
     private Integer versionNum;
     // other non-persitent fields
     private Collection<OrderProcessDTO> nonReviewPeriods = new ArrayList<OrderProcessDTO>(0);
     private Collection<InvoiceDTO> invoices = new ArrayList<InvoiceDTO>(0);
     private Collection<BillingProcessDTO> billingProcesses = new ArrayList<BillingProcessDTO>(0);
     private String periodStr = null;
     private String billingTypeStr = null;
     private String statusStr = null;
     private String timeUnitStr = null;
     private String currencySymbol = null;
     private String currencyName = null;
     private BigDecimal total = null;
     private List<PricingField> pricingFields = null;

    public OrderDTO() {
    }
    
    public OrderDTO(OrderDTO other) {
        init(other);
    }

    public void init(OrderDTO other) {
        this.id = other.getId();
        this.baseUserByUserId = other.getBaseUserByUserId();
        this.baseUserByCreatedBy = other.getBaseUserByCreatedBy();
        this.currencyDTO = other.getCurrency();
        this.orderStatusDTO = other.getOrderStatus(); 
        this.orderPeriodDTO = other.getOrderPeriod();
        this.orderBillingTypeDTO = other.getOrderBillingType();
        this.activeSince = other.getActiveSince();
        this.activeUntil = other.getActiveUntil();
        this.createDate = other.getCreateDate();
        this.nextBillableDay = other.getNextBillableDay();
        this.deleted = other.getDeleted();
        this.notify = other.getNotify();
        this.lastNotified = other.getLastNotified();
        this.notificationStep = other.getNotificationStep();
        this.dueDateUnitId = other.getDueDateUnitId();
        this.dueDateValue = other.getDueDateValue();
        this.dfFm = other.getDfFm();
        this.anticipatePeriods = other.getAnticipatePeriods();
        this.ownInvoice = other.getOwnInvoice();
        this.notes = other.getNotes();
        this.notesInInvoice = other.getNotesInInvoice();
        this.orderProcesses.addAll(other.getOrderProcesses());
    for (OrderLineDTO line: other.getLines()) {
        this.lines.add(new OrderLineDTO(line));
    }
        this.isCurrent = other.getIsCurrent();
        this.versionNum = other.getVersionNum();
        this.cycleStarts = other.getCycleStarts();
        this.pricingFields = other.getPricingFields();
    }
    
    public OrderDTO(int id, UserDTO baseUserByCreatedBy, CurrencyDTO currencyDTO, OrderStatusDTO orderStatusDTO, OrderBillingTypeDTO orderBillingTypeDTO, Date createDatetime, Integer deleted) {
        this.id = id;
        this.baseUserByCreatedBy = baseUserByCreatedBy;
        this.currencyDTO = currencyDTO;
        this.orderStatusDTO = orderStatusDTO;
        this.orderBillingTypeDTO = orderBillingTypeDTO;
        this.createDate = createDatetime;
        this.deleted = deleted;
    }
    public OrderDTO(int id, UserDTO baseUserByUserId, UserDTO baseUserByCreatedBy, CurrencyDTO currencyDTO, 
            OrderStatusDTO orderStatusDTO, OrderPeriodDTO orderPeriodDTO, 
            OrderBillingTypeDTO orderBillingTypeDTO, Date activeSince, Date activeUntil, Date createDatetime, 
            Date nextBillableDay, Integer deleted, Integer notify, Date lastNotified, Integer notificationStep, 
            Integer dueDateUnitId, Integer dueDateValue, Integer dfFm, Integer anticipatePeriods, 
            Integer ownInvoice, String notes, Integer notesInInvoice, Set<OrderProcessDTO> orderProcesses, 
            List<OrderLineDTO> orderLineDTOs, Integer isCurrent) {
       this.id = id;
       this.baseUserByUserId = baseUserByUserId;
       this.baseUserByCreatedBy = baseUserByCreatedBy;
       this.currencyDTO = currencyDTO;
       this.orderStatusDTO = orderStatusDTO;
       this.orderPeriodDTO = orderPeriodDTO;
       this.orderBillingTypeDTO = orderBillingTypeDTO;
       this.activeSince = activeSince;
       this.activeUntil = activeUntil;
       this.createDate = createDatetime;
       this.nextBillableDay = nextBillableDay;
       this.deleted = deleted;
       this.notify = notify;
       this.lastNotified = lastNotified;
       this.notificationStep = notificationStep;
       this.dueDateUnitId = dueDateUnitId;
       this.dueDateValue = dueDateValue;
       this.dfFm = dfFm;
       this.anticipatePeriods = anticipatePeriods;
       this.ownInvoice = ownInvoice;
       this.notes = notes;
       this.notesInInvoice = notesInInvoice;
       this.orderProcesses = orderProcesses;
       this.lines = orderLineDTOs;
       this.isCurrent = isCurrent;
    }
   
    @Id @GeneratedValue(strategy=GenerationType.TABLE, generator="purchase_order_GEN")
    @Column(name="id", unique=true, nullable=false)
    public Integer getId() {
        return this.id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    public UserDTO getBaseUserByUserId() {
        return this.baseUserByUserId;
    }
    public void setBaseUserByUserId(UserDTO baseUserByUserId) {
        this.baseUserByUserId = baseUserByUserId;
    }
    
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="created_by")
    public UserDTO getBaseUserByCreatedBy() {
        return this.baseUserByCreatedBy;
    }
    
    public void setBaseUserByCreatedBy(UserDTO baseUserByCreatedBy) {
        this.baseUserByCreatedBy = baseUserByCreatedBy;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="currency_id", nullable=false)
    public CurrencyDTO getCurrency() {
        return this.currencyDTO;
    }
    
    public void setCurrency(CurrencyDTO currencyDTO) {
        this.currencyDTO = currencyDTO;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="status_id", nullable=false)
    public OrderStatusDTO getOrderStatus() {
        return this.orderStatusDTO;
    }
    
    public void setOrderStatus(OrderStatusDTO orderStatusDTO) {
        this.orderStatusDTO = orderStatusDTO;
    }
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="period_id")
    public OrderPeriodDTO getOrderPeriod() {
        return this.orderPeriodDTO;
    }
    public void setOrderPeriod(OrderPeriodDTO orderPeriodDTO) {
        this.orderPeriodDTO = orderPeriodDTO;
    }
    
    public void setOrderPeriodId(Integer id) {
        if (id != null) {
            setOrderPeriod(new OrderPeriodDAS().find(id));
        } else {
            setOrderPeriod(null);
        }
    }
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="billing_type_id", nullable=false)
    public OrderBillingTypeDTO getOrderBillingType() {
        return this.orderBillingTypeDTO;
    }
    
    public void setOrderBillingType(OrderBillingTypeDTO orderBillingTypeDTO) {
        this.orderBillingTypeDTO = orderBillingTypeDTO;
    }
    @Column(name="active_since", length=13)
    public Date getActiveSince() {
        return this.activeSince;
    }
    
    public void setActiveSince(Date activeSince) {
        this.activeSince = activeSince;
    }
    
    @Column(name="cycle_start")
    public Date getCycleStarts() {
        return cycleStarts;
    }

    public void setCycleStarts(Date cycleStarts) {
        this.cycleStarts = cycleStarts;
    }

    @Column(name="active_until", length=13)
    public Date getActiveUntil() {
        return this.activeUntil;
    }
    
    public void setActiveUntil(Date activeUntil) {
        this.activeUntil = activeUntil;
    }
    @Column(name="create_datetime", nullable=false, length=29)
    public Date getCreateDate() {
        return this.createDate;
    }
    
    public void setCreateDate(Date createDatetime) {
        this.createDate = createDatetime;
    }
    @Column(name="next_billable_day", length=29)
    public Date getNextBillableDay() {
        return this.nextBillableDay;
    }
    
    public void setNextBillableDay(Date nextBillableDay) {
        this.nextBillableDay = nextBillableDay;
    }
    
    @Column(name="deleted", nullable=false)
    public int getDeleted() {
        return this.deleted;
    }
    
    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }
    
    @Column(name="notify")
    public Integer getNotify() {
        return this.notify;
    }
    
    public void setNotify(Integer notify) {
        this.notify = notify;
    }
    @Column(name="last_notified", length=29)
    public Date getLastNotified() {
        return this.lastNotified;
    }
    
    public void setLastNotified(Date lastNotified) {
        this.lastNotified = lastNotified;
    }
    
    @Column(name="notification_step")
    public Integer getNotificationStep() {
        return this.notificationStep;
    }
    
    public void setNotificationStep(Integer notificationStep) {
        this.notificationStep = notificationStep;
    }
    
    @Column(name="due_date_unit_id")
    public Integer getDueDateUnitId() {
        return this.dueDateUnitId;
    }
    
    public void setDueDateUnitId(Integer dueDateUnitId) {
        this.dueDateUnitId = dueDateUnitId;
    }
    
    @Column(name="due_date_value")
    public Integer getDueDateValue() {
        return this.dueDateValue;
    }
    
    public void setDueDateValue(Integer dueDateValue) {
        this.dueDateValue = dueDateValue;
    }
    
    @Column(name="df_fm")
    public Integer getDfFm() {
        return this.dfFm;
    }
    
    public void setDfFm(Integer dfFm) {
        this.dfFm = dfFm;
    }
    
    @Column(name="anticipate_periods")
    public Integer getAnticipatePeriods() {
        return this.anticipatePeriods;
    }
    
    public void setAnticipatePeriods(Integer anticipatePeriods) {
        this.anticipatePeriods = anticipatePeriods;
    }
    
    @Column(name="own_invoice")
    public Integer getOwnInvoice() {
        return this.ownInvoice;
    }
    
    public void setOwnInvoice(Integer ownInvoice) {
        this.ownInvoice = ownInvoice;
    }
    
    @Column(name="notes", length=200)
    public String getNotes() {
        return this.notes;
    }
    
    public void setNotes(String notes) {
        // make sure this is fits in the DB
        if (notes == null || notes.length() <= 200) { 
            this.notes = notes;
        } else {
            this.notes = notes.substring(0, 200);
            LOG.warn("Trimming notes to 200 lenght: from " + notes + " to " + this.notes);
        }
    }
    
    @Column(name="notes_in_invoice")
    public Integer getNotesInInvoice() {
        return this.notesInInvoice;
    }
    
    public void setNotesInInvoice(Integer notesInInvoice) {
        this.notesInInvoice = notesInInvoice;
    }
    
    /*
     * There might potentially hundreds of process records, but they are not read by the app.
     * They are only taken for display, and then all are needed
     */
    @CollectionOfElements
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="purchaseOrder")
    @Fetch ( FetchMode.SUBSELECT)
    @OrderBy (
        clause = "id desc"
    )
    public Set<OrderProcessDTO> getOrderProcesses() {
        return this.orderProcesses;
    }
    
    public void setOrderProcesses(Set<OrderProcessDTO> orderProcesses) {
        this.orderProcesses = orderProcesses;
    }
    
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="purchaseOrder")
    @Fetch (FetchMode.SUBSELECT)
    @OrderBy(clause="id")
    public List<OrderLineDTO> getLines() {
        return this.lines;
    }
    
    public void setLines(List<OrderLineDTO> orderLineDTOs) {
        this.lines = orderLineDTOs;
    }

    @Column(name="is_current")
    public Integer getIsCurrent() {
        return isCurrent;
    }
    public void setIsCurrent(Integer isCurrent) {
        this.isCurrent = isCurrent;
    }
    
    @Version
    @Column(name="OPTLOCK")
    public Integer getVersionNum() {
        return versionNum;
    }
    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }



    /*
     * Conveniant methods to ease migration from entity beans
     */
    @Transient
    public Integer getBillingTypeId() {
        return getOrderBillingType() == null ? null : getOrderBillingType().getId();
    }
    /*
    public void setBillingTypeId(Integer typeId) {
        if (orderBillingTypeDTO == null) {
            OrderBillingTypeDTO dto = new OrderBillingTypeDTO();
            dto.setId(typeId);
            setOrderBillingType(dto);
        } else {
            orderBillingTypeDTO.setId(id)
        }
    }
    */

    @Transient
    public Integer getStatusId() {
        return getOrderStatus() == null ? null : getOrderStatus().getId();
    }
    public void setStatusId(Integer statusId) {
        if (statusId == null) {
            setOrderStatus(null);
            return;
        }
        OrderStatusDTO dto = new OrderStatusDTO();
        dto.setId(statusId);
        setOrderStatus(dto);
    }

    @Transient
    public Integer getCurrencyId() {
        return getCurrency().getId();
    }
    public void setCurrencyId(Integer currencyId) {
        if (currencyId == null) {
            setCurrency(null);
        } else {
            CurrencyDTO currency = new CurrencyDTO(currencyId);
            setCurrency(currency);
        }
    }
    
    @Transient
    public UserDTO getUser() {
        return getBaseUserByUserId();
    }
    
    @Transient
    public BigDecimal getTotal() {
        if (total != null) {
            return total;
        }
        BigDecimal result = new BigDecimal(0);
        for (OrderLineDTO line: lines) {
            if (line.getDeleted() == 0 && line.getAmount() != null) {
                result = result.add(line.getAmount());
            }
        }
        return result;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    @Transient
    // all the periods, but excluding those from process reviews
    public Collection<OrderProcessDTO> getPeriods() {
        return nonReviewPeriods;
    }

    @Transient
    public Collection<InvoiceDTO> getInvoices() {
        return invoices;
        
    }
    
    @Transient
    public String getPeriodStr() {
        return periodStr;
    }
    public void setPeriodStr(String str) {
        periodStr = str;
    }

    @Transient
    public String getBillingTypeStr() {
        return billingTypeStr;
    }
    public void setBillingTypeStr(String str) {
        this.billingTypeStr = str;
    }

    @Transient
    public String getStatusStr() {
        return statusStr;
    }
    
    @Transient
    public String getTimeUnitStr() {
        return timeUnitStr;
    }
    
    @Transient
    public String getCurrencyName() {
        return currencyName;
    }

    @Transient
    public String getCurrencySymbol() {
        return currencySymbol;
    }
    
    public void addExtraFields(Integer languageId) {
        invoices = new ArrayList<InvoiceDTO>();
        billingProcesses = new ArrayList<BillingProcessDTO>();
        nonReviewPeriods = new ArrayList<OrderProcessDTO>();
        
        for (OrderProcessDTO process: getOrderProcesses()) {
            if (process.getIsReview() == 1) continue;
            nonReviewPeriods.add(process);
            
            try {
                InvoiceBL invoiceBl = new InvoiceBL(process.getInvoice().getId());
                invoices.add(invoiceBl.getDTO());
            } catch (Exception e) {
                throw new SessionInternalError(e);
            } 
            
            billingProcesses.add(process.getBillingProcess());
        }
        
        periodStr = getOrderPeriod().getDescription(languageId);
        billingTypeStr = getOrderBillingType().getDescription(languageId);
        statusStr = getOrderStatus().getDescription(languageId);
        timeUnitStr = Util.getPeriodUnitStr(
                getDueDateUnitId(), languageId);
        
        currencySymbol = getCurrency().getSymbol();
        currencyName = getCurrency().getDescription(languageId);

        for (OrderLineDTO line : getLines()) {
            line.addExtraFields(languageId);
        } 
    }
    
    @Transient
    public Integer getPeriodId() {
        return getOrderPeriod().getId();
    }
    
    @Transient
    public Integer getUserId() {
        return (getBaseUserByUserId() == null) ? null : getBaseUserByUserId().getId();
    }

    @Transient
    public Integer getCreatedBy() {
        return (getBaseUserByCreatedBy() == null) ? null : getBaseUserByCreatedBy().getId();
    }

    @Transient
    public OrderLineDTO getLine(Integer itemId) {
        for (OrderLineDTO line : lines) {
            if (line.getDeleted() == 0 && line.getItem() != null && line.getItem().getId() == itemId) {
                return line;
            }
        }
        
        return null;
    }
    
    @Transient
    public void removeLine(Integer itemId) {
        OrderLineDTO line = getLine(itemId);
        if (line != null) {
            lines.remove(line);
        }
    }

    @Transient
    public boolean isEmpty() {
        return lines.isEmpty();
    }
    
    @Transient
    public int getNumberOfLines() {
        int count = 0;
        for (OrderLineDTO line: getLines()) {
            if (line.getDeleted() == 0) {
                count++;
            }
        }
        return count;
    }

    @Transient
    public List<PricingField> getPricingFields() {
        return this.pricingFields;
    }

    public void setPricingFields(List<PricingField> fields) {
        this.pricingFields = fields;
    }

    // default values
    @Transient
    public void setDefaults() {
        if (getCreateDate() == null) {
            setCreateDate(Calendar.getInstance().getTime());
            setDeleted(0);
        }
        if (getOrderStatus() == null) {
            setOrderStatus(new OrderStatusDAS().find(
                    Constants.ORDER_STATUS_ACTIVE));
        }
        for (OrderLineDTO line : lines) {
            line.setDefaults();
        }
    }

    /**
     * Makes sure that all the proxies are loaded, so no session is needed to
     * use the pojo
     */
    public void touch() {
        getActiveSince();
        if (getBaseUserByUserId() != null) 
            getBaseUserByUserId().getCreateDatetime();
        if (getBaseUserByCreatedBy() != null)
            getBaseUserByCreatedBy().getCreateDatetime();
        for (OrderLineDTO line: getLines()) {
            line.touch();
        }
        for (InvoiceDTO invoice: getInvoices()) {
            invoice.getCreateDatetime();
        }
        for (OrderProcessDTO process: getOrderProcesses()) {
            process.getPeriodStart();
        }
        if (getOrderBillingType() != null)
            getOrderBillingType().getId();
        if (getOrderPeriod() != null)
            getOrderPeriod().getId();
        if (getOrderStatus() != null)
            getOrderStatus().getId();
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer("Order = " +
         "id=" + id + "," + 
         "baseUserByUserId=" + ((baseUserByUserId == null) ? null : baseUserByUserId.getId()) + "," +
         "baseUserByCreatedBy=" + ((baseUserByCreatedBy== null) ? null : baseUserByCreatedBy.getId()) + "," +
         "currencyDTO=" + currencyDTO + "," +
         "orderStatusDTO=" + ((orderStatusDTO == null) ? null : orderStatusDTO) + "," +
         "orderPeriodDTO=" + ((orderPeriodDTO == null) ? null : orderPeriodDTO) + "," +
         "orderBillingTypeDTO=" + ((orderBillingTypeDTO == null) ? null : orderBillingTypeDTO) + "," +
         "activeSince=" + activeSince + "," +
         "activeUntil=" + activeUntil + "," +
         "createDate=" + createDate + "," +
         "nextBillableDay=" + nextBillableDay + "," +
         "deleted=" + deleted + "," +
         "notify=" + notify + "," +
         "lastNotified=" + lastNotified + "," +
         "notificationStep=" + notificationStep + "," +
         "dueDateUnitId=" + dueDateUnitId + "," +
         "dueDateValue=" + dueDateValue + "," +
         "dfFm=" + dfFm + "," +
         "anticipatePeriods=" + anticipatePeriods + "," +
         "ownInvoice=" + ownInvoice + "," +
         "notes=" + notes + "," +
         "notesInInvoice=" + notesInInvoice + "," +
         "orderProcesses=" + orderProcesses + "," +
         "isCurrent=" + isCurrent + "," +
         "versionNum=" + versionNum +
         " lines:[");
        
        for (OrderLineDTO line: getLines()) {
            str.append(line.toString() + "-");
        }
        str.append("]");
        return str.toString();

    }

    @Transient
    public String[] getFieldNames() {
        return new String[] {
                "id",
                "userId",
                "userName",
                "status",
                "period",
                "billingType",
                "currency",
                "total",
                "activeSince",
                "activeUntil",
                "cycleStart",
                "createdDate",
                "nextBillableDay",
                "isMainSubscription",
                "notes",

                // order lines
                "lineItemId",
                "lineProductCode",
                "lineQuantity",
                "linePrice",
                "lineAmount",
                "lineDescription"
        };
    }

    @Transient
    public Object[][] getFieldValues() {
        List<Object[]> values = new ArrayList<Object[]>();

        // main invoice row
        values.add(
            new Object[] {
                id,
                (baseUserByUserId != null ? baseUserByUserId.getId() : null),
                (baseUserByUserId != null ? baseUserByUserId.getUserName() : null),
                (orderStatusDTO != null ? orderStatusDTO.getDescription() : null),
                (orderPeriodDTO != null ? orderPeriodDTO.getDescription() : null),
                (orderBillingTypeDTO != null ? orderBillingTypeDTO.getDescription() : null),
                (currencyDTO != null ? currencyDTO.getDescription() : null),
                getTotal(),
                activeSince,
                activeUntil,
                cycleStarts,
                createDate,
                nextBillableDay,
                isCurrent,
                notes
            }
        );

        // indented row for each order line
        for (OrderLineDTO line : lines) {
            if (line.getDeleted() == 0) {
                values.add(
                    new Object[] {
                        // padding for the main invoice columns
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,

                        // order line
                        line.getItem().getId(),
                        line.getItem().getInternalNumber(),
                        line.getQuantity(),
                        line.getPrice(),
                        line.getAmount(),
                        line.getDescription()
                    }
                );
            }
        }

        return values.toArray(new Object[values.size()][]);
    }
}


