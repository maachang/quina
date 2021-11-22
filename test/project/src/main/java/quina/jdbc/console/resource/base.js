/**
 * jdbc console base js.
 */
(function(_g) {
"use strict";

// nowLoading-z-index.
var NOW_LOADING_ZINDEX = 500;

// nowLoadingViewId.
var NOW_LOADING_VIEW_ID = "nowLoadingView";

// nowLoadingBackRGBA.
var NOW_LOADING_RGBA = {r:32,g:32,b:32,a:0.5};

// alert-z-index.
var ALERT_ZINDEX = 1000;

// alertViewId.
var ALERT_VIEW_ID = "alertView";

// alert confirm yes button id.
var ALERT_YES_BUTTON_ID = ALERT_VIEW_ID + "_" + "yes";

// alert confirm no button id.
var ALERT_NO_BUTTON_ID = ALERT_VIEW_ID + "_" + "no";

// shadow dialog.
var BACK_DIALOG_SHADOW = "box-shadow: 10px 10px 10px rgba(0,0,0,0.75);"

// 時間差コール.
var timeLagCall = function(call) {
    setTimeout(function() {
        call();
    }, 100);
}

// Screen display while loading.
var nowLoading = function(rgba) {
    // get nowLoadingViewId.
    var em = document.getElementById(NOW_LOADING_VIEW_ID);
    if(isNull(em)) {
        return;
    }
    var rgba = NOW_LOADING_RGBA;
    var w = document.documentElement.scrollWidth || document.body.scrollWidth;
    var h = document.documentElement.scrollHeight || document.body.scrollHeight;
    em.innerHTML = "<div style='z-index:" + NOW_LOADING_ZINDEX +
        ";position:absolute;width:"+w+"px;height:"+h+"px;" +
        "left:0px;top:0px;background-color:rgba("
            +rgba.r+","+rgba.g+","+rgba.b+","+rgba.a+");' " +
        "onclick='event.preventDefault()' " +
        "ontouchstart='event.preventDefault()' " +
        "ontouchend='event.preventDefault()' " +
        "ontouchmove='event.preventDefault()'>" +
        "</div>";
}

// Clears the screen display while loading. 
var clearNowLoading = function() {
    // get nowLoadingViewId.
    var em = document.getElementById(NOW_LOADING_VIEW_ID);
    if(isNull(em)) {
        return;
    }
    em.innerHTML = "";
}

// Calculate the optimal size of the dialog display frame. 
var dialogPositionCalcSize = function() {
    var w = innerWidth;
    var h = innerHeight;
    if(w > h) {
        var left = (w*0.3)|0;
        var top = (h*0.2)|0;
        var width = (w*0.4)|0;
        var height = (h*0.6)|0;
        var radius = 10;
    } else {
        var left = (w*0.15)|0;
        var top = (h*0.2)|0;
        var width = (w*0.7)|0;
        var height = (h*0.6)|0;
        var radius = 10;
    }
    return {w:w,h:h,left:left,top:top,width:width,
        height:height,radius:radius};
}

// change html.
var changeHtml = (function() {
    var _chkCD = "&<>\'\" \r\n" ;
    return function( string ) {
      var len = string.length ;
      var chkCd = _chkCD ;
      var ret = "";
      var c ;
      for( var i = 0 ; i < len ; i ++ ) {
        switch( chkCd.indexOf( c = string.charAt( i ) ) ) {
          case -1: ret += c; break;
          case 0 : ret += "&amp;" ; break ;
          case 1 : ret += "&lt;" ; break ;
          case 2 : ret += "&gt;" ; break ;
          case 3 : ret += "&#039;" ; break ;
          case 4 : ret += "&#034;" ; break ;
          case 5 : ret += "&nbsp;" ; break ;
          case 6 : ret += "" ; break ;
          case 7 : ret += "<br>" ; break ;
        }
      }
      return ret
    }
})();

// add js event.
var addEvent = function(node, name, func) {
    if(isNull(node)) {
        node = window;
    }
    if(node.addEventListener){
        node.addEventListener(name, func, false);
    } else if(node.attachEvent){
        node.attachEvent("on"+name, func);
    }
}

// clear alert window.
var clearAlertWindow = function(noneNowLoading) {
    // get alertViewId.
    var em = document.getElementById(ALERT_VIEW_ID);
    if(isNull(em)) {
        return;
    }
    em.innerHTML = "";
    if(noneNowLoading != true) {
        clearNowLoading();
    }
}

// alert window id.
var ALERT_WINDOW_ID = "alertWindowId";

// create start alert html.
var createStartAlertHtml = function(message) {
    var p = dialogPositionCalcSize();
    return "<div id='" + ALERT_WINDOW_ID + "' style='z-index:" + ALERT_ZINDEX + ";position:absolute;left:" +
    p.left + "px;top:" + p.top + "px;"+"width:" + p.width + "px;height:" + p.height + "px;border-radius:" +
    p.radius + "px;word-break:break-all;background:#ffffff;color:#000000;border: solid 2px #efefef;" +
    BACK_DIALOG_SHADOW + "overflow:auto;'" +
    ">" +
    "<div style='margin:10px;font-size:small;color:#666;'>" +
    changeHtml(message) ;
}

// create end alert html.
var createEndAlertHtml = function() {
    return "</div></div>";
}

// new window to alert.
var alertWindow = function(message) {
    if(isNull(message) || (message = ("" + message).trim()).length == 0) {
        return;
    }
    // get alertViewId.
    var em = document.getElementById(ALERT_VIEW_ID);
    if(isNull(em)) {
        return;
    }
    nowLoading();
    em.innerHTML = createStartAlertHtml(message) + createEndAlertHtml();
    // click callback.
    timeLagCall(function() {
        var em = document.getElementById("alertWindowId");
        if(!isNull(em)) {
            addEvent(em, "click", clearAlertWindow);
        }
    });
}

// new window to confirm.
var confirmWindow = function(message, call) {
    // get alertViewId.
    var em = document.getElementById(ALERT_VIEW_ID);
    if(isNull(em)) {
        return;
    } else if(isNull(message) || (message = ("" + message).trim()).length == 0) {
        return;
    }
    // get alertViewId.
    var em = document.getElementById(ALERT_VIEW_ID);
    if(isNull(em)) {
        return;
    }
    nowLoading();
    var p = dialogPositionCalcSize();
    em.innerHTML = createStartAlertHtml(message) +
    "<br><br>" +
    addButton(ALERT_YES_BUTTON_ID, "O&nbsp;&nbsp;K") +
    "&nbsp;&nbsp;" +
    addButton(ALERT_NO_BUTTON_ID, "CANCEL") +
    createEndAlertHtml();
    // yes no button click callback.
    timeLagCall(function() {
        var yesCall = function() {
            if(!call(true)) {
                clearAlertWindow()
            }
        };
        var noCall = function() {
            call(false);
            clearAlertWindow()
        };
        var em = document.getElementById(ALERT_YES_BUTTON_ID);
        if(!isNull(em)) {
            addEvent(em, "click", yesCall);
        }
        var em = document.getElementById(ALERT_NO_BUTTON_ID);
        if(!isNull(em)) {
            addEvent(em, "click", noCall);
            // default cancel focus.
            em.focus();
        }
    });
}

// add button.
var addButton = function(id, view) {
    return "<a href='javascript:void(0);' id='" + id +
        "' class='base_button'>" + view + "</a>";
}

// isNull(undefined or null).
var isNull = function(value) {
    return value == undefined || value == null; 
}

// rand.
var Xor128 = function(seet) {
    var o = {v:{a:123456789,b:362436069,c:521288629,d:88675123}} ;
    o.setSeet = function(s) {
        try {
            if(!isNull(s)) {
                s = parseInt(s);
            } else {
                s = Date.now();
            }
        } catch(n) {
            s = Date.now();
        }
        if(!isNull(s)) {
            var n = this.v; s = s|0;
            n.a=s=1812433253*(s^(s>>30))+1; n.b=s=1812433253*(s^(s>>30))+2;
            n.c=s=1812433253*(s^(s>>30))+3; n.d=s=1812433253*(s^(s>>30))+4;
        }
    }
    o.next = function() {
        var n = this.v;
        var t=n.a ;
        var r=t ;
        t = (t << 11) ; t = (t ^ r) ;
        r = t ; r = (r >> 8) ;
        t = (t ^ r) ; r = n.b ;
        n.a = r ; r = n.c ;
        n.b = r ; r = n.d ;
        n.c = r ; t = (t ^ r) ;
        r = (r >> 19) ; r = (r ^ t) ;
        n.d = r; return r ;
    }
    o.setSeet(seet) ;
    return o;
}

// ajax.
var ajax = (function() {
    var _ax = function() {
        return new XMLHttpRequest();
    }
    var _head = function(m, ax, h){
        if(m == 'JSON') {
            ax.setRequestHeader('Content-Type',
                'application/json');
        } else if(m == 'POST') {
            ax.setRequestHeader('Content-Type',
                'application/x-www-form-urlencoded');
        }
        if(!isNull(h)) {
            for(var k in h) {
                ax.setRequestHeader(k, h[k]);
            }
        }
    }
    var _method = function(m) {
        return m == 'JSON' ? 'POST' : m;
    }
    // execute ajax.
    return function(method, url, params, header, callback) {
        method = (method+"").toUpperCase() ;
        var pms = "" ;
        if(!isNull(params)) {
            if(typeof(params) == "string") {
                pms = params ;
            } else if(method == "JSON") {
                pms = JSON.stringify(params);
            } else {
                for( var k in params ) {
                    pms += "&" + k + "=" +
                        encodeURIComponent(params[k]) ;
                }
            }
        }
        params = null;
        if(method == "GET" && pms.length > 0) {
            url = url + "?" + pms;
            pms = null;
        }
        // sync.
        if(isNull(callback)) {
            var x = _ax();
            x.open(_method(method), url, false);
            _head(method, x, header);
            x.send(pms);
            var state = x.status;
            if(state == 0) {
                state = 500;
            }
            var ret = x.responseText;
            x.abort() ;
            if(state < 300) {
                return ret;
            }
            throw new Error(
                "response status:" + state + " error: " + ret);
        }
        // async.
        var x = _ax();
        x.open(_method(method), url, true);
        x.onreadystatechange = function() {
            if(x.readyState == 4) {
                try {
                    var status = x.status;
                    status == 0 ? 500 : status;
                    // response headers.
                    var headers = x.getAllResponseHeaders();
                    var arr = headers.trim().split(/[\r\n]+/);
                    var headerMap = {};
                    arr.forEach(function (line) {
                        var parts = line.split(': ');
                        var header = parts.shift();
                        var value = parts.join(': ');
                        headerMap[header.toLowerCase()] = value;
                    })
                    callback(status, x.responseText, headerMap);
                } finally {
                    x.abort();
                    x = null;
                    callback = null;
                }
            }
        };
        _head(method, x, header);
        x.send(pms);
    }
})();

// move page.
var movePage = function(url) {
    window.location.href = url;
}

// encode json value.
var encodeJSON = function(value) {
    return JSON.stringify(value);
}

// parse json value.
var parseJSON = function(value) {
    try {
        value = JSON.parse(value); 
    } catch(e) {
        value = undefined;
    }
    return value;
}

// utf8ToChar8bit.
var _utf8ToChar8bit = function(str, off, len) {
    if(isNull(off)) {
        off = 0;
    }
    if(isNull(len)) {
        len = str.length;
    }
    off = off|0;
    len = len|0;
    len += off
    var lst = [] ;
    var cnt = 0 ;
    var c ;
    for( var i = off ; i < len ; i ++ ) {
        c = str.charCodeAt(i)|0;
        if (c < 128) {
            lst[cnt++] = c|0 ;
        }
        else if ((c > 127) && (c < 2048)) {
            lst[cnt++] = (c >> 6) | 192 ;
            lst[cnt++] = (c & 63) | 128 ;
        }
        else {
            lst[cnt++] = (c >> 12) | 224 ;
            lst[cnt++] = ((c >> 6) & 63) | 128 ;
            lst[cnt++] = (c & 63) | 128 ;
        }
    }
    return lst ;
}

// char8BitToUtf8.
var _char8BitToUtf8 = function(str, off, len) {
    if(isNull(off)) {
        off = 0;
    }
    if(isNull(len)) {
        len = str.length;
    }
    off = off|0;
    len = len|0;
    len += off
    var c ;
    var ret = "" ;
    for( var i = off ; i < len ; i ++ ) {
        c = str[i] & 255;
        if (c < 128) {
            ret += String.fromCharCode(c);
        }
        else if ((c > 191) && (c < 224)) {
            ret += String.fromCharCode(((c & 31) << 6) |
                ((str[i+1] & 255) & 63));
            i += 1;
        }
        else {
            ret += String.fromCharCode(((c & 15) << 12) |
                (((str[i+1] & 255) & 63) << 6) |
                ((str[i+2] & 255) & 63));
            i += 2;
        }
    }
    return ret ;
}

// encodeBase64(UTF8 support).
var _b = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
var encodeBase64 = function(t) {
    var a, c, n;
    var r = '', l = 0, s = 0;
    t = _utf8ToChar8bit(t, 0, t.length);
    var tl = t.length;
    for (n = 0; n < tl; n++) {
        c = t[n]|0;
        if (s == 0) {
            r += _b.charAt((c >> 2) & 63);
            a = (c & 3) << 4;
        } else if (s == 1) {
            r += _b.charAt((a | (c >> 4) & 15));
            a = (c & 15) << 2;
        } else if (s == 2) {
            r += _b.charAt(a | ((c >> 6) & 3));
            l += 1;
            r += _b.charAt(c & 63);
        }
        l += 1;
        s += 1;
        if (s == 3) s = 0;
    }
    if (s > 0) {
        r += _b.charAt(a);
        l += 1;
        r += '=';
        l += 1;
    }
    if (s == 1) {
        r += '=';
    }
    return r;
}

// decodeBase64(UTF8 support).
var decodeBase64 = function(t) {
    var c, n;
    var r = [], s = 0, a = 0;
    var tl = t.length;
    for (n = 0; n < tl; n++) {
        if ((c = _b.indexOf(t.charAt(n))) >= 0) {
            if (s) r[r.length] = (a | (c >> (6 - s)) & 255);
            s = (s + 2) & 7;
            a = (c << s) & 255;
        }
    }
    return _char8BitToUtf8(r, 0, r.length);
}

// header key.
var getHeaderKey = function(key) {
    return key.toLowerCase();
}

// save login tsession
var saveLoginSession = function(signetureCode, response) {
    if(isNull(signetureCode)) {
        signetureCode = localStorage.getItem("signeture");
        if(isNull(signetureCode)) {
            return false;
        }
    }
    // save login session.
    localStorage.setItem("signeture", signetureCode);
    localStorage.setItem("jdbcLoginToken",
        response[getHeaderKey("X-Jdbc-Console-Auth-Token")]);    
    return true;
}

// load login session.
var loadLoginSession = function() {
    var signetureCode = localStorage.getItem("signeture");
    var token = localStorage.getItem("jdbcLoginToken");
    if(isNull(signetureCode) || isNull(token)) {
        return null;
    }
    return {
        "X-Jdbc-Console-Signeture": signetureCode
        ,"X-Jdbc-Console-Auth-Token": token
    };
}

// clear login session.
var clearLoginSession = function() {
    localStorage.removeItem("signeture");
    localStorage.removeItem("jdbcLoginToken");
}

_g.isNull = isNull;
_g.Xor128 = Xor128;
_g.ajax = ajax;
_g.addEvent = addEvent;
_g.timeLagCall = timeLagCall;
_g.parseJSON = parseJSON;
_g.encodeJSON = encodeJSON;
_g.encodeBase64 = encodeBase64;
_g.decodeBase64 = decodeBase64;
_g.getHeaderKey = getHeaderKey;
_g.movePage = movePage;

_g.nowLoading = nowLoading;
_g.alertWindow = alertWindow;
_g.confirmWindow = confirmWindow;
_g.clearAlertWindow = clearAlertWindow;
_g.clearNowLoading = clearNowLoading;

_g.saveLoginSession = saveLoginSession;
_g.loadLoginSession = loadLoginSession;
_g.clearLoginSession = clearLoginSession;

})(this);
