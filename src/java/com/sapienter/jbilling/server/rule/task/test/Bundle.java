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

package com.sapienter.jbilling.server.rule.task.test;

import java.util.LinkedList;
import java.util.List;

public class Bundle {
    private Product originalProduct;
    private List<Product> replacementProducts;

    public Bundle() {
        originalProduct = null;
        replacementProducts = new LinkedList<Product>();
    }

    public void setOriginalProduct(Product originalProduct) {
        this.originalProduct = originalProduct;
    }

    public Product getOriginalProduct() {
        return originalProduct;
    }

    public void setReplacementProducts(
            List<Product> replacementProducts) {
        this.replacementProducts = replacementProducts;
    }

    public List<Product> getReplacementProducts() {
        return replacementProducts;
    }

    public void addReplacementProduct(Product product) {
        replacementProducts.add(product);
    }
}
