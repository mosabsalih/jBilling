
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

var clicked;

var first = {
    index: function() {
        return 1;
    },
    visible: function() {
        return $('#viewport .column:first-child').get(0) != null;
    },
    animate: function() {
    }
};

var second = {
    index: function() {
        return 2;
    },
    visible: function() {
        return $('#viewport .column:nth-child(2)').get(0) != null;
    },
    animate: function() {
    }
};

var third = {
    index: function() {
        return 3;
    },
    visible: function() {
        return false;
    },
    animate: function() {
        $('#viewport .column:first-child').animate(
            {
                marginLeft: '-=100%'
            },
            {
                duration: 'slow',
                easing: 'easeInExpo',
                complete: function() {
                    $(this).empty().remove();
                    calculateColumnId();
                }
            }
        );
    }
};

var next = {
    index: function() {
        return clicked + 1;
    },
    visible: function() {
        var index = this.index();
        if (index > 2) {
            return false
        } else {
            return $('#viewport .column:nth-child(' + index + ')').get(0) != null;
        }
    },
    animate: function() {
        $('#viewport .column:first-child').animate(
            {
                marginLeft: '-=100%'
            },
            {
                duration: 'slow',
                easing: 'easeInExpo',
                complete: function() {
                    $(this).empty().remove();
                    calculateColumnId();
                }
            }
        );
    }
};

/**
 * Registers a click on an AJAX link so that the source column is known to the renderer.
 *
 * @param element source element
 */
function register(element) {
    var column = $(element).parents('.column.panel');
    $('#viewport').children().each(function(index, element) {
        if ($(column).get(0) == $(element).get(0)) {
            clicked = index + 1;
        }
    });
}

/**
 * Renders the AJAX return value in the target column (next or prev). If the target
 * column is not visible the view will be changed to show the target column.
 *
 * The target column is given as a simple object that can calculate the next column
 * position and animate the transition to show the column (if necessary).
 *
 * E.g.,
 *      render(data, next);
 *      render(data, first);
 *      render(data, second); // etc
 *
 * @param data data to render in target column
 * @param target column function.
 */
function render(data, target) {
    if (target.visible()) {
        // render data in visible column
        var column = $('#viewport .column:nth-child(' + target.index() + ')');
        column.find('.column-hold').html(data);


    } else {
        // build a new column node and append to viewport list
        var column = $('#panel-template').clone().attr('id', '');
        column.find('.column-hold').html(data);
        column.show();
        $('#viewport').append(column);

        // check if target needs animation to become visible after inserting new node
        if (!target.visible())
            target.animate();
    }
}

/**
 * Re-orders ID's of the columns to reflect they're position. A column in position 1
 * should always have the id 'column1', position 2 should always have the id 'column2'.
 */
function calculateColumnId() {
    $('#viewport').children().each(function(index, element) {
        $(element).find('.column-hold').attr('id', 'column' + (index + 1));
    });
}

/**
 * Closes the panel containing the passed element.
 *
 * @param element element contained in the panel to close. usually the ajax link or form element.
 */
function closePanel(element) {
    register(element);
    $('#viewport .column:nth-child(' + clicked + ')').remove();
}

/**
 * Returns the first selected element ID residing in the same column as the given element.
 *
 * E.g., getSelectedElementId(this);  #=>  "user-123"
 *
 * @param element in the same column as the selected row
 */
function getSelectedElementId(element) {
    var column = $(element).is('.column-hold') ? element : $(element).parents('.column-hold')[0];
    return $(column).find('.table-box .active').attr('id');
}

/**
 * Returns the object ID of the first selected element residing in the same column as the
 * given element.
 *
 * This method assumes that the list row contains the object's id as part of the element
 * ID. All non-digit characters are stripped and the remaining digits are returned as the
 * object id.
 *
 * E.g.,
 *      <tr id="user-123" class="active"></tr>
 *      <tr id="user-456"></tr>
 *
 *      getSelectedId(this);   #=>   "123"
 *
 * @param element in the same column as the selected row
 */
function getSelectedId(element) {
    var elementId = getSelectedElementId(element);
    return elementId ? elementId.replace(/\D+/, "") : undefined;
}


