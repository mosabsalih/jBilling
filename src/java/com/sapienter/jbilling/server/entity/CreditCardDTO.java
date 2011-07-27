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
package com.sapienter.jbilling.server.entity;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlType;

import com.sapienter.jbilling.server.user.validator.CreditCardNumber;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

/**
 * Only used for web services backward compatibility. 
 * Do not use!
 */
@XmlType(name = "credit-card")
public class CreditCardDTO implements Serializable {
    private java.lang.Integer id;
    private boolean idHasBeenSet = false;

    @CreditCardNumber(message = "validation.error.invalid.card.number")
    @NotEmpty(message="validation.error.notnull")
    private java.lang.String number;
    private boolean numberHasBeenSet = false;
    @NotNull(message="validation.error.notnull")
    private java.util.Date expiry;
    private boolean expiryHasBeenSet = false;
    @NotEmpty(message="validation.error.notnull")
    private java.lang.String name;
    private boolean nameHasBeenSet = false;
    private java.lang.Integer type;
    private boolean typeHasBeenSet = false;
    private java.lang.Integer deleted;
    private boolean deletedHasBeenSet = false;
    private java.lang.String securityCode;
    private boolean securityCodeHasBeenSet = false;
    private java.lang.String gatewayKey;

    private java.lang.Integer pk;

    public CreditCardDTO()
    {
    }

    public CreditCardDTO( java.lang.Integer id,java.lang.String number,java.util.Date expiry,java.lang.String name,java.lang.Integer type,java.lang.Integer deleted,java.lang.String securityCode )
    {
        this.id = id;
        idHasBeenSet = true;
        this.number = number;
        numberHasBeenSet = true;
        this.expiry = expiry;
        expiryHasBeenSet = true;
        this.name = name;
        nameHasBeenSet = true;
        this.type = type;
        typeHasBeenSet = true;
        this.deleted = deleted;
        deletedHasBeenSet = true;
        this.securityCode = securityCode;
        securityCodeHasBeenSet = true;
        pk = this.getId();
    }

    //TODO Cloneable is better than this !
    public CreditCardDTO( CreditCardDTO otherValue )
    {
        this.id = otherValue.id;
        idHasBeenSet = true;
        this.number = otherValue.number;
        numberHasBeenSet = true;
        this.expiry = otherValue.expiry;
        expiryHasBeenSet = true;
        this.name = otherValue.name;
        nameHasBeenSet = true;
        this.type = otherValue.type;
        typeHasBeenSet = true;
        this.deleted = otherValue.deleted;
        deletedHasBeenSet = true;
        this.securityCode = otherValue.securityCode;
        securityCodeHasBeenSet = true;
        this.gatewayKey = otherValue.gatewayKey;

        pk = this.getId();
    }

    public java.lang.Integer getPrimaryKey()
    {
        return pk;
    }

    public void setPrimaryKey( java.lang.Integer pk )
    {
        // it's also nice to update PK object - just in case
        // somebody would ask for it later...
        this.pk = pk;
        setId( pk );
    }

    public java.lang.Integer getId()
    {
        return this.id;
    }

    public void setId( java.lang.Integer id )
    {
        this.id = id;
        idHasBeenSet = true;

        pk = id;
    }

    public boolean idHasBeenSet(){
        return idHasBeenSet;
    }
    public java.lang.String getNumber()
    {
        return this.number;
    }

    public void setNumber( java.lang.String number )
    {
        this.number = number;
        numberHasBeenSet = true;

    }

    public boolean numberHasBeenSet(){
        return numberHasBeenSet;
    }
    public java.util.Date getExpiry()
    {
        return this.expiry;
    }

    public void setExpiry( java.util.Date expiry )
    {
        this.expiry = expiry;
        expiryHasBeenSet = true;

    }

    public boolean expiryHasBeenSet(){
        return expiryHasBeenSet;
    }
    public java.lang.String getName()
    {
        return this.name;
    }

    public void setName( java.lang.String name )
    {
        this.name = name;
        nameHasBeenSet = true;

    }

    public boolean nameHasBeenSet(){
        return nameHasBeenSet;
    }
    public java.lang.Integer getType()
    {
        return this.type;
    }

    public void setType( java.lang.Integer type )
    {
        this.type = type;
        typeHasBeenSet = true;

    }

    public boolean typeHasBeenSet(){
        return typeHasBeenSet;
    }
    public java.lang.Integer getDeleted()
    {
        return this.deleted;
    }

    public void setDeleted( java.lang.Integer deleted )
    {
        this.deleted = deleted;
        deletedHasBeenSet = true;

    }

    public boolean deletedHasBeenSet(){
        return deletedHasBeenSet;
    }
    public java.lang.String getSecurityCode()
    {
        return this.securityCode;
    }

    public void setSecurityCode( java.lang.String securityCode )
    {
        this.securityCode = securityCode;
        securityCodeHasBeenSet = true;

    }

    public boolean securityCodeHasBeenSet(){
        return securityCodeHasBeenSet;
    }

    public String getGatewayKey() {
        return gatewayKey;
    }

    public void setGatewayKey(String gatewayKey) {
        this.gatewayKey = gatewayKey;
    }

    @Override
    public String toString() {
        return "CreditCardDTO{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", expiry=" + expiry +
               ", type=" + type +
               ", deleted=" + deleted +
               ", securityCode='" + securityCode + '\'' +
               ", gatewayKey='" + gatewayKey + '\'' +
               '}';
    }

    /**
     * A Value Object has an identity if the attributes making its Primary Key have all been set. An object without identity is never equal to any other object.
     *
     * @return true if this instance has an identity.
     */
    protected boolean hasIdentity()
    {
        return idHasBeenSet;
    }

    public boolean equals(Object other)
    {
        if (this == other)
            return true;
        if ( ! hasIdentity() ) return false;
        if (other instanceof CreditCardDTO)
        {
            CreditCardDTO that = (CreditCardDTO) other;
            if ( ! that.hasIdentity() ) return false;
            boolean lEquals = true;
            if( this.id == null )
            {
                lEquals = lEquals && ( that.id == null );
            }
            else
            {
                lEquals = lEquals && this.id.equals( that.id );
            }

            lEquals = lEquals && isIdentical(that);

            return lEquals;
        }
        else
        {
            return false;
        }
    }

    public boolean isIdentical(Object other)
    {
        if (other instanceof CreditCardDTO)
        {
            CreditCardDTO that = (CreditCardDTO) other;
            boolean lEquals = true;
            if( this.number == null )
            {
                lEquals = lEquals && ( that.number == null );
            }
            else
            {
                lEquals = lEquals && this.number.equals( that.number );
            }
            if( this.expiry == null )
            {
                lEquals = lEquals && ( that.expiry == null );
            }
            else
            {
                lEquals = lEquals && this.expiry.equals( that.expiry );
            }
            if( this.name == null )
            {
                lEquals = lEquals && ( that.name == null );
            }
            else
            {
                lEquals = lEquals && this.name.equals( that.name );
            }
            if( this.type == null )
            {
                lEquals = lEquals && ( that.type == null );
            }
            else
            {
                lEquals = lEquals && this.type.equals( that.type );
            }
            if( this.deleted == null )
            {
                lEquals = lEquals && ( that.deleted == null );
            }
            else
            {
                lEquals = lEquals && this.deleted.equals( that.deleted );
            }
            if( this.securityCode == null )
            {
                lEquals = lEquals && ( that.securityCode == null );
            }
            else
            {
                lEquals = lEquals && this.securityCode.equals( that.securityCode );
            }

            return lEquals;
        }
        else
        {
            return false;
        }
    }

    public int hashCode(){
        int result = 17;
        result = 37*result + ((this.id != null) ? this.id.hashCode() : 0);

        result = 37*result + ((this.number != null) ? this.number.hashCode() : 0);

        result = 37*result + ((this.expiry != null) ? this.expiry.hashCode() : 0);

        result = 37*result + ((this.name != null) ? this.name.hashCode() : 0);

        result = 37*result + ((this.type != null) ? this.type.hashCode() : 0);

        result = 37*result + ((this.deleted != null) ? this.deleted.hashCode() : 0);

        result = 37*result + ((this.securityCode != null) ? this.securityCode.hashCode() : 0);

        return result;
    }

}
