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
package com.sapienter.jbilling.server.util;

import org.springframework.context.ApplicationContext;

/**
 * Static factory for accessing Spring beans from the local container.
 */
public class Context {

    // spring application context, injected by context aware clients
    private static ApplicationContext spring = null;

    // defined bean names
    public enum Name {
        // jbilling session beans
        ITEM_SESSION                    ("itemSession"),
        NOTIFICATION_SESSION            ("notificationSession"),
        CUSTOMER_SESSION                ("customerSession"),
        LIST_SESSION                    ("listSession"),
        USER_SESSION                    ("userSession"),
        INVOICE_SESSION                 ("invoiceSession"),
        ORDER_SESSION                   ("orderSession"),
        PLUGGABLE_TASK_SESSION          ("pluggableTaskSession"),
        PAYMENT_SESSION                 ("paymentSession"),
        MEDIATION_SESSION               ("mediationSession"),
        BILLING_PROCESS_SESSION         ("billingProcessSession"),
        PROVISIONING_PROCESS_SESSION    ("provisioningProcessSession"),
        WEB_SERVICES_SESSION            ("webServicesSession"),

        // jbilling data access service beans
        DESCRIPTION_DAS     ("internationalDescriptionDAS"),
        JBILLING_TABLE_DAS  ("jbillingTableDAS"),
        PLUGGABLE_TASK_DAS  ("pluggableTaskDAS"),

        // jbilling beans
        PROVISIONING                        ("provisioning"),
        INTERNAL_EVENTS_RULES_TASK_CONFIG   ("internalEventsRulesTaskConfig"),

        // persistence
        DATA_SOURCE         ("dataSource"),
        TRANSACTION_MANAGER ("transactionManager"),
        HIBERNATE_SESSION   ("sessionFactory"),
        JDBC_TEMPLATE       ("jdbcTemplate"),

        // security
        SPRING_SECURITY_SERVICE ("springSecurityService"),
        AUTHENTICATION_MANAGER  ("authenticationManager"),
        PASSWORD_ENCODER        ("passwordEncoder"),

        // cache
        CACHE                           ("cacheProviderFacade"),
        CACHE_MODEL_ITEM_PRICE          ("cacheModelItemPrice"),
        CACHE_FLUSH_MODEL_ITEM_PRICE    ("flushModelItemPrice"),
        CACHE_MODEL_READONLY            ("cacheModelReadOnly"),
        CACHE_MODEL_RW                  ("cacheModelPTDTO"),
        CACHE_FLUSH_MODEL_RW            ("flushModelPTDTO"),

        // jms
        JMS_TEMPLATE                            ("jmsTemplate"),
        PROCESSORS_DESTINATION                  ("processorsDestination"),
        PROVISIONING_COMMANDS_DESTINATION       ("provisioningCommandsDestination"),
        PROVISIONING_COMMANDS_REPLY_DESTINATION ("provisioningCommandsReplyDestination"),

        // misc
        CAI                 ("cai"),
        MMSC                ("mmsc"),
        VELOCITY            ("velocityEngine");
       
        private String name;
        Name(String name) { this.name = name; }
        public String getName() { return name; }
    }

    // static factory cannot be instantiated
    private Context() {
    }

    public static void setApplicationContext(ApplicationContext ctx) {
        spring = ctx;
    }

    public static ApplicationContext getApplicationContext() {
        return spring;
    }

    /**
     * Returns a Spring Bean of type T for the given Context.Name
     *
     * @param bean remote context name
     * @param <T> bean type
     * @return bean from remote context
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Name bean) {
        return (T) getApplicationContext().getBean(bean.getName());
    }

    /**
     * Returns a Spring Bean of type T for the given name
     *
     * @param beanName bean name
     * @param <T> bean type
     * @return bean from remote context
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) {
        return (T)  getApplicationContext().getBean(beanName);
    }
}
