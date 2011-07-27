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
package com.sapienter.jbilling.server.user.db;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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

import com.sapienter.jbilling.common.JBCrypto;
import com.sapienter.jbilling.server.payment.db.PaymentDTO;

@Entity
@TableGenerator(name = "ach_GEN", table = "jbilling_seqs", pkColumnName = "name", valueColumnName = "next_id", pkColumnValue = "ach", allocationSize = 100)
@Table(name = "ach")
public class AchDTO implements Serializable {

    private int id;
    private UserDTO baseUser;
    private String abaRouting;
    private String bankAccount;
    private int accountType;
    private String bankName;
    private String accountName;
    private Set<PaymentDTO> payments = new HashSet<PaymentDTO>(0);
    private int versionNum;
    private String gatewayKey;

    public AchDTO() {
    }
    
    public AchDTO(AchDTO other) {
        this.id = other.getId();
        setAbaRouting(other.getAbaRouting());
        setBankAccount(other.getBankAccount());
        this.accountType = other.getAccountType();
        this.bankName = other.getBankName();
        setAccountName(other.getAccountName());
        this.payments = other.payments;
        this.gatewayKey= other.getGatewayKey();
    }

    public AchDTO(int id, String abaRouting, String bankAccount,
            int accountType, String bankName, String accountName, String gatewayKey) {
        this.id = id;
        setAbaRouting(abaRouting);
        setBankAccount(bankAccount);
        this.accountType = accountType;
        this.bankName = bankName;
        setAccountName(accountName);
        this.gatewayKey= gatewayKey;
    }

    public AchDTO(int id, UserDTO baseUser, String abaRouting,
            String bankAccount, int accountType, String bankName,
            String accountName, String gatewayKey, Set<PaymentDTO> payments) {
        this.id = id;
        this.baseUser = baseUser;
        setAbaRouting(abaRouting);
        setBankAccount(bankAccount);
        this.accountType = accountType;
        this.bankName = bankName;
        setAccountName(accountName);
        this.payments = payments;
        this.gatewayKey= gatewayKey;
    }
    
    public AchDTO(com.sapienter.jbilling.server.entity.AchDTO oldDTO) {
        this.id = oldDTO.getId() == null ? 0 : oldDTO.getId().intValue();
        setAbaRouting(oldDTO.getAbaRouting());
        setBankAccount(oldDTO.getBankAccount());
        this.accountType = oldDTO.getAccountType();
        this.bankName = oldDTO.getBankName();
        setAccountName(oldDTO.getAccountName());
        this.gatewayKey= oldDTO.getGatewayKey();
    }
    
    public AchDTO(com.sapienter.jbilling.server.entity.AchDTO oldDTO, UserDTO baseUser) {
        this(oldDTO);
        this.baseUser = baseUser;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ach_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public UserDTO getBaseUser() {
        return this.baseUser;
    }

    public void setBaseUser(UserDTO baseUser) {
        this.baseUser = baseUser;
    }

    @Transient
    public String getAbaRouting() {
        if (getRawAbaRouting() == null)
            return null;
        return JBCrypto.getCreditCardCrypto().decrypt(getRawAbaRouting());
    }

    @Transient
    public void setAbaRouting(String abaRouting) {
        if (abaRouting == null || abaRouting.trim().equals("")) {
            setRawAbaRouting(null);
        } else {
            String crip = JBCrypto.getCreditCardCrypto().encrypt(abaRouting);
            setRawAbaRouting(crip);
        }
    }
    
    @Column(name = "aba_routing", nullable = false, length = 9)
    public String getRawAbaRouting() {
        return abaRouting;
    }
    
    public void setRawAbaRouting(String abaRouting) {
        this.abaRouting = abaRouting;
    }

    @Transient
    public String getBankAccount() {
        if (getRawBankAccount() == null)
            return null;
        return JBCrypto.getCreditCardCrypto().decrypt(getRawBankAccount());
    }

    @Transient
    public void setBankAccount(String bankAccount) {
        if (bankAccount == null || bankAccount.trim().equals("")) {
            setRawBankAccount(null);
        } else {
            String crip = JBCrypto.getCreditCardCrypto().encrypt(bankAccount);
            setRawBankAccount(crip);
        }
    }

    @Column(name = "bank_account", nullable = false, length = 20)
    public String getRawBankAccount() {
        return this.bankAccount;
    }

    public void setRawBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    @Column(name = "account_type", nullable = false)
    public int getAccountType() {
        return this.accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    @Column(name = "bank_name", nullable = false, length = 50)
    public String getBankName() {
        return this.bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    @Transient
    public String getAccountName() {
        if (getRawAccountName() == null)
            return null;
        return JBCrypto.getCreditCardCrypto().decrypt(getRawAccountName());
    }

    @Transient
    public void setAccountName(String accountName) {
        if (accountName == null || accountName.trim().equals("")) {
            setRawAccountName(null);
        } else {
            String crip = JBCrypto.getCreditCardCrypto().encrypt(accountName);
            setRawAccountName(crip);
        }
    }
    
    @Column(name = "account_name", nullable = false, length = 100)
    public String getRawAccountName() {
        return this.accountName;
    }
    public void setRawAccountName(String accountName) {
        this.accountName = accountName;
    }
    
    @Column(name = "gateway_key", nullable = true, length = 100)
    public String getGatewayKey() {
        return this.gatewayKey;
    }
    
    public void setGatewayKey(String gatewayKey) {
        this.gatewayKey = gatewayKey;
    }
        
    public boolean useGatewayKey() {
        return (this.gatewayKey != null);
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "ach")
    public Set<PaymentDTO> getPayments() {
        return this.payments;
    }

    public void setPayments(Set<PaymentDTO> payments) {
        this.payments = payments;
    }

    @Version
    @Column(name = "OPTLOCK")
    public int getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(int versionNum) {
        this.versionNum = versionNum;
    }

    public void obscureBankAccount() {
        if (getRawBankAccount() != null) {
            String plain = getBankAccount();
            int len = plain.length() - 4;
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < len; i++) {
                sb.append('*');
            }
            sb.append(plain.substring(len));
            setBankAccount(sb.toString());
        }
    }
    
    @Transient
    public boolean isBankAccountObscured() {
        return getRawBankAccount() != null && getBankAccount().contains("*");
    }
    
    @Transient
    public com.sapienter.jbilling.server.entity.AchDTO getOldDTO() {
        return new com.sapienter.jbilling.server.entity.AchDTO(
                getId(), getAbaRouting(), getBankAccount(), getAccountType(),
                getBankName(), getAccountName(), getGatewayKey());
    }
}
