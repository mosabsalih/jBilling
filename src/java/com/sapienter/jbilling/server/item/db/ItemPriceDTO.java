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
package com.sapienter.jbilling.server.item.db;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.sapienter.jbilling.server.util.db.CurrencyDTO;
import java.math.BigDecimal;

@Entity
@TableGenerator(
        name="item_price_GEN",
        table="jbilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue="item_price",
        allocationSize = 100
        )
@Table(name="item_price")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ItemPriceDTO  implements java.io.Serializable {


    private Integer id;
    private CurrencyDTO currencyDTO;
    private ItemDTO item;
    private BigDecimal price;
    private int versionNum;

    // transient
    private String name = null;
    // this is useful for the form, exposing a Float is trouble
    private String priceForm = null;


    public ItemPriceDTO() {
    }

    
    public ItemPriceDTO(Integer id, BigDecimal price) {
        this.id = id;
        this.price = price;
    }

    public ItemPriceDTO(Integer id, ItemDTO item, BigDecimal price, CurrencyDTO currencyDTO) {
       this.id = id;
       this.item = item;
       this.price = price;
       this.currencyDTO = currencyDTO;
    }

    public ItemPriceDTO(ItemPriceDTO other) {
        id = other.id;
        currencyDTO = other.currencyDTO;
        item = other.item;
        price = other.price;
        versionNum = other.versionNum;
    }
   
    @Id @GeneratedValue(strategy=GenerationType.TABLE, generator="item_price_GEN")
    @Column(name="id", unique=true, nullable=false)
    public Integer getId() {
        return this.id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="currency_id")
    public CurrencyDTO getCurrency() {
        return this.currencyDTO;
    }
    
    public void setCurrency(CurrencyDTO currencyDTO) {
        this.currencyDTO = currencyDTO;
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="item_id")
    public ItemDTO getItem() {
        return this.item;
    }
    
    public void setItem(ItemDTO item) {
        this.item = item;
    }
    
    @Column(name="price", nullable=false, precision=17, scale=17)
    public BigDecimal getPrice() {
        return this.price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Version
    @Column(name="OPTLOCK")
    public int getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(int versionNum) {
        this.versionNum = versionNum;
    }

    @Transient
    public Integer getCurrencyId() {
        return getCurrency().getId();
    }

    /**
     * @return
     */
    @Transient
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    @Transient
    public void setName(String name) {
        this.name = name;
    }

    @Transient
    public String toString() {
        return "name = " + name + " priceForm = " + priceForm + super.toString();
    }

    /**
     * @return
     */
    @Transient
    public String getPriceForm() {
        return priceForm;
    }

    /**
     * @param priceForm
     */
    @Transient
    public void setPriceForm(String priceForm) {
        this.priceForm = priceForm;
    }

}


