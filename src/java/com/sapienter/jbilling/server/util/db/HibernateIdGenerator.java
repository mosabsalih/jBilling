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
package com.sapienter.jbilling.server.util.db;

import com.sapienter.jbilling.server.util.Context;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.IdentifierGeneratorFactory;
import org.hibernate.id.enhanced.TableGenerator;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.impl.StatelessSessionImpl;
import org.hibernate.type.IntegerType;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Properties;

/**
 * Generates a new ID from the jbilling sequence table.
 *
 * Example:
 * <code>
 *      HibernateIdGenerator gen = new HibernateIdGenerator("purchase_order");
 *      gen.getId(); // returns 1
 *      gen.getId(); // returns 2, etc.
 * </code
 *
 * @author Brian Cowdery
 * @since 01-04-2010
 */
public class HibernateIdGenerator {
    
    private IdentifierGenerator generator;
    private SessionFactory sessionFactory;

    /**
     * Constructs a new ID generator for the given segment. If the segment does
     * not exist in the sequence table, it will be created with the newly generated
     * id values starting from zero.
     *      *
     * @param segmentValue jbilling sequence name (value of the 'name' column)
     */
    public HibernateIdGenerator(String segmentValue) {
        /*
            I consider this code to be a "horrific sin against nature and a total affront to the programming gods",
            but it's the only way to gain access to Hibernates IdentifierGenerator framework. Future versions
            of Hibernate may change the underlying implementation which will break this code.
         */
        Properties configuration = new Properties();
        configuration.setProperty(TableGenerator.TABLE_PARAM, "jbilling_seqs");
        configuration.setProperty(TableGenerator.SEGMENT_COLUMN_PARAM, "name");
        configuration.setProperty(TableGenerator.SEGMENT_VALUE_PARAM, segmentValue);
        configuration.setProperty(TableGenerator.VALUE_COLUMN_PARAM, "next_id");
        configuration.setProperty(TableGenerator.INCREMENT_PARAM, "100");

        sessionFactory = ((SessionFactory) Context.getBean(Context.Name.HIBERNATE_SESSION));
        generator = IdentifierGeneratorFactory.create("org.hibernate.id.enhanced.TableGenerator",
                                                      new IntegerType(),
                                                      configuration,
                                                      ((SessionFactoryImpl) sessionFactory).getDialect());
    }

    public Serializable getId() {
        StatelessSession session = sessionFactory.openStatelessSession();
        Serializable id = generator.generate((StatelessSessionImpl) session, new ID());
        session.close();
        return id;
    }

    /**
     * Target object for ID generation
     */
    private static class ID {
        private Integer id;
        
        @Id
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }
}
