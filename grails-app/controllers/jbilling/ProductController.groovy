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

package jbilling

import com.sapienter.jbilling.client.util.DownloadHelper
import com.sapienter.jbilling.common.SessionInternalError
import com.sapienter.jbilling.server.item.CurrencyBL
import com.sapienter.jbilling.server.item.ItemDTOEx
import com.sapienter.jbilling.server.item.ItemPriceDTOEx
import com.sapienter.jbilling.server.item.ItemTypeWS
import com.sapienter.jbilling.server.item.db.ItemDTO
import com.sapienter.jbilling.server.item.db.ItemTypeDTO
import com.sapienter.jbilling.server.user.db.CompanyDTO
import com.sapienter.jbilling.server.util.csv.CsvExporter
import com.sapienter.jbilling.server.util.csv.Exporter
import grails.plugins.springsecurity.Secured
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import com.sapienter.jbilling.client.util.SortableCriteria

@Secured(["MENU_97"])
class ProductController {

    static pagination = [ max: 10, offset: 0, sort: 'id', order: 'desc' ]

    def webServicesSession
    def viewUtils
    def filterService
    def recentItemService
    def breadcrumbService

    def index = {
        redirect action: list, params: params
    }

    /**
     * Get a list of categories and render the "_categories.gsp" template. If a category ID is given as the
     * "id" parameter, the corresponding list of products will also be rendered.
     */
    def list = {
        def filters = filterService.getFilters(FilterType.PRODUCT, params)
        def categories = getProductCategories(true)

        def categoryId = params.int('id')
        def category = categoryId ? ItemTypeDTO.get(categoryId) : null
        def products = category ? getProducts(category.id, filters) : null

        breadcrumbService.addBreadcrumb(controllerName, actionName, null, params.int('id'), category?.description)

        if (params.applyFilter || params.partial) {
            render template: 'products', model: [ products: products, selectedCategoryId: category?.id ]
        } else {
            [ categories: categories, products: products, selectedCategoryId: category?.id, filters: filters, filterRender: 'second', filterAction: 'allProducts' ]
        }
    }

    def categories = {
        def categories = getProductCategories(true)
        render template: 'categories', model: [ categories: categories ]
    }

    def getProductCategories(paged = false) {
        if (paged) {
            params.max = params?.max?.toInteger() ?: pagination.max
            params.offset = params?.offset?.toInteger() ?: pagination.offset
        }

        return ItemTypeDTO.createCriteria().list(
            max: paged ? params.max : null,
            offset: paged ? params.offset : null
        ) {
            and {
                eq('entity', new CompanyDTO(session['company_id']))
            }
            order('id', 'desc')
        }
    }

    /**
     * Get a list of products for the given item type id and render the "_products.gsp" template.
     */
    def products = {
        if (params.id) {
            def filters = filterService.getFilters(FilterType.PRODUCT, params)
            def category = ItemTypeDTO.get(params.int('id'))
            def products = getProducts(category.id, filters)

            breadcrumbService.addBreadcrumb(controllerName, 'list', null, category.id, category?.description)

            render template: 'products', model: [ products: products, selectedCategoryId: category.id ]
        }
    }

    /**
     * Applies the set filters to the product list, and exports it as a CSV for download.
     */
    @Secured(["PRODUCT_44"])
    def csv = {
        def filters = filterService.getFilters(FilterType.PRODUCT, params)

        params.max = CsvExporter.MAX_RESULTS
        def products = getProducts(params.int('id'), filters)

        if (products.totalCount > CsvExporter.MAX_RESULTS) {
            flash.error = message(code: 'error.export.exceeds.maximum')
            flash.args = [ CsvExporter.MAX_RESULTS ]
            redirect action: 'list', id: params.id

        } else {
            DownloadHelper.setResponseHeader(response, "products.csv")
            Exporter<ItemDTO> exporter = CsvExporter.createExporter(ItemDTO.class);
            render text: exporter.export(products), contentType: "text/csv"
        }
    }

    def getProducts(Integer id, filters) {
        params.max = params?.max?.toInteger() ?: pagination.max
        params.offset = params?.offset?.toInteger() ?: pagination.offset
        params.sort = params?.sort ?: pagination.sort
        params.order = params?.order ?: pagination.order

        return ItemDTO.createCriteria().list(
                max:    params.max,
                offset: params.offset
        ) {
            and {
                filters.each { filter ->
                    if (filter.value != null) {
                        if (filter.field == 'description') {
                            def description = filter.stringValue?.toLowerCase()
                            sqlRestriction(
                                    """ exists (
                                            select a.foreign_id
                                            from international_description a
                                            where a.foreign_id = {alias}.id
                                            and a.language_id = ${session['language_id']}
                                            and lower(a.content) like '%${description}%'
                                        )
                                    """
                            )
                        } else {
                            addToCriteria(filter.getRestrictions());
                        }
                    }
                }

                if (id != null) {
                    itemTypes {
                        eq('id', id)
                    }
                }

                eq('deleted', 0)
                eq('entity', new CompanyDTO(session['company_id']))
            }

            // apply sorting
            SortableCriteria.sort(params, delegate)
        }
    }

    /**
     * Get a list of ALL products regardless of the item type selected, and render the "_products.gsp" template.
     */
    def allProducts = {
        def filters = filterService.getFilters(FilterType.PRODUCT, params)

        def products =  getProducts(null, filters)

        render template: 'products', model: [ products: products ]
    }

    /**
     * Show details of the selected product. By default, this action renders the entire list view
     * with the product category list, product list, and product details rendered. When rendering
     * for an AJAX request the template defined by the "template" parameter will be rendered.
     */
    @Secured(["PRODUCT_43"])
    def show = {
        ItemDTO product = ItemDTO.get(params.int('id'))
        recentItemService.addRecentItem(product?.id, RecentItemType.PRODUCT)
        breadcrumbService.addBreadcrumb(controllerName, actionName, null, params.int('id'), product?.internalNumber)

        if (params.template) {
            // render requested template, usually "_show.gsp"
            render template: params.template, model: [ selectedProduct: product, selectedCategoryId: params.category ]

        } else {
            // render default "list" view - needed so a breadcrumb can link to a product by id
            def filters = filterService.getFilters(FilterType.PRODUCT, params)
            def categories = getProductCategories();

            def productCategoryId = params.category ?: product?.itemTypes?.asList()?.get(0)?.id
            def products = getProducts(productCategoryId, filters);

            render view: 'list', model: [ categories: categories, products: products, selectedProduct: product, selectedCategoryId: productCategoryId, filters: filters ]
        }
    }

    /**
     * Delete the given category id
     */
    @Secured(["PRODUCT_CATEGORY_52"])
    def deleteCategory = {
        if (params.id) {
            try {
                webServicesSession.deleteItemCategory(params.int('id'))

                log.debug("Deleted item category ${params.id}.");

                flash.message = 'product.category.deleted'
                flash.args = [ params.id ]

            } catch (SessionInternalError e) {
                flash.error = 'product.category.delete.error'
                flash.args = [ params.id ]
            }
        }

        render template: 'categories', model: [ categories: getProductCategories() ]
    }

    /**
     * Delete the given product id
     */
    @Secured(["PRODUCT_42"])
    def deleteProduct = {
        if (params.id) {
            webServicesSession.deleteItem(params.int('id'))

            log.debug("Deleted item ${params.id}.");

            flash.message = 'product.deleted'
            flash.args = [ params.id ]
        }

        // call the rendering action directly instead of using 'chain' or 'redirect' which results
        // in a second request that clears the flash messages.
        if (params.category) {
            // return the products list, pass the category so the correct set of products is returned.
            params.id = params.category
            products()
        } else {
            // no category means we deleted from the 'allProducts' view
            allProducts()
        }
    }

    /**
     * Get the item category to be edited and show the "editCategory.gsp" view. If no ID is given
     * this view will allow creation of a new category.
     */
    @Secured(["hasAnyRole('PRODUCT_CATEGORY_50', 'PRODUCT_CATEGORY_51')"])
    def editCategory = {
        def category = params.id ? ItemTypeDTO.get(params.id) : null

        if (params.id && !category) {
            flash.error = 'product.category.not.found'
            flash.args = [ params.id ]

            redirect controller: 'product', action: 'list'
            return
        }

        breadcrumbService.addBreadcrumb(controllerName, actionName, params.id ? 'update' : 'create', params.int('id'), category?.description)

        [ category : category ]
    }

    /**
     * Validate and save a category.
     */
    @Secured(["hasAnyRole('PRODUCT_CATEGORY_50', 'PRODUCT_CATEGORY_51')"])
    def saveCategory = {
        def category = new ItemTypeWS()

        // grails has issues binding the ID for ItemTypeWS object...
        // bind category ID manually
        bindData(category, params, 'id')
        category.id = !params.id?.equals('') ? params.int('id') : null

        // save or update
        try {
            if (!category.id || category.id == 0) {
                if (SpringSecurityUtils.ifAllGranted("PRODUCT_CATEGORY_50")) {
                    log.debug("creating product category ${category}")

                    category.id = webServicesSession.createItemCategory(category)

                    flash.message = 'product.category.created'
                    flash.args = [ category.id ]

                } else {
                    render view: '/login/denied'
                    return
                }

            } else {
                if (SpringSecurityUtils.ifAllGranted("PRODUCT_CATEGORY_51")) {
                    log.debug("saving changes to product category ${category.id}")

                    webServicesSession.updateItemCategory(category)

                    flash.message = 'product.category.updated'
                    flash.args = [ category.id ]

                } else {
                    render view: '/login/denied'
                    return
                }
            }

        } catch (SessionInternalError e) {
            viewUtils.resolveException(flash, session.locale, e);
            render view: 'editCategory', model: [ category : category ]
            return
        }

        chain action: 'list', params: [ id: category.id ]
    }

    /**
     * Get the item to be edited and show the "editProduct.gsp" view. If no ID is given
     * this screen will allow creation of a new item.
     */
    @Secured(["hasAnyRole('PRODUCT_40', 'PRODUCT_41')"])
    def editProduct = {
        def product

        try {
            product = params.id ? webServicesSession.getItem(params.int('id'), session['user_id'], null) : null
        } catch (SessionInternalError e) {
            log.error("Could not fetch WS object", e)

            flash.error = 'product.not.found'
            flash.args = [ params.id ]

            redirect controller: 'product', action: 'list'
            return
        }

        breadcrumbService.addBreadcrumb(controllerName, actionName, params.id ? 'update' : 'create', params.int('id'), product?.number)

        [ product: product, currencies: currencies, categories: getProductCategories(), categoryId: params.category ]
    }

    /**
     * Validate and save a product.
     */
    @Secured(["hasAnyRole('PRODUCT_40', 'PRODUCT_41')"])
    def saveProduct = {
        def product = new ItemDTOEx()
        bindProduct(product, params)

        log.debug("Product ${product}")

        try{
            // save or update
            if (!product.id || product.id == 0) {
                if (SpringSecurityUtils.ifAllGranted("PRODUCT_40")) {
                    log.debug("creating product ${product}")

                    product.id = webServicesSession.createItem(product)

                    flash.message = 'product.created'
                    flash.args = [ product.id ]

                } else {
                    render view: '/login/denied'
                    return;
                }

            } else {
                if (SpringSecurityUtils.ifAllGranted("PRODUCT_41")) {
                    log.debug("saving changes to product ${product.id}")

                    webServicesSession.updateItem(product)

                    flash.message = 'product.updated'
                    flash.args = [ product.id ]

                } else {
                    render view: '/login/denied'
                    return;
                }
            }

        } catch (SessionInternalError e) {
            viewUtils.resolveException(flash, session.locale, e);
            render view: 'editProduct', model: [ product: product, categories: getProductCategories(), currencies: currencies ]
            return
        }

        chain action: 'show', params: [ id: product.id ]
    }

    def bindProduct(ItemDTOEx product, GrailsParameterMap params) {
        bindData(product, params, 'product')

        // bind parameters with odd types (integer booleans, string integers  etc.)
        product.priceManual = params.product.priceManual ? 1 : 0
        product.hasDecimals = params.product.hasDecimals ? 1 : 0
        product.percentage = !params.product.percentageAsDecimal?.equals('') ? params.product.percentageAsDecimal : null

        // bind prices
        if (!product.percentage) {
            def prices = params.prices.collect { currencyId, price ->
                new ItemPriceDTOEx(null, !price?.equals('') ? price.toBigDecimal() : null, currencyId.toInteger())
            }
            product.prices = prices
        }
    }

    def getCurrencies() {
        def currencies = new CurrencyBL().getCurrencies(session['language_id'].toInteger(), session['company_id'].toInteger())
        return currencies.findAll { it.inUse }
    }

}
