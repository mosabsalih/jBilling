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

package com.sapienter.jbilling.server.provisioning.task.mmsc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for modifyCustomerRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="modifyCustomerRequest">
 *   &lt;complexContent>
 *     &lt;extension base="{http://mmschandlerfacade.efs.teliasonera.se/}efsBaseMSISDNRequest">
 *       &lt;sequence>
 *         &lt;element name="mmsCapability" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "modifyCustomerRequest", propOrder = {
    "mmsCapability"
})
public class ModifyCustomerRequest
    extends EfsBaseMSISDNRequest
{

    protected String mmsCapability;

    /**
     * Gets the value of the mmsCapability property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMmsCapability() {
        return mmsCapability;
    }

    /**
     * Sets the value of the mmsCapability property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMmsCapability(String value) {
        this.mmsCapability = value;
    }

}
