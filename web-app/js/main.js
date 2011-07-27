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

// IE 6 hover plugin
eval(function(p,a,c,k,e,r){e=function(c){return(c<a?'':e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--)r[e(c)]=k[c]||e(c);k=[function(e){return r[e]}];e=function(){return'\\w+'};c=1};while(c--)if(k[c])p=p.replace(new RegExp('\\b'+e(c)+'\\b','g'),k[c]);return p}('9 u=k(){9 g=/^([^#.>`]*)(#|\\.|\\>|\\`)(.+)$/;k u(a,b){9 c=a.J(/\\s*\\,\\s*/);9 d=[];n(9 i=0;i<c.l;i++){d=d.v(o(c[i],b))};6 d};k o(a,b,c){a=a.z(" ","`");9 d=a.r(g);9 e,5,m,7,i,h;9 f=[];4(d==8){d=[a,a]};4(d[1]==""){d[1]="*"};4(c==8){c="`"};4(b==8){b=E};K(d[2]){w"#":7=d[3].r(g);4(7==8){7=[8,d[3]]};e=E.L(7[1]);4(e==8||(d[1]!="*"&&!x(e,d[1]))){6 f};4(7.l==2){f.A(e);6 f};6 o(7[3],e,7[2]);w".":4(c!=">"){5=p(b,d[1])}y{5=b.B};n(i=0,h=5.l;i<h;i++){e=5[i];4(e.C!=1){q};7=d[3].r(g);4(7!=8){4(e.j==8||e.j.r("(\\\\s|^)"+7[1]+"(\\\\s|$)")==8){q};m=o(7[3],e,7[2]);f=f.v(m)}y 4(e.j!=8&&e.j.r("(\\\\s|^)"+d[3]+"(\\\\s|$)")!=8){f.A(e)}};6 f;w">":4(c!=">"){5=p(b,d[1])}y{5=b.B};n(i=0,h=5.l;i<h;i++){e=5[i];4(e.C!=1){q};4(!x(e,d[1])){q};m=o(d[3],e,">");f=f.v(m)};6 f;w"`":5=p(b,d[1]);n(i=0,h=5.l;i<h;i++){e=5[i];m=o(d[3],e,"`");f=f.v(m)};6 f;M:4(c!=">"){5=p(b,d[1])}y{5=b.B};n(i=0,h=5.l;i<h;i++){e=5[i];4(e.C!=1){q};4(!x(e,d[1])){q};f.A(e)};6 f}};k p(a,b){4(b=="*"&&a.F!=8){6 a.F};6 a.p(b)};k x(a,b){4(b=="*"){6 N};6 a.O.G().z("P:","")==b.G()};6 u}();k Q(a,b){9 c=u(a);n(9 i=0;i<c.l;i++){c[i].R=k(){4(t.j.H(b)==-1){t.j+=" "+b}};c[i].S=k(){4(t.j.H(b)!=-1){t.j=t.j.z(b,"")}}}}4(D.I&&!D.T){D.I("U",V)}',58,58,'||||if|listNodes|return|subselector|null|var||||||||limit||className|function|length|listSubNodes|for|doParse|getElementsByTagName|continue|match||this|parseSelector|concat|case|matchNodeNames|else|replace|push|childNodes|nodeType|window|document|all|toLowerCase|indexOf|attachEvent|split|switch|getElementById|default|true|nodeName|html|hoverForIE6|onmouseover|onmouseout|opera|onload|ieHover'.split('|'),0,{}))
/*parametrs [selector, hover_class]*/
function ieHover() {
	hoverForIE6('.column .box, .table-box li', 'hover');
}


function initPopups()
{
	initPopup({
		openEvent:'click'
	});
	initPopup({
		popupHolderClass:'popup-hover'
	});
    initDropdown();
}
if (window.addEventListener)
	window.addEventListener("load", initPopups, false);
else if (window.attachEvent)
	window.attachEvent("onload", initPopups);

function initPopup(_popup) {
	if (!_popup.popupHolderTag) _popup.popupHolderTag = 'div';
	if (!_popup.popupTag) _popup.popupTag = 'div';
	if (!_popup.popupHolderClass) _popup.popupHolderClass = 'input-bg';
	if (!_popup.popupClass) _popup.popupClass = 'popup';
	if (!_popup.linkOpenClass) _popup.linkOpenClass = 'open';
	if (!_popup.linkCloseClass) _popup.linkCloseClass = 'close';
	if (!_popup.openClass) _popup.openClass = 'active';
	if (!_popup.openEvent) _popup.openEvent = 'hover';
	
	var timer = [];	
	var _popupHolderTag = document.getElementsByTagName(_popup.popupHolderTag);
	if (_popupHolderTag) {
		for (var i=0; i<_popupHolderTag.length; i++) {
			if (_popupHolderTag[i].className.indexOf(_popup.popupHolderClass) != -1) {
				var _popupLink = _popupHolderTag[i].getElementsByTagName('a');
				for (var j=0; j<_popupLink.length; j++) {
					_popupLink[j].parent = _popupHolderTag[i];
					if (_popupLink[j].className.indexOf(_popup.linkOpenClass) != -1) {
						if (_popup.openEvent == 'click') {
							_popupLink[j].onclick = function(){
								if (this.parent.className.indexOf(_popup.openClass) != -1) {
									this.parent.className = this.parent.className.replace(_popup.openClass,'');
								} else {
									this.parent.className += ' '+_popup.openClass;
								}
								return false;
							}
						} else {
							var _popupTag = _popupHolderTag[i].getElementsByTagName(_popup.popupTag);
							for (var k=0; k<_popupTag.length; k++) {
								if (_popupTag[k].className.indexOf(_popup.popupClass) != -1) {
									_popupTag[k].parent = _popupHolderTag[i];
									_popupTag[k].onmouseover = function(){
										if (timer[j]) clearTimeout(timer[j]);
										if (this.parent.className.indexOf(_popup.openClass) == -1) {
											this.parent.className += ' '+_popup.openClass;
										}
									}
									_popupTag[k].onmouseout = function(){
										var _this = this;
										timer[j] = setTimeout(function(){
											_this.parent.className = _this.parent.className.replace(_popup.openClass,'');
										},2);
									}	
								}
							}
							_popupLink[j].onmouseover = function(){
								if (timer[j]) clearTimeout(timer[j]);
								if (this.parent.className.indexOf(_popup.openClass) == -1) {
									this.parent.className += ' '+_popup.openClass;
								}
							}
							_popupLink[j].onmouseout = function(){
								var _this = this;
								timer[j] = setTimeout(function(){
									_this.parent.className = _this.parent.className.replace(_popup.openClass,'');
								},2);
							}
						}
					} else if (_popupLink[j].className.indexOf(_popup.linkCloseClass) != -1) {
						_popupLink[j].onclick = function(){
							if (this.parent.className.indexOf(_popup.openClass) != -1) {
								this.parent.className = this.parent.className.replace(_popup.openClass,'');
							} else {
								this.parent.className += ' '+_popup.openClass;
							}
							return false;
						}
					}
				}
			}		
		}
	}
}

function initDropdown() {
    $('.dropdown').each(function() {
        var open = $(this).find('a.open');
        var drop = $(this).find('.drop');

        // position drop menu in line with the "open" link
        drop.css('top', open.position().top + open.outerHeight())
            .css('left', open.position().left);
    });
}

function initScript() {
	addClass({
		tagName:'a',
		tagClass:'open',
		classAdd:'active',
		addToParent:true
	})
}

function addClass (_options) {
	var _tagName = _options.tagName;
	var _tagClass = _options.tagClass;
	var _classAdd = _options.classAdd;
	var _addToParent = false || _options.addToParent;
	var _el = document.getElementsByTagName(_tagName);
	if (_el) {
		for (var i=0; i < _el.length; i++) {
			if (_el[i].className.indexOf(_tagClass) != -1) {
				_el[i].onclick = function() {
					if (_addToParent) {
						if (this.parentNode.className.indexOf(_classAdd) == -1) {
							this.parentNode.className += ' '+_classAdd;
						} else {
							this.parentNode.className = this.parentNode.className.replace(_classAdd,'');
						}
					} else {
						if (this.className.indexOf(_classAdd) == -1) {
							this.className += ' '+_classAdd;
						} else {
							this.className = this.className.replace(_classAdd,'');
						}
					}
					return false;
				}
			}
		}
	}
}
if (window.addEventListener)
	window.addEventListener("load", initScript, false);
else if (window.attachEvent)
	window.attachEvent("onload", initScript);