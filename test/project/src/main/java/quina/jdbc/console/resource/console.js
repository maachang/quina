/**
 * jdbc console js.
 */
(function(_g) {
"use strict";

// max upload file size.
var MAX_UPLOAD_FILE_SIZE = 0x00100000;

// tab space.
var _TABSPACE = '\t';

// input tab element.
var _inputTabElement = function(em) {
    if (em.key === "Tab") {
        em.preventDefault();
        var value = this.value;
        var sPos = this.selectionStart;
        var ePos = this.selectionEnd;
        var result = value.slice(0, sPos) +
            _TABSPACE + value.slice(ePos);
        var cPos = sPos + _TABSPACE.length;
        this.value = result;
        this.setSelectionRange(cPos, cPos);
    }
}

// register inputTab.
var regInputTab = function(em) {
    addEvent(em, "keydown", _inputTabElement);
}

// update work area.
var flushWorkArea = function(html) {
    document.getElementById("workArea")
        .innerHTML = html;
}

// newLine.
var newLine = function(count) {
    if(isNull(count)) {
        return "<br>";
    }
    var ret = "";
    count = count|0;
    for(var i = 0; i < count; i ++) {
        ret += "<br>";
    }
    return ret;
}

// space.
var space = function(count) {
    if(isNull(count)) {
        return "&nbsp;"
    }
    var ret = "";
    count = count|0;
    for(var i = 0; i < count; i ++) {
        ret += "&nbsp;";
    }
    return ret;
}

// create file upload.
var createFileUpload = function(id, mime) {
    var ret = "<input type='file' id='" +
        id + "' ";
    if(!isNull(mime) && mime.length > 0) {
        ret += "accept='" + mime + "' ";
    }
    return ret + "style='display:none'>";
}

// button action.
var _buttonAction = [];

// clear button action.
var _clearButtonAction = function() {
    _buttonAction = [];
}

// flushButton.
var _flushButtonAction = function() {
    if(_buttonAction.length > 0) {
        var em;
        var list = _buttonAction;
        _clearButtonAction();
        var len = list.length;
        for(var i = 0; i < len; i += 2) {
            em = document.getElementById(list[i]);
            (function(idEm, clickFunction) {
                addEvent(idEm, "keydown", function(e) {
                    // enter.
                    if(e.keyCode == 13) {
                        e.preventDefault();
                        // click button.
                        clickFunction();
                    // etc.
                    } else {
                        e.preventDefault();
                    }
                });
                addEvent(idEm, "click", function(e) {
                    e.preventDefault();
                    // click button.
                    clickFunction();
                });
            })(em, list[i + 1]);
        }
    }
}

// create button.
var createButton = function(view, id, js) {
    var btn = "<a href='javascript:void(0);' id='" + id +
        "' class='base_button'>" + view + "</a>";
    // append button action.
    _buttonAction.push(id, js);
    return btn;
}

// sql text area width size.
var SQL_TEXT_AREA_WIDTH = "80%";

// Create a text area for SQL execution. 
var createSqlTextArea = function() {
    var maxHeight = window.innerHeight;
    var tarea = "<textarea id='sql' name='sql' class='base_input_text_area' " +
    "style='width:" + SQL_TEXT_AREA_WIDTH + ";margin-left:16px;";
    tarea += "height:" + ((maxHeight * 0.625)|0) + "px;";
    tarea += "ime-mode:inactive;"
    tarea += "'";
    tarea += " spellcheck='false'";
    tarea += " placeholder=' Set the SQL statement you want to execute.'></textarea>";
    return tarea;
}

// clear sql text area button.
var clearSqlTextAreaButton = function() {
    return createButton("c l e a r", "clearSqlTextAreaId",
        _clearSqlTextArea);
}

// execute button.
var executeSqlButton = function() {
    return createButton("execute", "executeSqlId",
        _executeSql);
}

// Clear resultJSON and generate SQL Text Area. 
var clearResultJSONAndGenerateSqlTextAreaButton = function(no) {
    no = no | 0;
    return createButton("c l e a r", "clearViewSqlTextArea" + no + "Id",
        _clearViewSqlTextArea);
}

// logout button.
var logoutButton = function() {
    return createButton("logout", "accessLogoutConsoleId",
        _accessLogoutConsole);
}

// sql file upload button.
var sqlFileUploadButton = function() {
    return createFileUpload("uploadSqlFile", ".sql,text/x-sql") +
        createButton("upload", "uploadSqlFileButtonToClickId",
            _uploadSqlFileButtonToClick);
}

// create view sql area.
var viewSqlTextArea = function() {
    flushWorkArea(
        clearSqlTextAreaButton() +
        space() +
        sqlFileUploadButton() +
        space(5) +
        executeSqlButton() +
        newLine() +
        createSqlTextArea()
    );
    // focus sql text area.
    timeLagCall(function() {
        // flush button action.
        _flushButtonAction()
        // load sql file.
        var em = document.getElementById("uploadSqlFile");
        addEvent(em, "change", _loadToUploadSqlFile);
        // sql text area to valid TAB key.
        em = document.getElementById("sql");
        regInputTab(em);
        em.focus();
    });
}

// clear view sql area.
var _clearViewSqlTextArea = function() {
    viewSqlTextArea()
    setErrorMessage("");
}

// dataSource selectBox widht size;
var DATA_SOURCE_SELECT_BOX_WIDTH = "30%";

// generate select box.
var createDataSourceSelectBox = function(list) {
    var sbox = "<select size='1' name='selectDataSource' id='selectDataSource' " +
        "class='base_select' style='width:" + DATA_SOURCE_SELECT_BOX_WIDTH +
        ";' onchange='javascript:onChangeSelectDataSource(this);'>";
    sbox += "\n<option value='' hidden>* Select the DataSource to connect to</option>";
    var selected = false;
    var len = list.length;
    var beforeSelect = getSelectDataSource();
    for(var i = 0; i < len; i ++) {
        sbox += "\n<option value='" + list[i] + "'";
        if(beforeSelect != null && beforeSelect == list[i]) {
            sbox += " selected";
            selected = true;
        }
        sbox += ">" + list[i] + "</option>";
    }
    sbox += "</select>";
    sbox += space(5) + logoutButton();
    document.getElementById("dataSourceList").innerHTML = sbox;

    // select dataSource name.
    if(selected) {
        // view sql text area.
        viewSqlTextArea();
    } else {
        // focus select dataSource.
        timeLagCall(function() {
            _flushButtonAction();
            var em = document.getElementById("selectDataSource");
            addEvent(em, "keydown", function(e) {
                // tab.
                if(e.keyCode == 8) {
                    e.preventDefault();
                    em.focus();
                }
            });
            em.focus();
        });
    }
}

// create view sql.
var createViewSql = function(width, sql) {
    return "<div class='sql_view' style='width:"+ width +"'>&nbsp;" +
        decodeBase64(sql) + "</div>";
}

// create view number.
var createViewNumber = function(zero, no) {
    var z = "";
    for(var i = 0; i < zero; i ++) {
        z += "0";
    }
    no = "" + no;
    if(z.length > no.length) {
        return z.substring(no.length) + no; 
    }
    return no;
}

// Odd color of table body.
var TABLE_BODY_BY_ODD_BK_COLOR = "#e7e7e7";

// Generate the list HTML returned by the select statement.
var createResultTable = function(isNumber, width, keys, list) {
    if(isNull(list) || list.length == 0) {
        return "";
    }
    var i, j, o;
    var lenJ = keys.length;
    var ret = "<table class='base_table' style='width:"+ width +"'><thead><tr>";
    // header.
    if(isNumber == true) {
        // append row no.
        ret += "<th style='width:3%;'>&nbsp;</th>";
    }
    for(j = 0; j < lenJ; j ++) {
        ret += "<th>" + keys[j] + "&nbsp;</th>";
    }
    ret += "</tr></thead>";
    // odd rows.
    var odd = " style='background:" + TABLE_BODY_BY_ODD_BK_COLOR + "'";
    var appendOdd = "";
    // body.
    var len = list.length - 1;
    for(var i = 0; i < len; i ++) {
        appendOdd = ""
        if((i & 1) == 1) {
            appendOdd = odd;
        } 
        o = list[i];
        ret += "<tbody><tr>";
        if(isNumber == true) {
            // append row no.
            ret += "<td" + appendOdd + ">" + createViewNumber(5, i + 1) + "&nbsp;</td>";
        }
        for(j = 0; j < lenJ; j ++) {
            ret += "<td" + appendOdd + ">" + o[keys[j]] + "&nbsp;</td>";
        }
        ret += "</tr></tbody>";
    }
    appendOdd = ""
    if((len & 1) == 1) {
        appendOdd = odd;
    } 
    // food.
    ret += "<tfoot><tr>";
    if(isNumber == true) {
        // append row no.
        ret += "<td" + appendOdd + ">" + createViewNumber(5, len + 1) + "&nbsp;</td>";
    }
    o = list[len];
    for(j = 0; j < lenJ; j ++) {
        ret += "<td" + appendOdd + ">" + o[keys[j]] + "&nbsp;</td>";
    }
    ret += "</tr></tfoot>";
    // end.
    return ret + "</table>";
}

// view count.
var createViewCount = function(no) {
    return "<div style='font-size:x-large;font-weight:bold;'><" +
        createViewNumber(3, no) + "></div>";
}

// get result table keys.
var getResultTableKeys = function(list) {
    if(isNull(list) || list.length == 0) {
        return [];
    }
    var cnt = 0;
    // Extract and sort columns.
    var o = list[0];
    var keys = [];
    for(var k in o) {
        keys[cnt ++] = k; 
    }
    keys.sort();
    return keys;
}

// view result json.
var viewResultJSON = function(resultJSON) {
    var o, t, n, keys;
    var sqlList = resultJSON.sqlList;
    var resultValue = resultJSON.value;
    var len = sqlList.length;

    var width = "60%";

    // sql text area button.
    var html = clearResultJSONAndGenerateSqlTextAreaButton(0) +
        newLine();
    
    for(var i = 0; i < len; i ++) {
        o = resultValue[i];
        // Change width for the number of characters.
        if((n = sqlList[i].length) > 60) {
            if(n >= 100) {
                width = "100%";
            } else {
                width = n + "%";
            }
        } else {
            width = "60%";
        }
        // view count.
        html += createViewCount(i + 1);
        // view sql.
        html += createViewSql(width, sqlList[i]);
        // Other than select.
        if((t = typeof(o)) == "number") {
            html += createResultTable(
                false, "10%", ["result"], [{"result": o}]);
        // select query.
        } else if(t == "object") {
            // result table keys.
            keys = getResultTableKeys(o);
            // Change width for the number of keys.
            n = keys.length * 10;
            if(n >= 100) {
                width = "100%";
            } else {
                width = n + "%";
            }
            // create result table.
            html += createResultTable(
                true, width, keys, o);
        }
        // new line.
        html += newLine();
    }
    // view work area.
    flushWorkArea(html +
        clearResultJSONAndGenerateSqlTextAreaButton(1));
    // focus sql text area.
    timeLagCall(function() {
        // flush button action.
        _flushButtonAction();
    });
}

// access logout console.
var _accessLogoutConsole = function() {
    confirmWindow("Log out of the jdbc console. Is it OK?",
    function(yes) {
        if(yes == true) {
            logoutConsole();
            clearAlertWindow(true);
            return true;
        }
    });
}

// click the to upload sql file button.
var _uploadSqlFileButtonToClick = function() {
    setErrorMessage("");
    var uploadEm = document.getElementById("uploadSqlFile");
    uploadEm.click();
}

// load sql file.
var _loadToUploadSqlFile = function() {
    var files = this.files;
    var file = files[0];
    // not file name.
    if(isNull(file.name) || file.name.length == 0) {
        setErrorMessage("Failed to get the uploaded file name.");
        return;
    // max file size.
    } else if(file.size > MAX_UPLOAD_FILE_SIZE) {
        setErrorMessage("A file that is too large has been uploaded.");
        return;
    }
    // load file to update sql text area.
    let reader = new FileReader();
    reader.readAsText(file);
    reader.onload = function() {
        var em = document.getElementById("sql");
        em.value = reader.result + "\n";
        em.focus();
    };
}

// logout console.
var logoutConsole = function() {
    nowLoading();
    try {
        clearLoginSession();
        localStorage.removeItem("selectDataSource");
        movePage("./login.html");
    } catch(e) {
        clearNowLoading();
        throw e;
    }
}

// errorMessage.
var setErrorMessage = function(msg) {
    if(isNull(msg) || (msg = "" + msg.trim()).length == 0) {
        msg = "";
    }
    var e = document.getElementById("errorMessage");
    e.innerHTML = msg;
}

// Save the selected DataSource name.
var onChangeSelectDataSource = function(e) {
    var idx = e.selectedIndex;
    var value = e.options[idx].value;
    // The new dataSource has changed.
    localStorage.setItem("selectDataSource", value);
    // view sql text area.
    viewSqlTextArea();
}

// Get the previously selected DataSource name.
var getSelectDataSource = function() {
    var ret = localStorage.getItem("selectDataSource");
    if(isNull(ret)) {
        return null;
    }
    return ret;
}

// clear sql text area.
var _clearSqlTextArea = function() {
    var tarea = document.getElementById("sql");
    if(isNull(tarea)) {
        return;
    }
    setErrorMessage("");
    if(tarea.value.length != 0) {
        /**
        confirmWindow("Clears the entered SQL content. Is it OK?",
        function(yes) {
            if(yes == true) {
                tarea.value = "";
            }
            // focus sql text area.
            document.getElementById("sql").focus();
        });
        **/
        // clear text area.
        tarea.value = "";
        // focus sql text area.
        document.getElementById("sql").focus();
    } else {
        // focus sql text area.
        document.getElementById("sql").focus();
    }
}

// Get the DataSource list and generate a SelectBox .
var loadDataSourceList = function(count) {
    count = count|0;
    setErrorMessage("");
    nowLoading();
    try {
        // load login session.
        var header = loadLoginSession();
        if(isNull(header)) {
            // logout console.
            logoutConsole();
            return;
        }
    } catch(e) {
        clearNowLoading();
        throw e;
    }
    // call ajax.
    ajax("GET",
        "/quina/jdbc/console/getDataSources", null, header,
        function(state, result, responseHeader) {
            // error auth.
            if(state == 401) {
                // logout console.
                logoutConsole();
                return;
            }
            try {
                var value = parseJSON(result);
                if(isNull(value) || isNull(value = value.value)) {
                    if(count > 3) {
                        // logout console.
                        logoutConsole();
                    } else {
                        // retry.
                        loadDataSourceList(count + 1);
                    }
                    return;
                }
                // generate select box.
                createDataSourceSelectBox(value);
            } finally {
                clearNowLoading();
            }
        });
}

// execute sql.
var _executeSql = function() {
    var header, params;
    setErrorMessage("");
    nowLoading();
    try {
        // load login session.
        header = loadLoginSession();
        if(isNull(header)) {
            // logout console.
            logoutConsole();
            return;
        }
        var dataSource = getSelectDataSource();
        if(isNull(dataSource) || (dataSource = dataSource.trim()).length == 0) {
            setErrorMessage("dataSource is not specified.");
            clearNowLoading();
            return;
        }
        var tarea = document.getElementById("sql");
        if(isNull(tarea) || (sql = tarea.value.trim()).length == 0) {
            setErrorMessage("The SQL statement to be executed does not exist.");
            clearNowLoading();
            if(!isNull(tarea)) {
                tarea.focus();
            }
            return;
        }
        // Convert the sql statement to base64. 
        sql = cutSqlComment(sql).trim();
        var sql = encodeBase64(sql);
        // create json params.
        params = {"dataSource": dataSource, "sql": sql};
    } catch(e) {
        clearNowLoading();
        throw e;
    }
    // call ajax.
    ajax("JSON",
        "/quina/jdbc/console/executeSql", params, header,
        function(state, result, responseHeader) {
            // error auth.
            if(state == 401) {
                // logout console.
                logoutConsole();
                return;
            }            
            try {
                var resultJSON = parseJSON(result);
                if(isNull(resultJSON)) {
                    // result error.
                    setErrorMessage("Execution processing failed.");
                    return;
                }
                // success login.
                if(state == 200) {
                    if(isNull(resultJSON.value)) {
                        // result error.
                        setErrorMessage("Execution processing failed.");
                        return;
                    }
                    // save login session.
                    saveLoginSession(null, responseHeader);
                    // view result json.
                    viewResultJSON(resultJSON);
                } else {
                    // login error.
                    setErrorMessage(resultJSON.message);
                }
            } finally {
                clearNowLoading();
            }
        });
}

// 指定クォーテーション以前に文字列と認識される条件かチェック.
var isStringQuotation = function(src, pos, srcQuotation) {
    if(src.charAt(pos) != srcQuotation) {
        return false;
    }
    var yenCount = 0;
    for(var i = pos - 1; i >= 0; i --) {
        if(src.charAt(i) == '\\') {
            yenCount ++;
            continue;
        }
        break;
    }
    return (yenCount & 1) == 1;
}

// cut sql comment.
var cutSqlComment = function(sql) {
    if(isNull(sql) || sql.length == 0) {
        return "";
    }
    var c;
    var len = sql.length;
    var befYen = false;
    var quate = null;
    var lineCmt = false;
    var cmt = false;
    var ret = "";
    for(var i = 0; i < len; i ++) {
        c = sql.charAt(i);
        // single or double quoating.
        if(quate != null) {
            // end quoating.
            if(!isStringQuotation(sql, i, quate)) {
                quate = null;
            }
            ret += c;
            // before yen code.
            befYen = (c == "\\");
        // [/*] in comment.
        } else if(cmt) {
            // [*/] endComment.
            if(c == "*" && i + 1 < len) {
                if(sql.charAt(i+1) == "/") {
                    i ++;
                    cmt = false;
                }
            }
            // before yen code.
            befYen = (c == "\\");
        // in line comment.
        } else if(lineCmt) {
            // end line.
            if(c == '\n') {
                ret += c;
                lineCmt = false;
            }
            // before yen code.
            befYen = (c == "\\");
        // start quoating.
        // not [\\"] or [\\']
        } else if(!befYen && (c == "\"" || c == "\'")) {
            quate = c;
            ret += c;
            // before yen code.
            befYen = (c == "\\");
        // start line comment [--] or [//] or [#]
        // start comment [/*].
        } else if(c == "-" || c == "/" || c == "#") {
            // start [#] line comment.
            if(c == "#") {
                lineCmt = true;
            // start [--] or [//] line comment.
            // start comment [/*].
            } else if(i + 1 < len &&
                (
                    (c == "/" && sql.charAt(i+1) == "/") ||
                    (c == "/" && sql.charAt(i+1) == "*") ||
                    (c == "-" && sql.charAt(i+1) == "-")
                )
            ) {
                // start comment [/*].
                if((c == "/" && sql.charAt(i+1) == "*")) {
                    cmt = true;
                // start [--] or [//] line comment.
                } else {
                    lineCmt = true;
                }
                i ++;
            // not line comment or not comment.
            } else {
                ret += c;
            }
            // before yen code.
            befYen = (c == "\\");
        // normal.
        } else {
            ret += c;
            // before yen code.
            befYen = (c == "\\");
        }
    }
    return ret;
}

_g.loadDataSourceList = loadDataSourceList;
_g.onChangeSelectDataSource = onChangeSelectDataSource;

})(this);
