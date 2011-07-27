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

package com.sapienter.jbilling.server.util.csv;

import au.com.bytecode.opencsv.CSVWriter;
import com.sapienter.jbilling.server.util.converter.BigDecimalConverter;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;

/**
 * CsvExporter
 *
 * @author Brian Cowdery
 * @since 03/03/11
 */
public class CsvExporter<T extends Exportable> implements Exporter<T> {

    private static final Logger LOG = Logger.getLogger(CsvExporter.class);

    /** The maximum safe number of exportable elements to processes.  */
    public static final Integer MAX_RESULTS = 10000;

    static {
        ConvertUtils.register(new BigDecimalConverter(), BigDecimal.class);
    }

    private Class<T> type;

    private CsvExporter(Class<T> type) {
        this.type = type;
    }

    /**
     * Factory method to produce a new instance of CsvExporter for the given type.
     *
     * @param type type of exporter
     * @param <T> type T
     * @return new exporter of type T
     */
    public static <T extends Exportable> CsvExporter<T> createExporter(Class<T> type) {
        return new CsvExporter<T>(type);
    }

    public Class<T> getType() {
        return type;
    }

    public String export(List<? extends Exportable> list) {
        String[] header;

        // list can be empty, instantiate a new instance of type to
        // extract the field names for the CSV header
        try {
            header = type.newInstance().getFieldNames();
        } catch (InstantiationException e) {
            LOG.debug("Could not produce a new instance of " + type.getSimpleName() + " to build CSV header.");
            return null;

        } catch (IllegalAccessException e) {
            LOG.debug("Constructor of " + type.getSimpleName() + " is not accessible to build CSV header.");
            return null;
        }

        StringWriter out = new StringWriter();
        CSVWriter writer = new CSVWriter(out);
        writer.writeNext(header);

        for (Exportable exportable : list) {
            for (Object[] values : exportable.getFieldValues()) {
                writer.writeNext(convertToString(values));
            }
        }

        try {
            writer.close();
            out.close();
        } catch (IOException e) {
            LOG.debug("Writer cannot be closed, exported CSV may be missing data.");
        }

        return out.toString();
    }

    public String[] convertToString(Object[] objects) {
        String[] strings = new String[objects.length];

        int i = 0;
        for (Object object : objects) {
            if (object != null) {
                Converter converter = ConvertUtils.lookup(object.getClass());
                strings[i++] = converter.convert(object.getClass(), object).toString();
            } else {
                strings[i++] = "";
            }
        }

        return strings;
    }
}
