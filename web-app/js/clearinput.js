/*
 * jBilling - The Enterprise Open Source Billing System
 * Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde
 *
 * This file is part of jbilling.
 *
 * jbilling is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jbilling is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Initialize input field place-holder values.
 */
function placeholder() {
    $(':input.default').not('[placeholder=*]').each(function() {
        var element = $(this);

        if (element.attr('placeholder') == null && element.attr('value') != null)
            element.attr('placeholder', this.getAttribute('value'));

        if (element.val() == null || element.val().length == 0)
            element.val(element.attr('placeholder'));
    });
}

/*
    A rough approximation of the HTML5 input field place-holder text. Provides
    a visible default for the input field that is cleared when the input field
    gains focus.
 */
$(document).ready(function() {
    $('body').delegate(':input.default', 'focus', function() {
        // clear placeholder text on focus
        var element = $(this);
        if (element.val() == element.attr('placeholder'))
            element.val('');


    }).delegate(':input.default', 'blur', function() {
        // no text entered, show placeholder text
        var element = $(this);
        if (element.val().length == 0)
            element.val(element.attr('placeholder'));
    });

    placeholder();
});