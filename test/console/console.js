(function(_g) {
"use strict";

// isNull(undefined or null).
var isNull = function(value) {
    return value == undefined || value == null; 
}

// rand.
var Xor128 = function(seet) {
    var o = {v:{a:123456789,b:362436069,c:521288629,d:88675123}} ;
    o.setSeet = function(s) {
        try {
            if(!isNull(s))
                s = parseInt(s);
            else
                s = Date.now();
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
        if(m=='POST')
            ax.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
        else if(m=='JSON')
            ax.setRequestHeader('Content-Type', 'application/json');
        if(!isNull(h))
            for(var k in h)
                ax.setRequestHeader(k,h[k]);
    }
    var _method = function(m) {
        return m == 'JSON' ? 'POST' : m;
    }
    // execute ajax.
    return function(method, url, params, header, callback) {
        method = (method+"").toUpperCase() ;
        var pms = "" ;
        if(!isNull(params))
            if(typeof(params) == "string")
                pms = params ;
            else
                for( var k in params )
                    pms += "&" + k + "=" + encodeURIComponent(params[k]) ;
        if(method == "GET") {
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
            throw new Error("response status:" + state + " error");
        }
        // async.
        var x = _ax();
        x.open(_method(method), url, true);
        x.onload = function(){
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

// header key.
var getHeaderKey = function(key) {
    return key.toLowerCase();
}

// save login tsession
var saveLoginSession = function(signetureCode, response) {
    // save login session.
    localStorage.setItem("signeture", signetureCode);
    localStorage.setItem("jdbcLoginToken",
        response[getHeaderKey("X-Jdbc-Console-Auth-Token")]);    
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
_g.parseJSON = parseJSON;
_g.encodeJSON = encodeJSON;
_g.getHeaderKey = getHeaderKey;
_g.movePage = movePage;

_g.saveLoginSession = saveLoginSession;
_g.loadLoginSession = loadLoginSession;
_g.clearLoginSession = clearLoginSession;

})(this);
