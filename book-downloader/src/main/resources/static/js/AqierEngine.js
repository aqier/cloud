(function($, aqier, log) {
var AqierEngine = function(selector, initPage) {
	// 当hash改变后等于此值, 将不触发onHashChange事件
	this.noRefreshHash = null;
	this.selector = selector || "#contentPage";
	this.initPage = initPage || function(){};
	// 监听事件
	$(window).on("hashchange", function() { //兼容ie8+和手机端
		var hash = window.location.hash;
		if(aqier.engine.noRefreshHash != hash) { // 不刷新页面的hash
			aqier.engine.onHashChange(hash);
		}else { // noRefreshHash 只一次有效
			aqier.engine.noRefreshHash = null;
		}
	});
}
AqierEngine.prototype = {
	parse : function(text) {
        // 替换或执行函数 {[function() {return 'HaHa';}]} (?!'[.*\]\}.*]'|"[.*\]\}.*]")
        var reg = /\{\[([^\]]*(?!'[.*\]\}.*]'|"[.*\]\}.*]"))\]\}/g;
        return text.replace(reg, function(match, group, index, orgStr) {
            var result = group;
            try {
                result = eval(group);
            } catch (e) { // try catch 即使解析失败, 也不影响其他部分解析
        		log.error("parseHtml error:", e);
            }
            log.debug("replace [" + group + "] to [" + result + "]");
            return result;
        });
    },
    onHashChange : function(hash) {
    	log.debug("Begin load page: " + hash);
        hash = hash || window.location.hash;
        if (!aqier.isBlank(hash) && hash != "#") {
        	var engine = aqier.engine;
            hash = engine.parse(hash.substring(1));
            aqier.getJSON(hash, null, function(html) {
                html = engine.parseFn(html);
                var jq = $(engine.selector);
                jq.hide(); // 防止初始化过程被显示
                jq.html(html);
                engine.initPage(jq, hash);
                jq.show();
            }, {
                dataType : "text",
                contentType : "text/html;charset=UTF-8",
                type : "GET",
                error : function() {
                	alert("loading page ["+hash+"] error");
                }
            });
        }
    }
}

if(aqier.engine === undefined) {
	aqier.engine = new AqierEngine();
}
// execute
})(jQuery, aqier, log)