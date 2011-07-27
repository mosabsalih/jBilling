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
package com.sapienter.jbilling.server.notification.barcode;

import org.krysalis.barcode4j.ChecksumMode;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.codabar.CodabarBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code128.EAN128Bean;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.impl.datamatrix.DataMatrixBean;
import org.krysalis.barcode4j.impl.fourstate.RoyalMailCBCBean;
import org.krysalis.barcode4j.impl.fourstate.USPSIntelligentMailBean;
import org.krysalis.barcode4j.impl.int2of5.Interleaved2Of5Bean;
import org.krysalis.barcode4j.impl.pdf417.PDF417Bean;
import org.krysalis.barcode4j.impl.postnet.POSTNETBean;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.impl.upcean.EAN8Bean;
import org.krysalis.barcode4j.impl.upcean.UPCABean;
import org.krysalis.barcode4j.impl.upcean.UPCEBean;

public class BarcodeFactory {

    public static AbstractBarcodeBean code128(TextPlacement placement) {

        Code128Bean result = new Code128Bean();
        result.setMsgPosition(getPlacement(placement));
        result.setFontSize(5);
        return result;
    }
    
    public static AbstractBarcodeBean codabar(Checksum checksum, TextPlacement placement) {

        CodabarBean result = new CodabarBean();
        result.setMsgPosition(getPlacement(placement));
        result.setChecksumMode(getChecksum(checksum));
        return result;
    }
    
    public static AbstractBarcodeBean code39(Checksum checksum, boolean displayChecksum, 
            TextPlacement placement) {
        
        Code39Bean result = new Code39Bean();
        result.setChecksumMode(getChecksum(checksum));
        result.setDisplayChecksum(displayChecksum);
        result.setMsgPosition(getPlacement(placement));
        return result;
    }
    
    public static AbstractBarcodeBean ean128(Checksum checksum, TextPlacement placement) {

        EAN128Bean result = new EAN128Bean();
        result.setChecksumMode(getChecksum(checksum));
        result.setMsgPosition(getPlacement(placement));
        result.setFontSize(5);
        return result;
    }
    
    public static AbstractBarcodeBean datamatrix(TextPlacement placement) {

        DataMatrixBean result = new DataMatrixBean();
        result.setMsgPosition(getPlacement(placement));
        return result;
    }
    
    public static AbstractBarcodeBean interleaved2of5(Checksum checksum, 
            boolean displayChecksum, TextPlacement placement) {

        Interleaved2Of5Bean result = new Interleaved2Of5Bean();
        result.setChecksumMode(getChecksum(checksum));
        result.setMsgPosition(getPlacement(placement));
        return result;
    }
    
    public static AbstractBarcodeBean pdf417(TextPlacement placement) {
        
        PDF417Bean result = new PDF417Bean();
        result.setMsgPosition(getPlacement(placement));
        return result;
    }
    
    public static AbstractBarcodeBean postnet(Checksum checksum, 
            boolean displayChecksum, TextPlacement placement) {
        
        POSTNETBean result = new POSTNETBean();
        result.setChecksumMode(getChecksum(checksum));
        result.setDisplayChecksum(displayChecksum);
        result.setMsgPosition(getPlacement(placement));
        return result;
    }
    
    public static AbstractBarcodeBean ean13(Checksum checksum, TextPlacement placement) {
        EAN13Bean result = new EAN13Bean();
        result.setChecksumMode(getChecksum(checksum));
        result.setMsgPosition(getPlacement(placement));
        return result;
    }
    
    public static AbstractBarcodeBean ean8(Checksum checksum, TextPlacement placement) {
        EAN8Bean result = new EAN8Bean();
        result.setChecksumMode(getChecksum(checksum));
        result.setMsgPosition(getPlacement(placement));
        return result;
    }
    
    public static AbstractBarcodeBean upcA(Checksum checksum, TextPlacement placement) {
        UPCABean result = new UPCABean();
        result.setChecksumMode(getChecksum(checksum));
        result.setMsgPosition(getPlacement(placement));
        return result;
    }
    
    public static AbstractBarcodeBean upcE(Checksum checksum, TextPlacement placement) {
        UPCEBean result = new UPCEBean();
        result.setChecksumMode(getChecksum(checksum));
        result.setMsgPosition(getPlacement(placement));
        return result;
    }
    
    public static AbstractBarcodeBean royalMailCBC(Checksum checksum, TextPlacement placement) {
        RoyalMailCBCBean result = new RoyalMailCBCBean();
        result.setChecksumMode(getChecksum(checksum));
        result.setMsgPosition(getPlacement(placement));
        return result;
    }
    
    public static AbstractBarcodeBean USPSIntelligentMail(Checksum checksum, TextPlacement placement) {
        USPSIntelligentMailBean result = new USPSIntelligentMailBean();
        result.setChecksumMode(getChecksum(checksum));
        result.setMsgPosition(getPlacement(placement));
        return result;
    }
    
    private static HumanReadablePlacement getPlacement(TextPlacement placement) {
        if (placement == null) {
            return HumanReadablePlacement.HRP_NONE;
        }
        switch(placement) {
            case TOP    : return HumanReadablePlacement.HRP_TOP;
            case BOTTOM : return HumanReadablePlacement.HRP_BOTTOM;
            default     : return HumanReadablePlacement.HRP_NONE;
        }
    }
    
    private static ChecksumMode getChecksum(Checksum checksum) {
        if (checksum == null) {
            return ChecksumMode.CP_AUTO;
        }
        switch(checksum) {
            case ADD    : return ChecksumMode.CP_ADD;
            case IGNORE : return ChecksumMode.CP_IGNORE;
            case CHECK  : return ChecksumMode.CP_CHECK;
            default     : return ChecksumMode.CP_AUTO;
        }
    }
}
