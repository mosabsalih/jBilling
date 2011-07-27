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
package com.sapienter.jbilling.server.payment.tasks;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sapienter.jbilling.server.payment.IExternalCreditCardStorage;
import com.sapienter.jbilling.server.payment.PaymentAuthorizationBL;
import com.sapienter.jbilling.server.payment.PaymentDTOEx;
import com.sapienter.jbilling.server.payment.db.PaymentAuthorizationDTO;
import com.sapienter.jbilling.server.payment.db.PaymentResultDAS;
import com.sapienter.jbilling.server.pluggableTask.PaymentTask;
import com.sapienter.jbilling.server.pluggableTask.PaymentTaskWithTimeout;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.user.ContactBL;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.contact.db.ContactDTO;
import com.sapienter.jbilling.server.user.db.AchDTO;
import com.sapienter.jbilling.server.user.db.CreditCardDTO;
import com.sapienter.jbilling.server.user.db.UserDTO;
import com.sapienter.jbilling.server.util.Constants;

public class PaymentAuthorizeNetCIMTask extends PaymentTaskWithTimeout
    implements PaymentTask, IExternalCreditCardStorage {

    private static final Logger LOG = Logger.getLogger(PaymentAuthorizeNetCIMTask.class);

    private static final String PARAMETER_NAME = "login";
    private static final String PARAMETER_KEY = "transaction_key";
    private static final String PARAMETER_TEST_MODE = "test"; // true or false

    /**
     * Validation mode allows you to generate a test transaction at the time you create a customer profile. In Test
     * Mode, only field validation is performed. In Live Mode, a transaction is generated and submitted to the processor
     * with the amount of $0.00 or $0.01. If successful, the transaction is immediately voided. Visa transactions are
     * being switched from $0.01 to $0.00 for all processors. All other credit card types use $0.01. We recommend you
     * consult your Merchant Account Provider before switching to Zero Dollar Authorizations for Visa, because you may
     * be subject to fees For Visa transactions using $0.00, the billTo address and billTo zip fields are required.
     */
    private static final String PARAMETER_VALIDATION_MODE = "validation_mode"; // none/testMode/liveMode

    private String getProcessorName() {
        return "Authorize.Net CIM";
    }

    public boolean process(PaymentDTOEx info) throws PluggableTaskException {
        return doProcess(info, "profileTransAuthCapture", null);
    }

    public boolean preAuth(PaymentDTOEx info) throws PluggableTaskException {
        return doProcess(info, "profileTransAuthOnly", null);
    }

    public boolean confirmPreAuth(PaymentAuthorizationDTO auth, PaymentDTOEx info) throws PluggableTaskException {
        return doProcess(info, "profileTransCaptureOnly", auth.getApprovalCode());
    }

    public void failure(Integer userId, Integer retry) { /* noop */ }

    private boolean doProcess(PaymentDTOEx info, String txType, String approvalCode) throws PluggableTaskException {
        int method = -1; /* 1 cc , 2 ach*/

        if (info.getCreditCard() != null) {
            method = 1;
        }
        if (info.getAch() != null) {
            method = 2;
        }

        if ( Constants.PAYMENT_METHOD_ACH.equals(info.getMethodId() ) ) {
            method = 2;
            if (null == info.getAch()) {
                LOG.error("Can't process payment without a ACH Details");
                throw new PluggableTaskException("Payment Method ACH but ACH Info not present in payment");
            }
        } else {
            method=1;
        }

        if (info.getCreditCard() == null &&
            info.getAch() == null) {
            LOG.error("Can't process without a credit card or ach");
            throw new PluggableTaskException("Credit card/ACH not present in payment");
        }

//        if (info.getCreditCard() != null &&
//          info.getAch() != null) {
//            LOG.warn("Both cc and ach are present");
//            method = 2; // default to ach (cheaper)
//        }

        if (isCreditCardStored(info, method == 1)) {
            LOG.debug("credit card is obscured, retrieving from database to use external store.");
            if (method == 1)
                info.setCreditCard(new UserBL(info.getUserId()).getCreditCard());
        } else {
            /*  Credit cards being used for one time payments do not need to be saved in the CIM
               as they do not represent the customers primary card.

               Process using the next payment processor in the chain. This should be configured
               as the PaymentAuthorizeNetTask to process normal credit cards through Authorize.net
            */
            LOG.debug("One time payment credit card (not obscured!) or ACH Gateway Key Not available, process using the next PaymentTask.");
            info.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_UNAVAILABLE));
            return true;
        }

        String gatewayKey = (method == 1) ? info.getCreditCard().getGatewayKey() : info.getAch().getGatewayKey();
        AuthorizeNetCIMApi api = createApi();
        CustomerProfileData profile = CustomerProfileData.buildFromGatewayKey(gatewayKey);
        PaymentAuthorizationDTO paymentDTO = api.performTransaction(info.getAmount(),
                                                                    profile,
                                                                    txType, approvalCode);

        if (paymentDTO.getCode1().equals("1")) {
            info.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_OK));
            info.setAuthorization(paymentDTO);
            PaymentAuthorizationBL bl = new PaymentAuthorizationBL();
            bl.create(paymentDTO, info.getId());
            return false;
        } else {
            info.setPaymentResult(new PaymentResultDAS().find(Constants.RESULT_FAIL));
            info.setAuthorization(paymentDTO);
            PaymentAuthorizationBL bl = new PaymentAuthorizationBL();
            bl.create(paymentDTO, info.getId());
            return false;
        }
    }

    public String deleteCreditCard(ContactDTO contact, CreditCardDTO creditCard, AchDTO ach) {
        //credit card or ach was passed as null
        if (creditCard == null && ach == null) {
            LOG.warn("No credit card/Ach details to store externally.");
            return null;
        }

        try {
            deletePaymentProfile(creditCard, ach, createApi());
        } catch (PluggableTaskException e) {
            LOG.debug("Could not process delete event.");
            return null;
        }

        return ach.getGatewayKey();
    }

    public String storeCreditCard(ContactDTO contact, CreditCardDTO creditCard, AchDTO ach) {

        // new contact that has not had a credit card created yet
        if (creditCard == null && ach == null) {
            LOG.warn("No credit card/Ach details to store externally.");
            return null;
        }

        LOG.debug("Storing credit card/ach details info within " + getProcessorName() + " gateway");

        // fetch contact info if missing
        if (contact == null) {
            UserDTO user = null;
            if (creditCard != null && !creditCard.getBaseUsers().isEmpty()) {
                user = creditCard.getBaseUsers().iterator().next();
            } else if (ach != null && ach.getBaseUser() != null) {
                user = ach.getBaseUser();
            }
            if (user != null) {
                ContactBL bl = new ContactBL();
                bl.set(user.getId());
                contact = bl.getEntity();
            }
        }

        // user does not have contact info
        if (contact == null) {
            LOG.error("Could not determine contact info for external credit card storage");
            return null;
        }

        try {
            CustomerProfileData profile = createOrUpdateProfile(contact, creditCard, ach, createApi());
            String gatewayKey = profile.toGatewayKey();
            LOG.debug("Obtained card reference number during external storage: " + gatewayKey);
            return gatewayKey;
        } catch (PluggableTaskException e) {
            LOG.debug("Could not process external storage payment", e);
        }
        return null;
    }

    private AuthorizeNetCIMApi createApi() throws PluggableTaskException {
        return new AuthorizeNetCIMApi(ensureGetParameter(PARAMETER_NAME),
                                      ensureGetParameter(PARAMETER_KEY),
                                      getOptionalParameter(PARAMETER_VALIDATION_MODE, "none"),
                                      Boolean.valueOf(getOptionalParameter(PARAMETER_TEST_MODE, "false")),
                                      getTimeoutSeconds());
    }

    private CustomerProfileData createOrUpdateProfile(ContactDTO contact, CreditCardDTO creditCard, AchDTO ach,
                                                      AuthorizeNetCIMApi api) throws PluggableTaskException {

        if ( ( creditCard != null && !creditCard.isNumberObsucred() ) || ach != null) {
            try {
                return api.createCustomerProfile(CustomerProfileData.buildFromContactAndCreditCardOrACH(contact, creditCard, ach));
            } catch (DublicateProfileRecordException e) {
                return updateProfile(contact, creditCard, ach, e.getProfileId(), api);
            }
        } else {
            CustomerProfileData customerProfile = CustomerProfileData.buildFromGatewayKey((creditCard != null) ? creditCard.getGatewayKey() :
        ach.getGatewayKey());
            return updateProfile(contact, creditCard, ach, customerProfile.getCustomerProfileId(), api);
        }
    }

    private CustomerProfileData updateProfile(ContactDTO contact, CreditCardDTO creditCard, AchDTO ach, String profileID,
                                              AuthorizeNetCIMApi api) throws PluggableTaskException {

        CustomerProfileData customerProfile = api.getCustomerProfile(profileID);
        customerProfile.fillWith(contact, creditCard, ach);
        api.updateCustomerProfile(customerProfile);

        return customerProfile;
    }

    private void deletePaymentProfile(CreditCardDTO creditCard, AchDTO ach, AuthorizeNetCIMApi api) throws PluggableTaskException {

        String gatewayKey= null;

        if ( ach != null ) {
            gatewayKey= ach.getGatewayKey();
        } else {
            gatewayKey= creditCard.getGatewayKey();
        }

        try {
            CustomerProfileData customerProfile= CustomerProfileData.buildFromGatewayKey(gatewayKey);
            api.deletePaymentProfile(customerProfile);
        } catch (Exception e) {
            throw new PluggableTaskException("Could not process delete Payment Profile.");
        }
    }

    private static boolean isCreditCardStored(PaymentDTOEx payment, boolean bUseCreditCard) {
        LOG.debug("IsCreditCardStored called, bUseCreditCard = " + bUseCreditCard + ", cc = " + payment.getCreditCard() + ", ACH = " + payment.getAch());
        return (bUseCreditCard) ? payment.getCreditCard().useGatewayKey() : payment.getAch().useGatewayKey();
    }
}


class DublicateProfileRecordException extends Exception {

    private static final long serialVersionUID = 1L;
    private final String profileId;

    DublicateProfileRecordException(String profileId, String errorMessage) {
        super(errorMessage);
        this.profileId = profileId;
    }

    public String getProfileId() {
        return profileId;
    }
}

class CustomerProfileData {

    public static final int CREDIT_CARD = 1;
    public static final int BANK_ACCOUNT = 2;

    private static final String GATEWAY_KEY_DELIMITER = "/";

    private String customerProfileId;
    private String email;

    private String customerPaymentProfileId;
    private String firstName;
    private String lastName;
    private String company;
    private String address;
    private String city;
    private String state;
    private String country;
    private String phoneNumber;
    private String faxNumber;
    private String creditCardNumber;
    private String creditCardExpirationDate;
    private String creditCardCode;
    // Added for ACH support
    private int paymentType;
    private String accountType;
    private String routingNumber;
    private String accountNumber;
    private String accountName;
    private String bankName;

    CustomerProfileData(String customerProfileId, String customerPaymentProfileId) {
        this.customerProfileId = customerProfileId;
        this.customerPaymentProfileId = customerPaymentProfileId;
    }

    CustomerProfileData() {
    }

    public String getCustomerProfileId() {
        return customerProfileId;
    }

    public void setCustomerProfileId(String customerProfileId) {
        this.customerProfileId = customerProfileId;
    }

    public String getCustomerPaymentProfileId() {
        return customerPaymentProfileId;
    }

    public void setCustomerPaymentProfileId(String customerPaymentProfileId) {
        this.customerPaymentProfileId = customerPaymentProfileId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFaxNumber() {
        return faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        this.faxNumber = faxNumber;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getCreditCardExpirationDate() {
        return creditCardExpirationDate;
    }

    public void setCreditCardExpirationDate(String creditCardExpirationDate) {
        this.creditCardExpirationDate = creditCardExpirationDate;
    }

    public String getCreditCardCode() {
        return creditCardCode;
    }

    public void setCreditCardCode(String creditCardCode) {
        this.creditCardCode = creditCardCode;
    }

    public String toGatewayKey() {
        return customerProfileId + GATEWAY_KEY_DELIMITER + customerPaymentProfileId;
    }

    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void fillWith(ContactDTO contact, CreditCardDTO creditCard, AchDTO ach) {
        if (contact != null) {
            setEmail(contact.getEmail());
            setFirstName(contact.getFirstName());
            setLastName(contact.getLastName());
            setCompany(contact.getOrganizationName());
            setAddress(contact.getAddress1());
            setCity(contact.getCity());
            setState(contact.getStateProvince());
            setCountry(contact.getCountryCode());
            setPhoneNumber(contact.getPhoneNumber());
            setFaxNumber(contact.getFaxNumber());
        }

        if (ach != null) {
            setAccountType(ach.getAccountType() == 1 ? "checking" : "savings");
            setRoutingNumber(ach.getAbaRouting());
            setAccountNumber(ach.getBankAccount());
            setAccountName(ach.getAccountName());
            setBankName(ach.getBankName());
            setPaymentType(CustomerProfileData.BANK_ACCOUNT);
        }
        else if (creditCard != null) {
            if (!creditCard.isNumberObsucred()) {
                setCreditCardNumber(creditCard.getNumber());
                setCreditCardExpirationDate(new SimpleDateFormat("yyyy-MM").format(creditCard.getCcExpiry()));
            }

            if (creditCard.getSecurityCode() != null) {
                setCreditCardNumber(creditCard.getSecurityCode());
            }
            setPaymentType(CustomerProfileData.CREDIT_CARD);
        }



    }

    public static CustomerProfileData buildFromGatewayKey(String gatewayKey) {
        int delimiterPosition = gatewayKey.indexOf(GATEWAY_KEY_DELIMITER);
        String customerProfileId = gatewayKey.substring(0, delimiterPosition);
        String paymentProfileId = gatewayKey.substring(delimiterPosition + GATEWAY_KEY_DELIMITER.length(),
                                                       gatewayKey.length());

        return new CustomerProfileData(customerProfileId, paymentProfileId);
    }

    public static CustomerProfileData buildFromContactAndCreditCardOrACH(ContactDTO contact, CreditCardDTO creditCard, AchDTO ach) {
        CustomerProfileData customerProfileData = new CustomerProfileData();
        customerProfileData.fillWith(contact, creditCard, ach);
        return customerProfileData;
    }
}

class AuthorizeNetCIMApi {
    private static final Logger LOG = Logger.getLogger(AuthorizeNetCIMApi.class);

    private static final String DUBLICATE_PROFILE_ID_PREFIX = "id ";
    private static final String DUPLICATE_PROFILE_ERROR_CODE = "E00039";

    // Authorize.Net Web Service Resources
    private static final String AUTHNET_XML_TEST_URL = "https://apitest.authorize.net/xml/v1/request.api";
    private static final String AUTHNET_XML_PROD_URL = "https://api.authorize.net/xml/v1/request.api";
    private static final String AUTHNET_XML_NAMESPACE = "AnetApi/xml/v1/schema/AnetApiSchema.xsd";

    private final String loginID;
    private final String transactionKey;
    private final String validationMode;
    private final boolean testMode;
    private final int timeout;

    AuthorizeNetCIMApi(String loginID, String transactionKey, String validationMode, boolean testMode, int timeout) {
        this.loginID = loginID;
        this.transactionKey = transactionKey;
        this.validationMode = validationMode;
        this.testMode = testMode;
        this.timeout = timeout;
    }

    public PaymentAuthorizationDTO performTransaction(BigDecimal amount, CustomerProfileData customerProfile,
                                                      String txType, String approvalCode)
        throws PluggableTaskException {

        String XML = buildCustomerProfileTransactionRequest(amount, customerProfile, txType, approvalCode);
        String HTTPResponse = sendViaXML(XML);

        return parseCustomerProfileTransactionResponse(HTTPResponse);
    }

    public CustomerProfileData createCustomerProfile(CustomerProfileData customerProfile)
        throws DublicateProfileRecordException, PluggableTaskException {

        String XML = buildCreateCustomerProfileRequest(customerProfile);
        String HTTPResponse = sendViaXML(XML);

        return parseCreateCustomerProfileResponse(HTTPResponse);
    }

    public CustomerProfileData getCustomerProfile(String customerProfileID)
        throws PluggableTaskException {

        String XML = buildGetCustomerProfileRequest(customerProfileID);
        String HTTPResponse = sendViaXML(XML);

        return parseGetCustomerProfileResponse(HTTPResponse);
    }

    public void updateCustomerProfile(CustomerProfileData customerProfile) throws PluggableTaskException {
        String XML = buildUpdateCustomerProfileRequest(customerProfile);
        String HTTPResponse = sendViaXML(XML);
        parseSimpleResponse(HTTPResponse, "updateCustomerProfile");

        if ( null == customerProfile.getCustomerPaymentProfileId()) {
            XML= buildCreateCustomerPaymentProfileRequest(customerProfile);
        } else {
            XML = buildUpdateCustomerPaymentProfileRequest(customerProfile);
        }
        HTTPResponse = sendViaXML(XML);
        if ( null == customerProfile.getCustomerPaymentProfileId()) {
            try {
                String customerPaymentProfileId= parseCreateCustomerPaymentProfileResponse(HTTPResponse);
                customerProfile.setCustomerPaymentProfileId(customerPaymentProfileId);
            } catch (DublicateProfileRecordException e) {
                LOG.warn("Error creating a Payment Profile for this customer in absence of any. "
                        + customerProfile.getCustomerProfileId());
                throw new PluggableTaskException(e);
            }
        }
        parseSimpleResponse(HTTPResponse, "updateCustomerPaymentProfile");
    }

    public void deletePaymentProfile(CustomerProfileData customerProfile) throws PluggableTaskException {
        String XML = buildDeleteCustomerPaymentProfileRequest(customerProfile);
        String HTTPResponse = sendViaXML(XML);
        parseSimpleResponse(HTTPResponse, "deleteCustomerPaymentProfileRequest");
    }

    private void buildTag(StringBuffer xml, String name, String value) {
        xml.append(String.format("<%s>%s</%s>", name, value, name));
    }

    private void buildTagIfNotEmpty(StringBuffer xml, String name, String value) {
        if (value != null) {
            buildTag(xml, name, value);
        }
    }

    private void beginTag(StringBuffer xml, String name) {
        xml.append(String.format("<%s>", name));
    }

    private void endTag(StringBuffer xml, String name) {
        xml.append(String.format("</%s>", name));
    }

    private String getMerchantAuthenticationXML() throws PluggableTaskException {
        StringBuffer xml = new StringBuffer();

        beginTag(xml, "merchantAuthentication");
        buildTag(xml, "name", loginID);
        buildTag(xml, "transactionKey", transactionKey);
        endTag(xml, "merchantAuthentication");

        return xml.toString();
    }

    private String buildDeleteCustomerPaymentProfileRequest(CustomerProfileData customerProfileData)
        throws PluggableTaskException {
        StringBuffer XML = new StringBuffer();
        XML.append("<deleteCustomerPaymentProfileRequest xmlns=\"" + AUTHNET_XML_NAMESPACE + "\">");
        XML.append(getMerchantAuthenticationXML());
        buildTag(XML, "customerProfileId", customerProfileData.getCustomerProfileId());
        buildTag(XML, "customerPaymentProfileId", customerProfileData.getCustomerPaymentProfileId());
        endTag(XML, "deleteCustomerPaymentProfileRequest");
        return XML.toString();
    }

    private String buildCreateCustomerProfileRequest(CustomerProfileData customerProfileData)
        throws PluggableTaskException {

        StringBuffer XML = new StringBuffer();
        XML.append("<createCustomerProfileRequest xmlns=\"" + AUTHNET_XML_NAMESPACE + "\">");
        XML.append(getMerchantAuthenticationXML());

        beginTag(XML, "profile");
        buildTag(XML, "email", customerProfileData.getEmail());

        beginTag(XML, "paymentProfiles");

        beginTag(XML, "billTo");
        buildTag(XML, "firstName", customerProfileData.getFirstName());
        buildTag(XML, "lastName", customerProfileData.getLastName());
        buildTag(XML, "company", customerProfileData.getCompany());
        buildTag(XML, "address", customerProfileData.getAddress());
        buildTag(XML, "city", customerProfileData.getCity());
        buildTag(XML, "state", customerProfileData.getState());
        buildTag(XML, "country", customerProfileData.getCountry());
        buildTag(XML, "phoneNumber", customerProfileData.getPhoneNumber());
        buildTag(XML, "faxNumber", customerProfileData.getFaxNumber());
        endTag(XML, "billTo");

        beginTag(XML, "payment");
        if (customerProfileData.getPaymentType() == CustomerProfileData.CREDIT_CARD) {
            beginTag(XML, "creditCard");
            buildTag(XML, "cardNumber", customerProfileData.getCreditCardNumber());
            buildTag(XML, "expirationDate", customerProfileData.getCreditCardExpirationDate());
            buildTagIfNotEmpty(XML, "cardCode", customerProfileData.getCreditCardCode());
            endTag(XML, "creditCard");
        } else if (customerProfileData.getPaymentType() == CustomerProfileData.BANK_ACCOUNT) {
            beginTag(XML, "bankAccount");
            buildTag(XML, "accountType", customerProfileData.getAccountType());
            buildTag(XML, "routingNumber", customerProfileData.getRoutingNumber());
            buildTag(XML, "accountNumber", customerProfileData.getAccountNumber());
            buildTag(XML, "nameOnAccount", customerProfileData.getAccountName());
            buildTag(XML, "bankName", customerProfileData.getBankName());
            endTag(XML, "bankAccount");
        }
        endTag(XML, "payment");

        endTag(XML, "paymentProfiles");
        endTag(XML, "profile");
        buildTag(XML, "validationMode", validationMode);
        endTag(XML, "createCustomerProfileRequest");

        return XML.toString();
    }

    private String buildUpdateCustomerProfileRequest(CustomerProfileData customerProfileData)
        throws PluggableTaskException {

        StringBuffer XML = new StringBuffer();
        XML.append("<updateCustomerProfileRequest xmlns=\"" + AUTHNET_XML_NAMESPACE + "\">");
        XML.append(getMerchantAuthenticationXML());

        beginTag(XML, "profile");
        buildTag(XML, "email", customerProfileData.getEmail());
        buildTag(XML, "customerProfileId", customerProfileData.getCustomerProfileId());
        endTag(XML, "profile");

        endTag(XML, "updateCustomerProfileRequest");

        return XML.toString();
    }

    private String buildCreateCustomerPaymentProfileRequest(CustomerProfileData customerProfileData)
    throws PluggableTaskException {

    StringBuffer XML = new StringBuffer();
    XML.append("<createCustomerPaymentProfileRequest xmlns=\"" + AUTHNET_XML_NAMESPACE + "\">");
    XML.append(getMerchantAuthenticationXML());

    buildTag(XML, "customerProfileId", customerProfileData.getCustomerProfileId());

    beginTag(XML, "paymentProfile");

    beginTag(XML, "billTo");
    buildTag(XML, "firstName", customerProfileData.getFirstName());
    buildTag(XML, "lastName", customerProfileData.getLastName());
    buildTag(XML, "company", customerProfileData.getCompany());
    buildTag(XML, "address", customerProfileData.getAddress());
    buildTag(XML, "city", customerProfileData.getCity());
    buildTag(XML, "state", customerProfileData.getState());
    buildTag(XML, "country", customerProfileData.getCountry());
    buildTag(XML, "phoneNumber", customerProfileData.getPhoneNumber());
    buildTag(XML, "faxNumber", customerProfileData.getFaxNumber());
    endTag(XML, "billTo");

    beginTag(XML, "payment");
    if (customerProfileData.getPaymentType() == CustomerProfileData.CREDIT_CARD) {
        beginTag(XML, "creditCard");
        buildTag(XML, "cardNumber", customerProfileData.getCreditCardNumber());
        buildTag(XML, "expirationDate", customerProfileData.getCreditCardExpirationDate());
        buildTagIfNotEmpty(XML, "cardCode", customerProfileData.getCreditCardCode());
        endTag(XML, "creditCard");
    } else if (customerProfileData.getPaymentType() == CustomerProfileData.BANK_ACCOUNT) {
        beginTag(XML, "bankAccount");
        buildTag(XML, "accountType", customerProfileData.getAccountType());
        buildTag(XML, "routingNumber", customerProfileData.getRoutingNumber());
        buildTag(XML, "accountNumber", customerProfileData.getAccountNumber());
        buildTag(XML, "nameOnAccount", customerProfileData.getAccountName());
        buildTag(XML, "bankName", customerProfileData.getBankName());
        endTag(XML, "bankAccount");
    }
    endTag(XML, "payment");
    endTag(XML, "paymentProfile");
    endTag(XML, "createCustomerPaymentProfileRequest");

    return XML.toString();
}

    private String buildUpdateCustomerPaymentProfileRequest(CustomerProfileData customerProfileData)
        throws PluggableTaskException {

        StringBuffer XML = new StringBuffer();
        XML.append("<updateCustomerPaymentProfileRequest xmlns=\"" + AUTHNET_XML_NAMESPACE + "\">");
        XML.append(getMerchantAuthenticationXML());

        buildTag(XML, "customerProfileId", customerProfileData.getCustomerProfileId());

        beginTag(XML, "paymentProfile");

        beginTag(XML, "billTo");
        buildTag(XML, "firstName", customerProfileData.getFirstName());
        buildTag(XML, "lastName", customerProfileData.getLastName());
        buildTag(XML, "company", customerProfileData.getCompany());
        buildTag(XML, "address", customerProfileData.getAddress());
        buildTag(XML, "city", customerProfileData.getCity());
        buildTag(XML, "state", customerProfileData.getState());
        buildTag(XML, "country", customerProfileData.getCountry());
        buildTag(XML, "phoneNumber", customerProfileData.getPhoneNumber());
        buildTag(XML, "faxNumber", customerProfileData.getFaxNumber());
        endTag(XML, "billTo");

        beginTag(XML, "payment");
        if (customerProfileData.getPaymentType() == CustomerProfileData.CREDIT_CARD) {
            beginTag(XML, "creditCard");
            buildTag(XML, "cardNumber", customerProfileData.getCreditCardNumber());
            buildTag(XML, "expirationDate", customerProfileData.getCreditCardExpirationDate());
            buildTagIfNotEmpty(XML, "cardCode", customerProfileData.getCreditCardCode());
            endTag(XML, "creditCard");
        } else if (customerProfileData.getPaymentType() == CustomerProfileData.BANK_ACCOUNT) {
            beginTag(XML, "bankAccount");
            buildTag(XML, "accountType", customerProfileData.getAccountType());
            buildTag(XML, "routingNumber", customerProfileData.getRoutingNumber());
            buildTag(XML, "accountNumber", customerProfileData.getAccountNumber());
            buildTag(XML, "nameOnAccount", customerProfileData.getAccountName());
            buildTag(XML, "bankName", customerProfileData.getBankName());
            endTag(XML, "bankAccount");
        }
        endTag(XML, "payment");

        buildTag(XML, "customerPaymentProfileId", customerProfileData.getCustomerPaymentProfileId());
        endTag(XML, "paymentProfile");

        endTag(XML, "updateCustomerPaymentProfileRequest");

        return XML.toString();
    }

    private String buildGetCustomerProfileRequest(String customerProfileId) throws PluggableTaskException {

        StringBuffer XML = new StringBuffer();
        XML.append("<getCustomerProfileRequest xmlns=\"" + AUTHNET_XML_NAMESPACE + "\">");
        XML.append(getMerchantAuthenticationXML());
        buildTag(XML, "customerProfileId", customerProfileId);
        endTag(XML, "getCustomerProfileRequest");

        return XML.toString();
    }

    private String buildCustomerProfileTransactionRequest(BigDecimal amount, CustomerProfileData customerProfile,
                                                          String transactionType, String approvalCode)
        throws PluggableTaskException {

        StringBuffer XML = new StringBuffer();

        XML.append("<createCustomerProfileTransactionRequest xmlns=\"" + AUTHNET_XML_NAMESPACE + "\">");
        XML.append(getMerchantAuthenticationXML());

        beginTag(XML, "transaction");
        beginTag(XML, transactionType);
        buildTag(XML, "amount", amount.toString());
        buildTag(XML, "customerProfileId", customerProfile.getCustomerProfileId());
        buildTag(XML, "customerPaymentProfileId", customerProfile.getCustomerPaymentProfileId());

        if ("profileTransCaptureOnly".equals(transactionType)) {
            buildTag(XML, "approvalCode", approvalCode);
        }

        endTag(XML, transactionType);
        endTag(XML, "transaction");
        XML.append("<extraOptions><![CDATA[x_delim_char=|&x_encap_char=]]></extraOptions>");
        endTag(XML, "createCustomerProfileTransactionRequest");

        return XML.toString();
    }

    /**
     * Sends the request to the Authorize.Net payment processor
     *
     * @param data String The HTTP POST formatted as a GET string
     * @return String
     * @throws PluggableTaskException when error occured.
     */
    private String sendViaXML(String data) throws PluggableTaskException {
        int ch;
        StringBuffer responseText = new StringBuffer();
        String XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + data;

        try {
            // Set up the connection
            URL url = testMode ? new URL(AUTHNET_XML_TEST_URL) : new URL(AUTHNET_XML_PROD_URL);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("CONTENT-TYPE", "application/xml");
            conn.setConnectTimeout(timeout * 1000);
            conn.setDoOutput(true);

            LOG.debug("Sending request: " + XML);

            // Send the request
            OutputStream ostream = conn.getOutputStream();
            ostream.write(XML.getBytes());
            ostream.close();

            // Get the response
            InputStream istream = conn.getInputStream();
            while ((ch = istream.read()) != -1)
                responseText.append((char) ch);
            istream.close();
            responseText.replace(0, 3, ""); // KLUDGE: Strips erroneous chars from response stream.

            LOG.debug("Authorize.Net response: " + responseText);

            return responseText.toString();
        } catch (Exception e) {

            LOG.error(e);
            throw new PluggableTaskException(e);
        }
    }

    private PaymentAuthorizationDTO parseCustomerProfileTransactionResponse(String HTTPResponse)
        throws PluggableTaskException {

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource inStream = new InputSource();
            inStream.setCharacterStream(new StringReader(HTTPResponse));
            Document doc = builder.parse(inStream);
            doc.getDocumentElement().normalize();
            Element rootElement = doc.getDocumentElement();
            NodeList nodeLst = rootElement.getChildNodes();
            NodeList messagesNodeLst = nodeLst.item(0).getChildNodes();
            NodeList resultCodeNodeLst = messagesNodeLst.item(0).getChildNodes();
            String resultCode = resultCodeNodeLst.item(0).getNodeValue();
            NodeList messageNodeLst = messagesNodeLst.item(1).getChildNodes();
            NodeList codeNodeLst = messageNodeLst.item(0).getChildNodes();
            String code = codeNodeLst.item(0).getNodeValue();
            NodeList textNodeLst = messageNodeLst.item(1).getChildNodes();
            String text = textNodeLst.item(0).getNodeValue();

            PaymentAuthorizationDTO paymentDTO = new PaymentAuthorizationDTO();

            // check for errors
            if (!resultCode.equals("Ok")) {
                paymentDTO.setCode1(resultCode);
                paymentDTO.setCode2(code);
                paymentDTO.setResponseMessage(text);
                paymentDTO.setProcessor("PaymentAuthorizeNetCIMTask");

                return paymentDTO;
            }
            /**
             * If the response was ok the direct response node gets parsed and
             * PaymentAuthorizationDTO gets updated with the parsed values
             */
            NodeList directResponseNodeLst = nodeLst.item(1).getChildNodes();
            String response = directResponseNodeLst.item(0).getNodeValue();
            String[] responseList = response.split("\\|", -2);
            paymentDTO.setApprovalCode(responseList[4]);
            paymentDTO.setAvs(responseList[5]);
            paymentDTO.setProcessor("PaymentAuthorizeNetCIMTask");
            paymentDTO.setCode1(responseList[0]);
            paymentDTO.setCode2(responseList[1]);
            paymentDTO.setCode3(responseList[2]);
            paymentDTO.setResponseMessage(responseList[3]);
            paymentDTO.setTransactionId(responseList[6]);
            paymentDTO.setMD5(responseList[37]);
            paymentDTO.setCreateDate(Calendar.getInstance().getTime());

            return paymentDTO;
        } catch (Exception e) {

            LOG.error(e);
            throw new PluggableTaskException(e);
        }
    }

    private CustomerProfileData parseCreateCustomerProfileResponse(String HTTPResponse)
        throws PluggableTaskException, DublicateProfileRecordException {

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource inStream = new InputSource();
            inStream.setCharacterStream(new StringReader(HTTPResponse));
            Document doc = builder.parse(inStream);
            doc.getDocumentElement().normalize();
            Element rootElement = doc.getDocumentElement();
            NodeList nodeLst = rootElement.getChildNodes();
            NodeList messagesNodeLst = nodeLst.item(0).getChildNodes();
            NodeList resultCodeNodeLst = messagesNodeLst.item(0).getChildNodes();
            String resultCode = resultCodeNodeLst.item(0).getNodeValue();
            NodeList messageNodeLst = messagesNodeLst.item(1).getChildNodes();
            NodeList codeNodeLst = messageNodeLst.item(0).getChildNodes();
            String code = codeNodeLst.item(0).getNodeValue();
            NodeList textNodeLst = messageNodeLst.item(1).getChildNodes();
            String text = textNodeLst.item(0).getNodeValue();

            // check for errors
            if (!resultCode.equals("Ok")) {
                String errorMessage = String.format(
                    "Authorize.Net createCustomerProfile error: %s (code1: %s, code2: %s)",
                    text, resultCode, code);

                if (DUPLICATE_PROFILE_ERROR_CODE.equals(code)) {
                    throwDuplicateProfileError(errorMessage, text);
                }

                throw new PluggableTaskException(errorMessage);
            }

            /**
             * If the response was ok the direct response node gets parsed and
             * PaymentAuthorizationDTO gets updated with the parsed values
             */
            NodeList customerProfileIdNodeLst = nodeLst.item(1).getChildNodes();
            String customerProfileId = customerProfileIdNodeLst.item(0).getNodeValue();
            NodeList customerPaymentProfileIdListNodeLst = nodeLst.item(2).getChildNodes();
            NodeList numericStringNodeLst = customerPaymentProfileIdListNodeLst.item(0).getChildNodes();
            String customerPaymentProfileId = numericStringNodeLst.item(0).getNodeValue();

            return new CustomerProfileData(customerProfileId, customerPaymentProfileId);
        } catch (DublicateProfileRecordException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e);
            throw new PluggableTaskException(e);
        }
    }

    private CustomerProfileData parseGetCustomerProfileResponse(String HTTPResponse) throws PluggableTaskException {
        CustomerProfileData customerProfile =null;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource inStream = new InputSource();
            inStream.setCharacterStream(new StringReader(HTTPResponse));
            Document doc = builder.parse(inStream);
            doc.getDocumentElement().normalize();
            Element rootElement = doc.getDocumentElement();
            NodeList nodeLst = rootElement.getChildNodes();
            NodeList messagesNodeLst = nodeLst.item(0).getChildNodes();
            NodeList resultCodeNodeLst = messagesNodeLst.item(0).getChildNodes();
            String resultCode = resultCodeNodeLst.item(0).getNodeValue();
            NodeList messageNodeLst = messagesNodeLst.item(1).getChildNodes();
            NodeList codeNodeLst = messageNodeLst.item(0).getChildNodes();
            String code = codeNodeLst.item(0).getNodeValue();
            NodeList textNodeLst = messageNodeLst.item(1).getChildNodes();
            String text = textNodeLst.item(0).getNodeValue();

            // check for errors
            if (!resultCode.equals("Ok")) {
                String errorMessage = String.format("Authorize.Net getCustomerProfile error: %s (code1: %s, code2: %s)",
                                                    text, resultCode, code);

                throw new PluggableTaskException(errorMessage);
            }

            NodeList profileNodeLst = nodeLst.item(1).getChildNodes();
            NodeList emailNode = profileNodeLst.item(0).getChildNodes();
            NodeList customerProfileIdNode = profileNodeLst.item(1).getChildNodes();

            customerProfile = new CustomerProfileData();
            customerProfile.setCustomerProfileId(customerProfileIdNode.item(0).getNodeValue());
            customerProfile.setEmail(emailNode.item(0).getNodeValue());

            NodeList paymentProfilesNodeLst = profileNodeLst.item(2).getChildNodes();
            NodeList billToNodeLst = paymentProfilesNodeLst.item(0).getChildNodes();
            NodeList firstNameNode = billToNodeLst.item(0).getChildNodes();
            NodeList lastNameNode = billToNodeLst.item(1).getChildNodes();
            NodeList companyNode = billToNodeLst.item(2).getChildNodes();
            NodeList addressNode = billToNodeLst.item(3).getChildNodes();
            NodeList cityNode = billToNodeLst.item(4).getChildNodes();
            NodeList stateNode = billToNodeLst.item(5).getChildNodes();
            NodeList countryNode = billToNodeLst.item(6).getChildNodes();
            NodeList phoneNumberNode = billToNodeLst.item(7).getChildNodes();
            NodeList faxNumberNode = billToNodeLst.item(8).getChildNodes();

            NodeList customerPaymentProfileIdNode = paymentProfilesNodeLst.item(1).getChildNodes();

            NodeList paymentNodeLst = paymentProfilesNodeLst.item(2).getChildNodes();
            NodeList creditCardNodeLst = paymentNodeLst.item(0).getChildNodes();

            if (null != creditCardNodeLst ) {
                if( "creditCard".equalsIgnoreCase(paymentNodeLst.item(0).getNodeName()))
                {
                    NodeList cardNumberNode = creditCardNodeLst.item(0).getChildNodes();
                    NodeList expirationDateNode = creditCardNodeLst.item(1).getChildNodes();
                    customerProfile.setCreditCardNumber(cardNumberNode.item(0).getNodeValue());
                    customerProfile.setCreditCardExpirationDate(expirationDateNode.item(0).getNodeValue());
                } else if ( "bankAccount".equalsIgnoreCase(paymentNodeLst.item(0).getNodeName() )) {
                    NodeList nlAccountType= creditCardNodeLst.item(0).getChildNodes();
                    NodeList nlRoutingNumber= creditCardNodeLst.item(1).getChildNodes();
                    NodeList nlAccountNumber= creditCardNodeLst.item(2).getChildNodes();
                    NodeList nlNameOnAccount= creditCardNodeLst.item(3).getChildNodes();
                    NodeList nlBankName= creditCardNodeLst.item(4).getChildNodes();
                    customerProfile.setAccountName(nlNameOnAccount.item(0).getNodeValue());
                    customerProfile.setAccountType(nlAccountType.item(0).getNodeValue());
                    customerProfile.setAccountNumber(nlAccountNumber.item(0).getNodeValue());
                    customerProfile.setBankName(nlBankName.item(0).getNodeValue());
                    customerProfile.setRoutingNumber(nlRoutingNumber.item(0).getNodeValue());
                }
            }

//            customerProfile = new CustomerProfileData();
//            customerProfile.setCustomerProfileId(customerProfileIdNode.item(0).getNodeValue());
            customerProfile.setCustomerPaymentProfileId(customerPaymentProfileIdNode.item(0).getNodeValue());
//            customerProfile.setEmail(emailNode.item(0).getNodeValue());
            customerProfile.setFirstName(firstNameNode.item(0).getNodeValue());
            customerProfile.setLastName(lastNameNode.item(0).getNodeValue());
            customerProfile.setCompany(companyNode.item(0).getNodeValue());
            customerProfile.setAddress(addressNode.item(0).getNodeValue());
            customerProfile.setCity(cityNode.item(0).getNodeValue());
            customerProfile.setState(stateNode.item(0).getNodeValue());
            customerProfile.setCountry(countryNode.item(0).getNodeValue());
            customerProfile.setPhoneNumber(phoneNumberNode.item(0).getNodeValue());
            customerProfile.setFaxNumber(faxNumberNode.item(0).getNodeValue());

        } catch (NullPointerException e) {
            //noop
            if ( null == customerProfile ) customerProfile= new CustomerProfileData();
        } catch (Exception e) {
            LOG.error(e);
            throw new PluggableTaskException(e);
        }
        return customerProfile;
    }

    private void parseSimpleResponse(String HTTPResponse, String request) throws PluggableTaskException {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource inStream = new InputSource();
            inStream.setCharacterStream(new StringReader(HTTPResponse));
            Document doc = builder.parse(inStream);
            doc.getDocumentElement().normalize();
            Element rootElement = doc.getDocumentElement();
            NodeList nodeLst = rootElement.getChildNodes();
            NodeList messagesNodeLst = nodeLst.item(0).getChildNodes();
            NodeList resultCodeNodeLst = messagesNodeLst.item(0).getChildNodes();
            String resultCode = resultCodeNodeLst.item(0).getNodeValue();
            NodeList messageNodeLst = messagesNodeLst.item(1).getChildNodes();
            NodeList codeNodeLst = messageNodeLst.item(0).getChildNodes();
            String code = codeNodeLst.item(0).getNodeValue();
            NodeList textNodeLst = messageNodeLst.item(1).getChildNodes();
            String text = textNodeLst.item(0).getNodeValue();

            // check for errors
            if (!resultCode.equals("Ok")) {
                String errorMessage = String.format("Authorize.Net %s error: %s (code1: %s, code2: %s)",
                                                    request, text, resultCode, code);

                throw new PluggableTaskException(errorMessage);
            }
        } catch (Exception e) {
            LOG.error(e);
            throw new PluggableTaskException(e);
        }
    }

    private String parseCreateCustomerPaymentProfileResponse(String HTTPResponse)
        throws PluggableTaskException, DublicateProfileRecordException {

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource inStream = new InputSource();
            inStream.setCharacterStream(new StringReader(HTTPResponse));
            Document doc = builder.parse(inStream);
            doc.getDocumentElement().normalize();
            Element rootElement = doc.getDocumentElement();
            NodeList nodeLst = rootElement.getChildNodes();
            NodeList messagesNodeLst = nodeLst.item(0).getChildNodes();
            NodeList resultCodeNodeLst = messagesNodeLst.item(0).getChildNodes();
            String resultCode = resultCodeNodeLst.item(0).getNodeValue();
            NodeList messageNodeLst = messagesNodeLst.item(1).getChildNodes();
            NodeList codeNodeLst = messageNodeLst.item(0).getChildNodes();
            String code = codeNodeLst.item(0).getNodeValue();
            NodeList textNodeLst = messageNodeLst.item(1).getChildNodes();
            String text = textNodeLst.item(0).getNodeValue();

            // check for errors
            if (!resultCode.equals("Ok")) {
                String errorMessage = String.format(
                    "Authorize.Net createCustomerPaymentProfile error: %s (code1: %s, code2: %s)",
                    text, resultCode, code);

                if (DUPLICATE_PROFILE_ERROR_CODE.equals(code)) {
                    throwDuplicateProfileError(errorMessage, text);
                }
                throw new PluggableTaskException(errorMessage);
            }

            /**
             * If the response was ok the direct response node gets parsed and
             * a new Customer Payment Profile ID gets returned with the parsed values
             */
            NodeList customerPaymentProfileIdNodeLst = nodeLst.item(1).getChildNodes();
            String customerPaymentProfileId = customerPaymentProfileIdNodeLst.item(0).getNodeValue();

            return customerPaymentProfileId;
        } catch (DublicateProfileRecordException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e);
            throw new PluggableTaskException(e);
        }
    }

    private static void throwDuplicateProfileError(String errorMessage, String serverErrorMessage)
        throws DublicateProfileRecordException {

        int idStart = serverErrorMessage.indexOf(DUBLICATE_PROFILE_ID_PREFIX) + DUBLICATE_PROFILE_ID_PREFIX.length();
        int idEnd = serverErrorMessage.indexOf(' ', idStart);
        String profileId = serverErrorMessage.substring(idStart, idEnd);

        throw new DublicateProfileRecordException(profileId, errorMessage);
    }

    /*public static void main(String args[]) throws Exception {
        AuthorizeNetCIMApi api= new AuthorizeNetCIMApi("", "",
                "none",true, 10);
        String XML = api.buildGetCustomerProfileRequest("");
        System.out.println("REQUEST\n" + XML);
        String HTTPResponse = api.sendViaXML(XML);
        System.out.println("RESPONSE\n"+HTTPResponse);
    }*/

}
