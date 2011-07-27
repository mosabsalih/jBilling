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

package com.sapienter.jbilling.server.mediation.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.List;

import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.item.PricingField;
import com.sapienter.jbilling.server.mediation.Format;
import com.sapienter.jbilling.server.mediation.FormatField;
import com.sapienter.jbilling.server.mediation.Record;
import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.digester.Digester;

public abstract class AbstractFileReader extends AbstractReader {

    private String directory;
    private String suffix;
    private boolean rename;
    private SimpleDateFormat dateFormat;
    private boolean removeQuote;
    private boolean autoID;
    private static final Logger LOG = Logger.getLogger(AbstractFileReader.class);
    private String formatFileName = null;
    protected Format format = null;
    private int bufferSize;

    public AbstractFileReader() {
    }

    public static final ParameterDescription PARAMETER_FORMAT_FILE =
    	new ParameterDescription("format_file", true, ParameterDescription.Type.STR);

    // optionals
    public static final ParameterDescription PARAMETER_FORMAT_DIRECTORY =
    	new ParameterDescription("format_directory", false, ParameterDescription.Type.STR);

    public static final ParameterDescription PARAMETER_DIRECTORY =
    	new ParameterDescription("directory", false, ParameterDescription.Type.STR);

    public static final ParameterDescription PARAMETER_SUFFIX =
    	new ParameterDescription("suffix", false, ParameterDescription.Type.STR);

    public static final ParameterDescription PARAMETER_RENAME =
    	new ParameterDescription("rename", false, ParameterDescription.Type.STR);

    public static final ParameterDescription PARAMETER_DATE_FORMAT =
    	new ParameterDescription("date_format", false, ParameterDescription.Type.STR);

    public static final ParameterDescription PARAMETER_REMOVE_QUOTE =
    	new ParameterDescription("removeQuote", false, ParameterDescription.Type.STR);

    public static final ParameterDescription PARAMETER_AUTO_ID =
    	new ParameterDescription("autoID", false, ParameterDescription.Type.STR);

    public static final ParameterDescription PARAMETER_BUFFER_SIZE =
    	new ParameterDescription("buffer_size", false, ParameterDescription.Type.STR);

    //initializer for pluggable params
    {
    	descriptions.add(PARAMETER_FORMAT_FILE);
        descriptions.add(PARAMETER_FORMAT_DIRECTORY);
        descriptions.add(PARAMETER_DIRECTORY);
		descriptions.add(PARAMETER_SUFFIX);
		descriptions.add(PARAMETER_RENAME);
		descriptions.add(PARAMETER_DATE_FORMAT);
		descriptions.add(PARAMETER_REMOVE_QUOTE);
		descriptions.add(PARAMETER_AUTO_ID);
		descriptions.add(PARAMETER_BUFFER_SIZE);
    }

    @Override
    public boolean validate(List<String> messages) {
        boolean retValue = super.validate(messages);

        String formatFile = getParameter(PARAMETER_FORMAT_FILE.getName(), (String) null);
        String formatDirectory = getParameter(PARAMETER_FORMAT_DIRECTORY.getName(), Util.getSysProp("base_dir") + "mediation");

        if (formatFile == null) {
            messages.add("parameter format_file is required");
            return false;
        }

        formatFileName = formatDirectory + File.separator + formatFile;

        // optionals
        directory = getParameter(PARAMETER_DIRECTORY.getName(), Util.getSysProp("base_dir") + "mediation");

        if (directory == null) {
            messages.add("The plug-in parameter 'directory' is mandatory");
            retValue = false;
        }

        suffix = getParameter(PARAMETER_SUFFIX.getName(), "ALL");
        rename = getParameter(PARAMETER_RENAME.getName(), false);
        dateFormat = new SimpleDateFormat(getParameter(PARAMETER_DATE_FORMAT.getName(), "yyyyMMdd-HHmmss"));
        removeQuote = getParameter(PARAMETER_REMOVE_QUOTE.getName(), true);
        autoID = getParameter(PARAMETER_AUTO_ID.getName(), false);

        try {
            bufferSize = getParameter(PARAMETER_BUFFER_SIZE.getName(), 0);
        } catch (PluggableTaskException e) {
            messages.add(e.getMessage());
        }

        LOG.debug("Started with " + " directory: " + directory + " suffix " + suffix + " rename " +
                rename + " date  format " + dateFormat.toPattern() + " removeQuote " + removeQuote +
                " autoID " + autoID);

        return retValue;
    }
    protected Format getFormat() throws IOException, SAXException {
        // parse the XML ...
        // create a field object per field element
        if (format == null) {
            Digester digester = new Digester();
            digester.setValidating(true);
            digester.setUseContextClassLoader(true);
            digester.addObjectCreate("format", "com.sapienter.jbilling.server.mediation.Format");
            digester.addObjectCreate("format/field", "com.sapienter.jbilling.server.mediation.FormatField");
            digester.addCallMethod("format/field/name","setName",0);
            digester.addCallMethod("format/field/type","setType",0);
            digester.addCallMethod("format/field/startPosition","setStartPosition",0);
            digester.addCallMethod("format/field/durationFormat","setDurationFormat",0);
            digester.addCallMethod("format/field/length","setLength",0);
            digester.addCallMethod("format/field/isKey","isKeyTrue");
            digester.addSetNext("format/field", "addField", "com.sapienter.jbilling.server.mediation.FormatField");

            format = (Format) digester.parse(new File(formatFileName));

            LOG.debug("using format: " + format);
        }

        return format;

    }


    @Override
    public Iterator<List<Record>> iterator() {
        try {
            return new Reader();
        } catch (Exception e) {
            throw new SessionInternalError(e);
        }
    }

    /**
     * This sorts the files so the oldest is processed first, and the newest last
     */
    public class Reader implements Iterator<List<Record>> {
        private final Logger LOG = Logger.getLogger(Reader.class);
        private File[] files = null;
        private int fileIndex = 0;
        private List<Record> records = null;
        private BufferedReader reader = null;
        private int counter;
        protected final Format format;

        protected Reader() throws FileNotFoundException, IOException, SAXException {
            files = new File(directory).listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    if (suffix.equalsIgnoreCase("all") || pathname.getName().endsWith(suffix)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            // sort the files, so the oldest is processed first
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File o1, File o2) {
                    return new Long(o1.lastModified()).compareTo(o2.lastModified());
                }
            });

            if (!nextReader()) {
                LOG.info("No files found to process");
                format = null;
            } else {
                LOG.debug("Files to process = " + files.length);
                format = getFormat();
                counter = 0;
                records = new ArrayList<Record>(getBatchSize());
            }
        }

        /**
         * Get the next set or records
         * @return true if there are some, otherwise false
         */
        public boolean hasNext() {
            if (reader == null) {
                return false;
            }

            records.clear();
            String line = readLine();
            int startedAt = 0;
            while (line != null) {
                counter++; // it read one just now
                // convert this line to a Record
                records.add(convertLineToRecord(line));
                if (++startedAt >= getBatchSize()) {
                    break;
                }
                line = readLine();
            }

            return records.size() > 0;
        }

        /**
         * Reads the next line from the current file, or closes this file and
         * starts with the next one.
         * @return The line read, or null if there are not any others.
         */
        private String readLine() {
            try {
                String line = reader.readLine();
                if (line == null) {
                    // we are done with this file
                    reader.close();
                    // rename it to avoid re-processing, if configured
                    if (rename) {
                        if (!files[fileIndex].renameTo(
                                new File(files[fileIndex].getAbsolutePath() + ".done"))) {
                            LOG.warn("Could not rename file " + files[fileIndex].getName());
                        }
                    }
                    // reached the last line, go to the next file
                    if (!nextReader()) {
                        return null; // all done then
                    } else {
                        // read the first line from the next file
                        line = reader.readLine();
                        counter = 0;
                    }
                }
                return line;
            } catch (Exception e) {
                throw new SessionInternalError(e);
            }
        }

        /**
         * Returns the records read since the last call to 'hasNext'
         */
        public List<Record> next() {
            if (records.size() == 0) {
                throw new NoSuchElementException();
            }

            return records;
        }

        private Record convertLineToRecord(String line) {
            // get the raw fields from the line
            String tokens[] = splitFields(line);
            if (tokens.length != format.getFields().size() && !autoID) {
                throw new SessionInternalError("Mismatch of number of fields between " +
                        "the format and the file for line " + line + " Expected " +
                        format.getFields().size() + " found " + tokens.length);

            }
            // remove quotes if needed
            if (removeQuote) {
                for (int f = 0; f < tokens.length; f++) {
                    if (tokens[f].length() < 2) {
                        continue;
                    }
                    // remove first and last char, if they are quotes
                    if ((tokens[f].charAt(0) == '\"' || tokens[f].charAt(0) == '\'') &&
                            (tokens[f].charAt(tokens[f].length() - 1) == '\"' || tokens[f].charAt(tokens[f].length() - 1) == '\'')) {
                        tokens[f] = tokens[f].substring(1, tokens[f].length() - 1);
                    }
                }
            }

            // create the record
            Record record = new Record();
            int tkIdx = 0;
            for (FormatField field:format.getFields()) {

                if (autoID && field.getIsKey()) {
                    record.addField(new PricingField(field.getName(),
                                files[fileIndex].getName() + "-" + counter ), field.getIsKey());
                    tkIdx++;
                    continue;
                }

                switch (PricingField.mapType(field.getType())) {
                    case STRING:
                        record.addField(new PricingField(field.getName(),
                                tokens[tkIdx++]), field.getIsKey());
                        break;
                    case INTEGER:
                        String intStr = tokens[tkIdx++].trim();
                        if (field.getDurationFormat() != null && field.getDurationFormat().length() > 0) {
                            // requires hour/minute conversion
                            record.addField(new PricingField(field.getName(), intStr.length() > 0 ?
                                    convertDuration(intStr, field.getDurationFormat()) : null),
                                        field.getIsKey());
                        } else {
                            try {
                                record.addField(new PricingField(field.getName(), intStr.length() > 0 ?
                                        Integer.valueOf(intStr.trim()) : null), field.getIsKey());
                            } catch (NumberFormatException e) {
                                throw new SessionInternalError("Converting to integer " + field +
                                        " line " + line, AbstractFileReader.class, e);
                            }
                        }
                        break;
                    case DATE:
                        try {
                            String dateStr = tokens[tkIdx++];
                            record.addField(new PricingField(field.getName(), dateStr.length() > 0 ?
                                    dateFormat.parse(dateStr) : null), field.getIsKey());
                        } catch (ParseException e) {
                            throw new SessionInternalError("Using format: " + dateFormat + "[" +
                                    parameters.get(PARAMETER_DATE_FORMAT.getName()) + "]",
                                    AbstractFileReader.class,e);
                        }
                        break;
                    case DECIMAL:
                        String floatStr = tokens[tkIdx++].trim();
                        record.addField(new PricingField(field.getName(), floatStr.length() > 0 ?
                                new BigDecimal(floatStr) : null), field.getIsKey());
                        break;
                    case BOOLEAN:
                        boolean value = "true".equalsIgnoreCase(tokens[tkIdx++].trim());
                        record.addField(new PricingField(field.getName(), value), field.getIsKey());
                        break;
                }
            }

            record.setPosition(counter);
            return record;
        }

        private boolean nextReader() throws FileNotFoundException {
            if (reader != null) { // first call
                fileIndex++;
            }

            if (files.length > fileIndex) { // any more to process ?
                if (bufferSize > 0) {
                    reader = new BufferedReader(new java.io.FileReader(files[fileIndex]), bufferSize);
                } else {
                    reader = new BufferedReader(new java.io.FileReader(files[fileIndex]));
                }
                LOG.debug("Now processing file " + files[fileIndex].getName());
                return true;
            }

            reader = null;
            return false;
        }

        public void remove() {
            // needed to comply with Iterator only
            throw new SessionInternalError("remove not supported");
        }
    }

    /**
     * Chars 'H', 'M' and 'S' have to be grouped or the behaviour will be unexpected
     * @param content
     * @param format
     * @return
     */
    public static int convertDuration(String content, String format) {
        int totalSeconds = 0;
        // hours

        try {
            try {
                totalSeconds += Integer.valueOf(content.substring(format.indexOf('H'),
                        format.lastIndexOf('H') + 1).trim()) * 60 * 60;
            } catch (IndexOutOfBoundsException e) {
                // no hours. ok
            }
            // minutes
            try {
                totalSeconds += Integer.valueOf(content.substring(format.indexOf('M'),
                        format.lastIndexOf('M') + 1).trim()) * 60;
            } catch (IndexOutOfBoundsException e) {
                // no minutes. ok
            }
            // seconds
            try {
                totalSeconds += Integer.valueOf(content.substring(format.indexOf('S'),
                        format.lastIndexOf('S') + 1).trim());
            } catch (IndexOutOfBoundsException e) {
                // no seconds. ok
            }
        } catch (NumberFormatException e) {
            throw new SessionInternalError("converting duration format " + format + " content " + content,
                    AbstractFileReader.class, e);
        }

        return totalSeconds;
    }

    protected abstract String[] splitFields(String line);
}
