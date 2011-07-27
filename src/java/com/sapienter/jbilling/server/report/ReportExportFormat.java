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

package com.sapienter.jbilling.server.report;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * ExportType
 *
 * @author Brian Cowdery
 * @since 09/03/11
 */
public enum ReportExportFormat {

    PDF {
        public ReportExportDTO export(JasperPrint print) throws JRException, IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            JasperExportManager.exportReportToPdfStream(print, baos);

            byte[] bytes = baos.toByteArray();
            baos.close();

            return new ReportExportDTO(print.getName() + ".pdf", "application/pdf", bytes);
        }
    },

    XLS {
        public ReportExportDTO export(JasperPrint print) throws JRException, IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            JExcelApiExporter exporter = new JExcelApiExporter();
            exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, print);
            exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, true);
            exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, false);
            exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, true);
            exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, baos);

            exporter.exportReport();

            byte[] bytes = baos.toByteArray();
            baos.close();

            return new ReportExportDTO(print.getName() + ".xls", "application/vnd.ms-excel", bytes);
        }
    };

    public abstract ReportExportDTO export(JasperPrint print) throws JRException, IOException;

}
