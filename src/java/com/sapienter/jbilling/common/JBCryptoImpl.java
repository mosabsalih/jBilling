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
package com.sapienter.jbilling.common;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;

import com.sapienter.jbilling.server.util.Util;

public final class JBCryptoImpl extends JBCrypto {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String ALGORITHM = "PBEWithMD5AndDES";
    private static SecretKeyFactory ourKeyFactory;
    private static final PBEParameterSpec ourPBEParameters;
    
    //private static final Logger LOG = Logger.getLogger(JBCryptoImpl.class);

    private final SecretKey mySecretKey;

    public JBCryptoImpl(String password) throws InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory keyFactory = getSecretKeyFactory();
        mySecretKey = keyFactory.generateSecret(spec);
    }

    public String decrypt(String cryptedText) {
        Cipher cipher = getCipher();
        byte[] crypted = useHexForBinary ? Util.stringToBinary(cryptedText) :
                Base64.decodeBase64(cryptedText.getBytes());
        byte[] result;
        try {
            cipher.init(Cipher.DECRYPT_MODE, mySecretKey, ourPBEParameters);
            result = cipher.doFinal(crypted);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Can not decrypt:" + cryptedText, e);
        }
        return new String(result, UTF8);
    }

    public String encrypt(String text) {
        Cipher cipher = getCipher();
        byte[] crypted;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, mySecretKey, ourPBEParameters);
            byte[] bytes = text.getBytes(UTF8);
            crypted = cipher.doFinal(bytes);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Can not encrypt :" + text, e);
        }
        String cryptedText = useHexForBinary ? Util.binaryToString(crypted) :
                new String(Base64.encodeBase64(crypted));
        return cryptedText;
    }
    
    private static SecretKeyFactory getSecretKeyFactory() {
        if (ourKeyFactory == null) {
            try {
                ourKeyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Algorithm is not supported: "
                        + ALGORITHM, e);
            }
        }
        return ourKeyFactory;
    }

    private static Cipher getCipher() {
        Cipher ourCipher;
        try {
            ourCipher = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                "Strange. Algorithm was supported a few seconds ago : "
                        + ALGORITHM, e);
        } catch (NoSuchPaddingException e) {
            throw new IllegalStateException(e);
        }
        return ourCipher;
    }
    
    static {
        // DON'T CHANGE THIS
        // IT WOULD BREAK BACKWARD COMPATIBILITY
        ourPBEParameters = new PBEParameterSpec(new byte[] { //
                (byte) 0x3c, (byte) 0x15, // 
                        (byte) 0x27, (byte) 0x7f, //
                        (byte) 0x2d, (byte) 0xda, //
                        (byte) 0xe6, (byte) 0x64, //
                }, 15);
    }
    
    static {
        
    }
}
