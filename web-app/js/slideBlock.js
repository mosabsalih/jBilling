
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

function toggleSlide(element) {
    var parent = $(element).is('.box-cards') ? element : $(element).parents('.box-cards');

    if ($(parent).is('.box-cards-open')) {
        closeSlide(parent);
    } else {
        openSlide(parent);
    }
}

function openSlide(parent) {
    if ($(parent).not('.box-cards-open')) {
        $(parent).addClass('box-cards-open');
        $(parent).find('.box-card-hold').slideDown(500, function() {
            eval($(parent).attr('onOpen'));
            eval($(parent).attr('onSlide'));
        });
    }
}

function closeSlide(parent) {
    if ($(parent).is('.box-cards-open')) {
        $(parent).removeClass('box-cards-open');
        $(parent).find('.box-card-hold').slideUp(500, function() {
            eval($(parent).attr('onClose'));
            eval($(parent).attr('onSlide'));
        });
    }
}

$(document).ready(function(){
    // hide closed box-cards
    $('.box-cards').each(function(){
        if (!$(this).is('.box-cards-open'))
            $(this).find('.box-card-hold').css('display','none');
    });

    // toggle box-cards on click
    $('a.btn-open', '.box-cards').click(function() {
        toggleSlide(this);
        return false;
    });
});
