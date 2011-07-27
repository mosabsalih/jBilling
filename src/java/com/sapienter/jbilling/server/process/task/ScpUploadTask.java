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

package com.sapienter.jbilling.server.process.task;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.rev6.scf.ScpException;
import org.rev6.scf.ScpFacade;
import org.rev6.scf.ScpFile;

import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;

/**
 * The ScpUploadTask will scan a defined path for files to upload using a matcher regular expression.
 *
 * Each sub directory of the given "file_path" is treated as an uploadable batch of files. This task
 * is designed to be used with export plug-ins where exports are organized into separate directories
 *
 * eg:
 *      invoices/xml/     <- plug-in file_path
 *          2010-05-01/
 *          2010-05-02/
 *          2010-05-03/
 *
 * When a batch of files is uploaded, the ScpUploadTask creates an upload "marker" file that it looks
 * for when scanning for uploadable files. If the ScpUploadTask encounters one of these "marker" files,
 * it simply skips over the directory containing it. The marker file is an easy way to exclude a directory
 * from being scanned and uploaded - By the same token, you can have the task re-upload the contents of a
 * directory by removing the marker file.
 *
 * Plug-in parameters:
 *
 *      cron_exp            Cron expression for the scheduled task (required)
 *
 *      scp_username        User name of the shell account on the remote host (required)
 *      scp_password        Password of the shell account on the remote host (required)
 *      scp_host            Remote host (required)
 *      scp_remote_path     Optional remote path to upload files to. Defaults to the users home directory.
 *
 *      file_path           Path to scan for uploadable files - Must be an absolute path! (required)
 *      file_pattern        Optional regular expression to match specific files in the path. Defaults to .*
 *      recursive           [true|false] If true, recursively scan all child directories within file_path.
 *                          Defaults to false
 *
 *      upload_log          File name of the upload "marker" file to generate when marking a folder as uploaded.
 *                          Defaults to "upload.log"
 *
 * @author Brian Cowdery
 * @since 08-06-2010
 */
public class ScpUploadTask extends AbstractCronTask {
	
    private static final Logger LOG = Logger.getLogger(ScpUploadTask.class);

    private static final ParameterDescription PARAM_SCP_USERNAME = 
    	new ParameterDescription("scp_username", true, ParameterDescription.Type.STR);
    private static final ParameterDescription PARAM_SCP_PASSWORD = 
    	new ParameterDescription("scp_password", true, ParameterDescription.Type.STR);
    private static final ParameterDescription PARAM_SCP_HOST = 
    	new ParameterDescription("scp_host", true, ParameterDescription.Type.STR);
    private static final ParameterDescription PARAM_SCP_REMOTE_PATH = 
    	new ParameterDescription("scp_remote_path", false, ParameterDescription.Type.STR);

    private static final ParameterDescription PARAM_FILE_PATH = 
    	new ParameterDescription("file_path", true, ParameterDescription.Type.STR);
    private static final ParameterDescription PARAM_FILE_PATTERN = 
    	new ParameterDescription("file_pattern", false, ParameterDescription.Type.STR);
    private static final ParameterDescription PARAM_RECURSIVE = 
    	new ParameterDescription("recursive", false, ParameterDescription.Type.STR);
    private static final ParameterDescription PARAM_UPLOAD_FILE = 
    	new ParameterDescription("upload_log", false, ParameterDescription.Type.STR);

    private static final String DEFAULT_FILE_PATTERN = ".*";
    private static final Boolean DEFAULT_RECURSIVE = false;
    private static final String DEFAULT_UPLOAD_FILE = "upload.log";

	//initializer for pluggable params
    { 
    	descriptions.add(PARAM_SCP_USERNAME);
    	descriptions.add(PARAM_SCP_PASSWORD);
    	descriptions.add(PARAM_SCP_HOST);
    	descriptions.add(PARAM_SCP_REMOTE_PATH);
    	descriptions.add(PARAM_FILE_PATH);
    	descriptions.add(PARAM_FILE_PATTERN);
    	descriptions.add(PARAM_RECURSIVE);
    	descriptions.add(PARAM_UPLOAD_FILE);
    }
    
    
    public String getTaskName() {
        return "scp upload task " + getScheduleString();
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        _init(context);

        // scp upload parameters
        String userName = getParameter(PARAM_SCP_USERNAME.getName());
        String password = getParameter(PARAM_SCP_PASSWORD.getName());
        String host = getParameter(PARAM_SCP_HOST.getName());
        String remotePath = getParameter(PARAM_SCP_REMOTE_PATH.getName(), (String) null);

        // files to upload
        File path = new File(getParameter(PARAM_FILE_PATH.getName()));
        String fileRegex = getParameter(PARAM_FILE_PATTERN.getName(), DEFAULT_FILE_PATTERN);
        Boolean recursive = getParameter(PARAM_RECURSIVE.getName(), DEFAULT_RECURSIVE);
        String uploadMarkerFilename = getParameter(PARAM_UPLOAD_FILE.getName(), DEFAULT_UPLOAD_FILE);

        LOG.debug("Scanning " + path.getPath() + (recursive ? " recursively" : "")
                  + " for files matching " + fileRegex);

        // iterate through sub-directories of the configured
        // FILE_PATH and look for files to upload
        for (File file : path.listFiles()) {
            if (file.isDirectory()) {

                // collect  applicable files for upload
                File uploadMarker = new File(file, uploadMarkerFilename);
                if (!uploadMarker.exists()) {
                    List<File> files = collectFiles(file, fileRegex, recursive);
                    LOG.debug("Found " + files.size() + " for upload.");

                    // do scp upload
                    if (!files.isEmpty()) {
                        upload(files, remotePath, host, userName, password);
                        mark(files, uploadMarker);
                    }
                }
            }
        }

        
    }

    /**
     * Collects files under the given path that match the provided regex. If recurse
     * is set to true, this method will scan into every directory under the starting
     * path to collect files. If recurse is false, only the root path is scanned. 
     *
     * @param path path to scan
     * @param fileRegex regular expression to identify matching files for upload
     * @param recurse if true, recurse into all directories under the starting path
     * @return files matching the fileRegex regular expression
     */
    public List<File> collectFiles(File path, final String fileRegex, final boolean recurse) {
        LOG.debug(path.getPath());
        List<File> files = new ArrayList<File>();

        // parse all files on current path, add those that match to the list
        final List<File> directories = new ArrayList<File>();
        File[] tmp = path.listFiles(new FilenameFilter() {
            public boolean accept(File parent, String filename) {
                File file = new File(parent.getPath() + File.separator + filename);
                if (recurse && file.isDirectory()) directories.add(file);
                return file.getPath().matches(fileRegex);
            }
        });
        files.addAll(Arrays.asList(tmp));

        // if set, recurse through sub-directories
        if (recurse) {
            for (File file : directories) {
                if (file.isDirectory()) {
                    files.addAll(collectFiles(file, fileRegex, recurse));
                }
            }
        }
        
        return files;
    }

    // todo: make this class an abstract super class with an abstract upload() method.
    //       ScpUploadTask, FtpUploadTask etc would only need to implement the upload method, this paves
    //       the way for additional scheduled upload task without needing to re-write a bunch of nasty
    //       file handling / transversal code.

    /**
     * Upload the given list of files to the remote server using SCP.
     *
     * @param files files to upload
     * @param remotePath remote path to upload to. If null, files will be uploaded to the users home directory.
     * @param host remote host
     * @param userName user name of remote shell account
     * @param password password of remote shell account
     * @throws JobExecutionException thrown if files cannot be SCP'd
     */
    public void upload(List<File> files, String remotePath, String host, String userName, String password )
            throws JobExecutionException {

        remotePath = (remotePath == null || remotePath.trim().equals("") ? "" : remotePath + File.separator);
        LOG.debug("Uploading " + files.size() + " files to " + userName  + "@" + host + ":" + remotePath);

        List<ScpFile> scpFiles = new ArrayList<ScpFile>();
        for (File file : files) {            
            scpFiles.add(new ScpFile(file, remotePath + file.getName()));
        }

        ScpFacade ssh = new ScpFacade(host, userName, password);
        try {
            ssh.sendFiles(scpFiles);
        } catch (ScpException e) {
            throw new JobExecutionException("Exception occurred uploading files via scp.", e, false);
        }
    }

    /**
     * Writes a list of uploaded files to the given marker file, essentially marking
     * the root directory as "uploaded" so it will be ignored on subsequent passes.
     *
     * @param files files uploaded
     * @param uploadMarker file to use as an upload marker
     * @throws JobExecutionException throw if file could not be written.
     */
    public void mark(List<File> files, File uploadMarker) throws JobExecutionException {
        LOG.debug("Marking folder " + uploadMarker.getParentFile().getPath()
                  + " as uploaded. Writing " + uploadMarker.getName());

        FileWriter writer = null;
        try {
            writer = new FileWriter(uploadMarker, true);
            writer.write("Uploaded " + new Date() + "\n");
            for (File file : files)
                writer.write(file.getPath() + "\n");

        } catch (IOException e) {
            throw new JobExecutionException("Could not create upload marker file " + uploadMarker.getPath(), e, false);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) { /* noop */ }
            }
        }
    }

    /**
     * Returns the named parameter value as a String, throws a JobExecutionException
     * if the value is null or blank.
     *
     * @param key parameter name
     * @return string value of parameter
     * @throws JobExecutionException thrown if string value is null or blank
     */
    public String getParameter(String key) throws JobExecutionException {
        String value = (String) parameters.get(key);
        if (value == null || value.trim().equals(""))
            throw new JobExecutionException("parameter '" + key + "' cannot be blank!");
        return value;
    }
}
