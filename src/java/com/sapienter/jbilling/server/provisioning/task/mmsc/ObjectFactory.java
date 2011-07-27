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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.sapienter.jbilling.server.provisioning.task.mmsc package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _DeleteCustomer_QNAME = new QName("http://mmschandlerfacade.efs.teliasonera.se/", "deleteCustomer");
    private final static QName _GetCustomerInfo_QNAME = new QName("http://mmschandlerfacade.efs.teliasonera.se/", "getCustomerInfo");
    private final static QName _GetCustomerInfoResponse_QNAME = new QName("http://mmschandlerfacade.efs.teliasonera.se/", "getCustomerInfoResponse");
    private final static QName _ModifyCustomerResponse_QNAME = new QName("http://mmschandlerfacade.efs.teliasonera.se/", "modifyCustomerResponse");
    private final static QName _AddCustomer_QNAME = new QName("http://mmschandlerfacade.efs.teliasonera.se/", "addCustomer");
    private final static QName _DeleteCustomerResponse_QNAME = new QName("http://mmschandlerfacade.efs.teliasonera.se/", "deleteCustomerResponse");
    private final static QName _Exception_QNAME = new QName("http://mmschandlerfacade.efs.teliasonera.se/", "Exception");
    private final static QName _AddCustomerResponse_QNAME = new QName("http://mmschandlerfacade.efs.teliasonera.se/", "addCustomerResponse");
    private final static QName _ModifyCustomer_QNAME = new QName("http://mmschandlerfacade.efs.teliasonera.se/", "modifyCustomer");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.sapienter.jbilling.server.provisioning.task.mmsc
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EfsBaseObject }
     * 
     */
    public EfsBaseObject createEfsBaseObject() {
        return new EfsBaseObject();
    }

    /**
     * Create an instance of {@link EfsBaseResponse }
     * 
     */
    public EfsBaseResponse createEfsBaseResponse() {
        return new EfsBaseResponse();
    }

    /**
     * Create an instance of {@link GetCustomerInfoResponse }
     * 
     */
    public GetCustomerInfoResponse createGetCustomerInfoResponse() {
        return new GetCustomerInfoResponse();
    }

    /**
     * Create an instance of {@link ModifyCustomerRequest }
     * 
     */
    public ModifyCustomerRequest createModifyCustomerRequest() {
        return new ModifyCustomerRequest();
    }

    /**
     * Create an instance of {@link GetCustomerResponse.CustomerData }
     * 
     */
    public GetCustomerResponse.CustomerData createGetCustomerResponseCustomerData() {
        return new GetCustomerResponse.CustomerData();
    }

    /**
     * Create an instance of {@link EfsBaseMSISDNRequest }
     * 
     */
    public EfsBaseMSISDNRequest createEfsBaseMSISDNRequest() {
        return new EfsBaseMSISDNRequest();
    }

    /**
     * Create an instance of {@link EfsBaseRequest }
     * 
     */
    public EfsBaseRequest createEfsBaseRequest() {
        return new EfsBaseRequest();
    }

    /**
     * Create an instance of {@link DeleteCustomerRequest }
     * 
     */
    public DeleteCustomerRequest createDeleteCustomerRequest() {
        return new DeleteCustomerRequest();
    }

    /**
     * Create an instance of {@link AddCustomerResponse }
     * 
     */
    public AddCustomerResponse createAddCustomerResponse() {
        return new AddCustomerResponse();
    }

    /**
     * Create an instance of {@link GetCustomerRequest }
     * 
     */
    public GetCustomerRequest createGetCustomerRequest() {
        return new GetCustomerRequest();
    }

    /**
     * Create an instance of {@link ModifyCustomer }
     * 
     */
    public ModifyCustomer createModifyCustomer() {
        return new ModifyCustomer();
    }

    /**
     * Create an instance of {@link ModifyCustomerResponse }
     * 
     */
    public ModifyCustomerResponse createModifyCustomerResponse() {
        return new ModifyCustomerResponse();
    }

    /**
     * Create an instance of {@link AddCustomer }
     * 
     */
    public AddCustomer createAddCustomer() {
        return new AddCustomer();
    }

    /**
     * Create an instance of {@link DeleteCustomerResponse }
     * 
     */
    public DeleteCustomerResponse createDeleteCustomerResponse() {
        return new DeleteCustomerResponse();
    }

    /**
     * Create an instance of {@link GetCustomerResponse.CustomerData.Entry }
     * 
     */
    public GetCustomerResponse.CustomerData.Entry createGetCustomerResponseCustomerDataEntry() {
        return new GetCustomerResponse.CustomerData.Entry();
    }

    /**
     * Create an instance of {@link DeleteCustomer }
     * 
     */
    public DeleteCustomer createDeleteCustomer() {
        return new DeleteCustomer();
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public MMSCException createException() {
        return new MMSCException();
    }

    /**
     * Create an instance of {@link MmscFacadeHandlerResponse }
     * 
     */
    public MmscFacadeHandlerResponse createMmscFacadeHandlerResponse() {
        return new MmscFacadeHandlerResponse();
    }

    /**
     * Create an instance of {@link AddCustomerRequest }
     * 
     */
    public AddCustomerRequest createAddCustomerRequest() {
        return new AddCustomerRequest();
    }

    /**
     * Create an instance of {@link GetCustomerInfo }
     * 
     */
    public GetCustomerInfo createGetCustomerInfo() {
        return new GetCustomerInfo();
    }

    /**
     * Create an instance of {@link GetCustomerResponse }
     * 
     */
    public GetCustomerResponse createGetCustomerResponse() {
        return new GetCustomerResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteCustomer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mmschandlerfacade.efs.teliasonera.se/", name = "deleteCustomer")
    public JAXBElement<DeleteCustomer> createDeleteCustomer(DeleteCustomer value) {
        return new JAXBElement<DeleteCustomer>(_DeleteCustomer_QNAME, DeleteCustomer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCustomerInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mmschandlerfacade.efs.teliasonera.se/", name = "getCustomerInfo")
    public JAXBElement<GetCustomerInfo> createGetCustomerInfo(GetCustomerInfo value) {
        return new JAXBElement<GetCustomerInfo>(_GetCustomerInfo_QNAME, GetCustomerInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCustomerInfoResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mmschandlerfacade.efs.teliasonera.se/", name = "getCustomerInfoResponse")
    public JAXBElement<GetCustomerInfoResponse> createGetCustomerInfoResponse(GetCustomerInfoResponse value) {
        return new JAXBElement<GetCustomerInfoResponse>(_GetCustomerInfoResponse_QNAME, GetCustomerInfoResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ModifyCustomerResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mmschandlerfacade.efs.teliasonera.se/", name = "modifyCustomerResponse")
    public JAXBElement<ModifyCustomerResponse> createModifyCustomerResponse(ModifyCustomerResponse value) {
        return new JAXBElement<ModifyCustomerResponse>(_ModifyCustomerResponse_QNAME, ModifyCustomerResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddCustomer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mmschandlerfacade.efs.teliasonera.se/", name = "addCustomer")
    public JAXBElement<AddCustomer> createAddCustomer(AddCustomer value) {
        return new JAXBElement<AddCustomer>(_AddCustomer_QNAME, AddCustomer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteCustomerResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mmschandlerfacade.efs.teliasonera.se/", name = "deleteCustomerResponse")
    public JAXBElement<DeleteCustomerResponse> createDeleteCustomerResponse(DeleteCustomerResponse value) {
        return new JAXBElement<DeleteCustomerResponse>(_DeleteCustomerResponse_QNAME, DeleteCustomerResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Exception }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mmschandlerfacade.efs.teliasonera.se/", name = "Exception")
    public JAXBElement<MMSCException> createException(MMSCException value) {
        return new JAXBElement<MMSCException>(_Exception_QNAME, MMSCException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddCustomerResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mmschandlerfacade.efs.teliasonera.se/", name = "addCustomerResponse")
    public JAXBElement<AddCustomerResponse> createAddCustomerResponse(AddCustomerResponse value) {
        return new JAXBElement<AddCustomerResponse>(_AddCustomerResponse_QNAME, AddCustomerResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ModifyCustomer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mmschandlerfacade.efs.teliasonera.se/", name = "modifyCustomer")
    public JAXBElement<ModifyCustomer> createModifyCustomer(ModifyCustomer value) {
        return new JAXBElement<ModifyCustomer>(_ModifyCustomer_QNAME, ModifyCustomer.class, null, value);
    }

}
