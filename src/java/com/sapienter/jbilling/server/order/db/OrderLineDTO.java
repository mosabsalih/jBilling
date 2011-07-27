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
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
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

import com.sapienter.jbilling.common.Constants;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.sapienter.jbilling.server.item.db.ItemDAS;
import com.sapienter.jbilling.server.item.db.ItemDTO;
import com.sapienter.jbilling.server.mediation.db.MediationRecordLineDTO;
import com.sapienter.jbilling.server.provisioning.db.ProvisioningStatusDAS;
import com.sapienter.jbilling.server.provisioning.db.ProvisioningStatusDTO;
import java.math.BigDecimal;
import java.util.ArrayList;

@Entity
@TableGenerator(
        name="order_line_GEN",
        table="jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue="order_line",
        allocationSize = 100
        )
@Table(name="order_line")
//No cache, mutable and critical
public class OrderLineDTO implements Serializable, Comparable {

    private static final Logger LOG =  Logger.getLogger(OrderLineDTO.class); 

    private int id;
    private OrderLineTypeDTO orderLineTypeDTO;
    private ItemDTO item;
    private OrderDTO orderDTO;
    private BigDecimal amount;
    private BigDecimal quantity;
    private BigDecimal price;
    private Date createDatetime;
    private int deleted;
    private Boolean useItem = true;
    private String description;
    private Integer versionNum;
    private Boolean editable = null;
    private List<MediationRecordLineDTO> events = new ArrayList<MediationRecordLineDTO>(0);

     //provisioning fields
     private ProvisioningStatusDTO provisioningStatus;
     private String provisioningRequestId;

     // other fields, non-persistent
     private String priceStr = null;
     private Boolean totalReadOnly = null;
     private String provisioningStatusStr;

     
    public OrderLineDTO() {
    }
    
    public OrderLineDTO(OrderLineDTO other) {
        this.id = other.id;
        this.orderLineTypeDTO = other.getOrderLineType();
        this.item = other.getItem();
        this.amount = other.getAmount();
        this.quantity = other.getQuantity();
        this.price = other.getPrice();
        this.createDatetime = other.getCreateDatetime();
        this.deleted = other.getDeleted();
        this.useItem = other.getUseItem();
        this.description = other.getDescription();
        this.orderDTO = other.getPurchaseOrder();
        this.versionNum = other.getVersionNum();
    }
    
    public OrderLineDTO(int id, BigDecimal amount, Date createDatetime, Integer deleted) {
        this.id = id;
        this.amount = amount;
        this.createDatetime = createDatetime;
        this.deleted = deleted;
    }

    public OrderLineDTO(int id, OrderLineTypeDTO orderLineTypeDTO, ItemDTO item, OrderDTO orderDTO, BigDecimal amount,
            BigDecimal quantity, BigDecimal price, Date createDatetime, Integer deleted,
            String description, ProvisioningStatusDTO provisioningStatus, String provisioningRequestId) {
       this.id = id;
       this.orderLineTypeDTO = orderLineTypeDTO;
       this.item = item;
       this.orderDTO = orderDTO;
       this.amount = amount;
       this.quantity = quantity;
       this.price = price;
       this.createDatetime = createDatetime;
       this.deleted = deleted;
       this.description = description;
       this.provisioningStatus=provisioningStatus;
       this.provisioningRequestId=provisioningRequestId;
    }
   
    @Id @GeneratedValue(strategy=GenerationType.TABLE, generator="order_line_GEN")
    @Column(name="id", unique=true, nullable=false)
    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="type_id", nullable=false)
    public OrderLineTypeDTO getOrderLineType() {
        return this.orderLineTypeDTO;
    }
    
    public void setOrderLineType(OrderLineTypeDTO orderLineTypeDTO) {
        this.orderLineTypeDTO = orderLineTypeDTO;
    }
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="item_id")
    public ItemDTO getItem() {
        return this.item;
    }
    
    public void setItem(ItemDTO item) {
        this.item = item;
    }
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="order_id")
    public OrderDTO getPurchaseOrder() {
        return this.orderDTO;
    }
    
    public void setPurchaseOrder(OrderDTO orderDTO) {
        this.orderDTO = orderDTO;
    }

    /**
     * Returns the total amount for this line. Usually this would be
     * the {@code price * quantity}
     *
     * @return amount
     */
    @Column(name="amount", nullable=false, precision=17, scale=17)
    public BigDecimal getAmount() {
        return this.amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    @Column(name="quantity", precision=17, scale=17)
    public BigDecimal getQuantity() {
        return this.quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    @Transient
    public void setQuantity(Double quantity) {
        setQuantity(new BigDecimal(quantity).setScale(Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND));
    }

    @Transient
    public void setQuantity(Integer quantity) {
        setQuantity(new BigDecimal(quantity));
    }

    /**
     * Returns the price of a single unit of this item.
     *
     * @return unit price
     */    
    @Column(name="price", precision=17, scale=17)
    public BigDecimal getPrice() {
        return this.price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Column(name="create_datetime", nullable=false, length=29)
    public Date getCreateDatetime() {
        return this.createDatetime;
    }
    public void setCreateDatetime(Date createDatetime) {
        this.createDatetime = createDatetime;
    }
    
    @Column(name="deleted", nullable=false)
    public int getDeleted() {
        return this.deleted;
    }
    
    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    @Column(name = "use_item", nullable = false)
    public Boolean getUseItem() {
        return useItem;
    }

    public void setUseItem(Boolean useItem) {
        this.useItem = useItem;
    }

    @Column(name="description", length=1000)
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        if (description != null && description.length() > 1000) {
            description = description.substring(0, 1000);
            LOG.warn("Truncated an order line description to " + description);
        }

        this.description = description;
    }

    @Version
    @Column(name="OPTLOCK")
    public Integer getVersionNum() {
        return versionNum;
    }
    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }
        
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="orderLine")
    @Cascade( value= org.hibernate.annotations.CascadeType.DELETE_ORPHAN )
    public List<MediationRecordLineDTO> getEvents() {
        return this.events;
    }
    
    public void setEvents(List<MediationRecordLineDTO> events) {
        this.events = events;
    }        

    /*
     * Conveniant methods to ease migration from entity beans
     */
    @Transient
    public Integer getItemId() {
        return (getItem() == null) ? null : getItem().getId();
    }

    public void setItemId(Integer itemId) {
        ItemDAS das = new ItemDAS();
        setItem(das.find(itemId));
    }

    @Transient
    public Boolean getEditable() {
        if (editable == null) {
            editable = getOrderLineType().getEditable() == 1;
        }
        return editable;
    }
    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    @Transient
    public String getPriceStr() {
        return priceStr;
    }

    public void setPriceStr(String priceStr) {
        this.priceStr = priceStr;
    }
    
    @Transient
    public Boolean getTotalReadOnly() {
        if (totalReadOnly == null) {
            setTotalReadOnly(false);
        }
        return totalReadOnly;
    }

    public void setTotalReadOnly(Boolean totalReadOnly) {
        this.totalReadOnly = totalReadOnly;
    }

    @Transient
    public String getProvisioningStatusStr() {
        return provisioningStatusStr;
    }

    public void setProvisioningStatusStr(String provisioningStatusStr) {
        this.provisioningStatusStr = provisioningStatusStr;
    }

    @Transient
    public Integer getTypeId() {
        return getOrderLineType() == null ? null : getOrderLineType().getId();
    }

    public void setTypeId(Integer typeId) {
        OrderLineTypeDAS das = new OrderLineTypeDAS();
        setOrderLineType(das.find(typeId));
    }
    
    @Transient
    public Integer getQuantityInt() {
        if (quantity == null) return null;
        return this.quantity.intValue();
    }

    /**
     * @return the provisioningStatus
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="provisioning_status")
    public ProvisioningStatusDTO getProvisioningStatus() {
        return provisioningStatus;
    }

    /**
     * @param provisioningStatus the provisioningStatus to set
     */
    public void setProvisioningStatus(ProvisioningStatusDTO provisioningStatus) {
        this.provisioningStatus = provisioningStatus;
    }

    @Transient
    public Integer getProvisioningStatusId() {
        return getProvisioningStatus() == null ? null : 
                getProvisioningStatus().getId();
    }

    public void setProvisioningStatusId(Integer provisioningStatusId) {
        ProvisioningStatusDAS das = new ProvisioningStatusDAS();
        setProvisioningStatus(das.find(provisioningStatusId));
    }

    /**
     * @return the provisioningRequestId
     */
    @Column(name="provisioning_request_id")
    public String getProvisioningRequestId() {
        return provisioningRequestId;
    }

    /**
     * @param provisioningRequestId the provisioningRequestId to set
     */
    public void setProvisioningRequestId(String provisioningRequestId) {
        this.provisioningRequestId = provisioningRequestId;
    }

    public void addExtraFields(Integer languageId) {
        if (getProvisioningStatus() != null) {
            provisioningStatusStr = getProvisioningStatus().getDescription(languageId);
        }
    }

    public void touch() {
        getCreateDatetime();
        if (getItem() != null) {
            getItem().getInternalNumber();
        }
        getEditable();
    }
    
    @Transient
    public void setDefaults() {
        if (getCreateDatetime() == null) {
            setCreateDatetime(Calendar.getInstance().getTime());
        }
    }
    
    // this helps to add lines to the treeSet
    public int compareTo(Object o) {
        OrderLineDTO other = (OrderLineDTO) o;
        if (other.getItem() == null || this.getItem() == null) {
            return -1;
        }
        return new Integer(this.getItem().getId()).compareTo(other.getItem().getId());
    }

    @Override
    public String toString() {
        return "OrderLine:[id=" + id +
        " orderLineType=" + ((orderLineTypeDTO == null) ? "null" : orderLineTypeDTO.getId()) +
        " item=" +  item.getId() +
        " order id=" + ((orderDTO == null) ? "null" : orderDTO.getId()) +
        " amount=" +  amount +
        " quantity=" +  quantity +
        " price=" +  price +
        " createDatetime=" +  createDatetime +
        " deleted=" + deleted  +
        " useItem=" + useItem +
        " description=" + description + 
        " versionNum=" + versionNum  +
        " provisioningStatus=" + provisioningStatus  +
        " provisionningRequestId=" + provisioningRequestId  +        
        " editable=" + editable + "]";
    }    
}
