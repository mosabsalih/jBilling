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

package com.sapienter.jbilling.client.util

/**
 * DownloadHelper 
 *
 * @author Brian Cowdery
 * @since 04/03/11
 */
class DownloadHelper {

    /**
     * Sets an appropriate response header for the downloaded file. These headers provide
     * a filename to the downloading client, and ensure that the download file is not cached.
     *
     * @param response response object
     * @param filename filename to set in the response header
     */
    static def setResponseHeader(response, String filename) {
        response.setHeader("Content-disposition", "attachment; filename=${filename}")
        response.setHeader("Expires", "0")
        response.setHeader("Cache-Control", "no-cache")
    }

    /**
     * Sends the given bytes as the content of the response object.
     *
     * @param response response object
     * @param filename filename to set in the response header
     * @param contentType MIME content type of the sent file
     * @param bytes bytes to send
     */
    static def sendFile(response, String filename, String contentType, byte[] bytes) {
        setResponseHeader(response, filename)

        response.setContentType(contentType)
        response.setContentLength(bytes.length)
        response.outputStream << bytes
    }
}
