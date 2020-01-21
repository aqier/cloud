if (typeof console == 'undefined') {
    console = function() {
    };
    console.log = console.error = console.warn = function() {
    };
}
function JParser() {
    this.local = { // 支持的语种
        zh_CN : {},
        en_US : {}
    };
    this.env = {
        lang : "zh_CN"
    };
    window.i18n = this.local[this.env.lang];
    var Path = function() {
    	var l = window.location;
        var serverHref = l.href;
        var pathname = l.pathname;
        /**
         * 服务器的绝对路径,比如'http://localhost:8080'
         * 
         * @returns {String}
         */
        this.server = l.protocol + "//" + l.host;
        
        var index = pathname.substring(1).indexOf("/");
        
        /**
         * 项目的上下文路径,比如'/webProject',在实际context为 '/' 时会出错
         * 
         * @returns {String}
         */
        this.context = l.pathname;
        /**
         * 项目的绝对路径,比如'http://localhost:8080/webProject'
         * 
         * @returns {String}
         */
        this.base = this.server + this.context;
    };
    /** 项目路径相关 * */
    this.path = new Path();
    /** hash change 事件 * */
    this.noRefreshHash = null; //
    $(function() {
    	$(window).on("hashchange", function() { //兼容ie8+和手机端
    		var hash = window.location.hash;
    		if(Aqier.noRefreshHash != hash) {
    			Aqier.onHashChange(hash);
    		}else { // noRefreshHash 只一次有效
    			Aqier.noRefreshHash = null;
    		}
        });
    });
};

JParser.prototype = {
    constructor : JParser,
    initPage : function(jq, pageHash) {
    	console.log("init page: " + pageHash);
        // 每个表单加上 外包裹form标签, 以便初始化 jquery-validator组件(必须要form标签)
    	jq.find(".form").each(function() {
            var f = $(this);
            if (f.is("form")) return; // form 不需要包裹
            var form = $("<form action=''></form>");
            f.wrap(form);
            form.validate(); // 新加上去的元素要初始化
        });
        // form 表单 绑定提交事件
    	jq.find(".form").bind("submit", function(formEvent, targetEvent) {
            var form = $(this);
            var data = Aqier.getFormJson(form, true);
            if (data === false) return false; // 表单效验不通过
            var action = form.attr("data-action"); // 表单提交的URL
            if (Aqier.isBlank(action)) return false;
            var url = Aqier.parseFn(action); // 解析URL
            var method = form.attr("data-method"); // 表单提交的方式
            console.log("form submit to:" + url);
            Aqier.getJSON(url, data, function(html) {
                $("html").html(Aqier.parseFn(html));
            });
        });
        // form 表单下的提交事件
    	jq.find(".form .submit").bind("click", function(event) {
            $(this).parents(".form").trigger("submit", event); // 触发表单提交
        });
    	if($.fn.initPage != null) {
    		for(var key in $.fn.initPage) {
    			var fn = $.fn.initPage[key];
    			if(typeof fn == "function") {
    				fn(jq);
    			}
    		}
    	}
    },
    putLocal : function(language, localMap) {
    	var local = this.local[language];
    	if(local == null) {
    		local = {};
    		this.local[language] = local;
    	}
    	$.extend(local, localMap);
    },
    getJSON : function(url, param, fn, options) {
        var result = null;
        if(param != null && typeof param != "string") {
        	param = JSON.stringify(param);
        }
        var opts = $.extend({
            url : url,
            async : (fn != null),
            type : "GET",
            contentType : "application/json;charset=UTF-8",
            data : param,
            success : fn || function(data) {
                result = data;
            }
        }, options || {});
        result = $.ajax(opts);
        return result;
    },
    postJSON : function(url, param, fn, options) {
    	options = $.extend(options, {type:"POST"});
    	return this.getJSON(url, param, fn, options);
    },
    escapeHtml : function(text) {
    	if(text == null) {
    		return text;
    	}
    	return text.replace(/[<>"&]/g, function(match, pos, originalText) {
    	    switch(match) {
	    	    case "<": return "&lt;";
	    	    case ">": return "&gt;";
	    	    case "&": return "&amp;";
	    	    case "\"":return "&quot;";
    	    }
        });
    },
    parseFn : function(txt) {
        // 替换或执行函数 {[function() {return 'HaHa';}]} (?!'[.*\]\}.*]'|"[.*\]\}.*]")
        var reg = /\{\[([^\]]*(?!'[.*\]\}.*]'|"[.*\]\}.*]"))\]\}/g;
        return txt.replace(reg, function(match, group, index, orgStr) {
            var result = group;
            try {
                result = eval(group);
            } catch (e) {
                console.log("parseFn error:", e);
            }
            console.log("replace:", group, " to:", result);
            return result;
        });
    },
    onHashChange : function(hash) {
    	console.log("window will jump to: " + hash);
        hash = hash || window.location.hash;
        if (!Aqier.isBlank(hash) && hash != "#") {
            hash = Aqier.parseFn(hash.substring(1));
            Aqier.getJSON(hash, null, function(html) {
                html = Aqier.parseFn(html);
                var jq = $("#centerPage");
                jq.hide(); // 防止初始化过程被显示
                jq.html(html);
                Aqier.initPage(jq, hash);
                jq.show();
            }, {
                dataType : "text",
                contentType : "text/html;charset=UTF-8",
                type : "GET"
            });
        }
    },
    changeHashNoRefresh : function(hash) {
    	Aqier.noRefreshHash = hash;
    	window.location.hash = hash;
    },
    isBlank : function(obj) {
        return obj == null || $.trim(obj.toString()) == "";
    },
    isNull : function(obj) {
        return obj === null || obj === undefined;
    },
    /**
     * 
     * @param formJq
     *            表单
     * @param valid
     *            是否效验表单
     * @returns 不需效验或效验通过则返回json,否则效验不通过返回false
     */
    getFormJson : function(formJq, valid) {
        if (Aqier.isNull(formJq)) formJq = $("form");
        var formJson = {};
        var result = true;
        formJq.find("[name]").each(function() {
            var theJq = $(this);
            if (valid) result = theJq.valid() && result;
            formJson[theJq.attr("name")] = theJq.val();
        });
        return !valid ? formJson : (result ? formJson : false);
    },
    validForm : function(formJq, names) {
        if (formJq == null) formJq = $("form");
        if (!$.isArray(names)) {
            if (names == null) names = [];
            else names = [
                names
            ];
        }
        var result = true;
        formJq.find("[name]").each(function() {
            if (names.length === 0 || names.indexOf(this.name) != -1) { // 在效验的元素中
                result = $(this).valid() && result;
            }
        });
        return result;
    },
    i18n : function(key) {
        var local = Aqier.local[Aqier.env.lang];
        var value = key;
        if (!Aqier.isNull(local)) {
            try {
                value = eval("value = Aqier.local." + Aqier.env.lang + "." + key);
            } catch (e) {
                console.log("get i18n error:", e);
            }
        }
        return value || key;
    },
    loadPage : function(hash, params) {
    	window.location.hash = hash;
    },
    loadResource : function(types) {
        var cssTag = '<link rel="stylesheet" href="{}"></link>';
        var scriptTag = '<script src="{}"></script>';
        var css = [
            "/lib/bootstrap/css/bootstrap.min.css"
        ];
        var script = [
            "/js/json2.js", "/lib/bootstrap/js/bootstrap.min.js", "/lib/jquery-validation/jquery.validate.min.js", "/lib/jquery-validation/additional-methods.min.js", "/lib/jquery-validation/localization/messages_zh_CN.min.js", "/js/placeholder.js"
        ];
        var result = "";
        if (types == null) {
            for (var i = 0; i < css.length; i++) {
                result += cssTag.replace("{}", Aqier.path.resource + css[i]) + "\n";
            }
            for (var j = 0; j < script.length; j++) {
                result += scriptTag.replace("{}", Aqier.path.resource + script[j]) + "\n";
            }
        }
        return Aqier.comment(result);
    },
    getUrlParam : function(name, search) {
        search = search || window.location.hash;
        var index = search.indexOf("?");
        if (index == -1) return null;
        var split = search.substring(index + 1).split("&");
        var result = {};
        for (var i = 0; i < split.length; i++) {
            var p = split[i].split("=");
            result[p[0]] = p.length > 1 ? p[1] : "";
        }
        return (name != null) ? result[name] : result;
    },
    changeParam : function(queryStr, paramName, paramValue) {
        var pattern = paramName + '=([^&]*)';
        var replaceText = paramName + '=' + paramValue;
        if (queryStr.match(pattern)) {
            var tmp = '' + paramName + '=[^\\&]*';
            tmp = queryStr.replace(new RegExp(tmp), replaceText);
            return tmp;
        } else {
            if (queryStr.match('[\?]')) {
                return queryStr + '&' + replaceText;
            } else {
                return queryStr + '?' + replaceText;
            }
        }
        return queryStr + '\n' + paramName + '\n' + paramValue;
    },
    comment : function(txt) {
        if (txt == null) txt = "";
        return "-->" + txt + "<!--";
    },
    changeLang : function(lang) {
        if(Aqier.local[lang] == null || Aqier.env.lang == lang) return;
        window.location.search = Aqier.changeParam(window.location.search, "lang", lang);
    }
}
if (typeof Aqier == "undefined") { // 只初始化一次
	JP = Aqier = new JParser();
	if(window.location.hash != "") {
		Aqier.onHashChange(window.location.hash);
	}
}
