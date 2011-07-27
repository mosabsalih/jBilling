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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.krysalis.barcode4j.BarcodeDimension;
import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.codabar.CodabarBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.impl.code128.EAN128Bean;
import org.krysalis.barcode4j.impl.datamatrix.DataMatrixBean;
import org.krysalis.barcode4j.impl.int2of5.Interleaved2Of5Bean;
import org.krysalis.barcode4j.impl.pdf417.PDF417Bean;
import org.krysalis.barcode4j.impl.postnet.POSTNETBean;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.impl.upcean.EAN8Bean;
import org.krysalis.barcode4j.impl.upcean.UPCABean;
import org.krysalis.barcode4j.impl.upcean.UPCEBean;
import org.krysalis.barcode4j.impl.fourstate.RoyalMailCBCBean;
import org.krysalis.barcode4j.impl.fourstate.USPSIntelligentMailBean;
import org.krysalis.barcode4j.output.java2d.Java2DCanvasProvider;

import net.sf.jasperreports.engine.JRAbstractSvgRenderer;
import net.sf.jasperreports.engine.JRException;

public class BarcodeRenderer extends JRAbstractSvgRenderer {

    private static final long serialVersionUID = 1L;
    
    private AbstractBarcodeBean barcode = null;
    private String code = null;
    
    public BarcodeRenderer(BarcodeType barcodeType, String code) {
        
        switch (barcodeType) {
        case CODE128               : this.barcode = new Code128Bean(); break;
        case CODABAR               : this.barcode = new CodabarBean(); break;
        case CODE39                : this.barcode = new Code39Bean(); break;
        case EAN128                : this.barcode = new EAN128Bean(); break;
        case DATAMATRIX            : this.barcode = new DataMatrixBean(); break;
        case INT2OF5               : this.barcode = new Interleaved2Of5Bean(); break;
        case PDF417                : this.barcode = new PDF417Bean(); break;
        case POSTNET               : this.barcode = new POSTNETBean(); break;
        case EAN13                 : this.barcode = new EAN13Bean(); break;
        case EAN8                  : this.barcode = new EAN8Bean(); break;
        case UPCA                  : this.barcode = new UPCABean(); break;
        case UPCE                  : this.barcode = new UPCEBean(); break;
        case ROYAL_MAIL_CBC        : this.barcode = new RoyalMailCBCBean(); break;
        case USPS_INTELLIGENT_MAIL : this.barcode = new USPSIntelligentMailBean(); break;
        }
        this.code = code;
    }
    
    public BarcodeRenderer(AbstractBarcodeBean bean, String code) {
        this.barcode = bean;
        this.code = code;
    }

    @Override
    public void render(Graphics2D grx, Rectangle2D rectangle) throws JRException {
        
        if (barcode == null || code == null || code.equals("")) {
            throw new JRException("Barcode not defined");
        }
        
        try {
            
            BarcodeDimension dimension = barcode.calcDimensions(code);
            Rectangle2D barcodeDim = dimension.getBoundingRect();
            
            Graphics2D graphics = (Graphics2D) grx.create();
            graphics.translate(rectangle.getX(), rectangle.getY());
            graphics.scale(rectangle.getWidth() / barcodeDim.getWidth(), 
                    rectangle.getHeight() / barcodeDim.getHeight());
    
            Java2DCanvasProvider canvasProvider = new Java2DCanvasProvider(graphics, 0);
            barcode.generateBarcode(canvasProvider, code);
            
        } catch (Throwable e) {
            throw new JRException("Error while generating barcode", e);
        }
    }

}
