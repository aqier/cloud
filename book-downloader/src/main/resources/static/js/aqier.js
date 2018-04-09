/**
 * @param {Window} win
 * 依赖jQuery 
 */
function Aqier(win) {
	/**
	 * 当前引用的Window对象
	 * @returns {Window}
	 */
	this.win = win || window;
	/**
	 * 当前引用的Document对象的简写
	 * @returns {Document}
	 */
	this.d = this.win.document;
	
	
	var serverHref = this.win.location.href;
	var pathname = this.win.location.pathname;
	/**
	 * 服务器的绝对路径,比如'http://localhost:8080'
	 * @returns {String}
	 */
	this.serverPath = serverHref.substring(0,serverHref.indexOf(pathname));
	var index = pathname.substring(1).indexOf("/");
	/**
	 * 项目的绝对路径,比如'/webProject'
	 * @returns {String}
	 */
	this.basePath = index==-1?pathname:pathname.substring(0,index);
	/**
	 * 
	 */
	var _TYPE = function() {
		this.Null = "null";
		this.String = "String";
		this.Integer = "Integer";
		this.Double = "Double";
		this.Boolean = "Boolean";
		this.Array = "Array";
		this.Function = "Function";
		this.JSON = "JSON";
		this.JSONArray = "JSONArray";
		this.Object = "Object";
		var _Element = function() {
			this.Text = "Text";
			this.CheckBox = "CheckBox";
			this.Password = "Password";
			this.HiddenText = "HiddenText";
			this.File = "File";
			this.Radio = "Radio";
			this.Button = "Button";
			this.Image = "Image";
			this.Reset = "Reset";
			this.Submit = "Submit";

			this.Select = "Select";
			this.TextArea = "TextArea";
			this.toString = function(){return "Element";};
			/**
			 * 可以记录输入信息的元素
			 */
			this.Message = "Text,CheckBox,Password,HiddenText,File,Radio,TextArea,Select,";
		};
		/**
		 * @returns Document 枚举对象
		 */
		this.Element = new _Element();
	};
	/**
	 * @returns javaScript 对象的枚举类型
	 */
	this.TYPE = new _TYPE();
	
};

Aqier.prototype =  {
/**
 * 私有属性,请勿修改此值;用来保存document初始化后要调用的函数
 */
documentReady : [],
/**
 * 私有属性,请勿修改此值
 * 当满足条件时,执行函数
 * key 条件的String类型表达式
 * value 要执行的函数
 */
satisfyCondition : [],
/**
 * 日志记录对象
 * @returns {Log}
 */
log : new Log(),
/**
 * 获取JQuery对象
 */
getJQuery : function() {
	var JQ = this.win.$ || this.win.jQuery;
	if(JQ == null) {
		this.log.warn("在Aqier引用的Window中没有找到jQuery插件,你是否忘记引用该插件?", null, 0);
	}else return JQ;
	
	JQ = this.win.parent && (this.win.parent.$ || this.win.parent.jQuery);
	if(JQ == null) {
		this.log.warn("在Aqier引用的Window的父窗口中也没有找到jQuery插件", null, 0);
	}else return JQ;
	
	JQ = this.win.parent && (this.win.parent.$ || this.win.parent.jQuery);
	if(JQ == null) {
		this.log.warn("在Aqier引用的Window的父窗口中也没有找到jQuery插件", null, 0);
	}else return JQ;
	
},
/**
 * 错误捕获器
 */
errorHandle : function(msg,url,rowNumber,colNumber,exception) {
	/**{Aqier} **/
	var aqier = typeof this.log == 'undefined'?this.A:this;
	aqier.log.error(msg,"error:url='"+url+"',row:"+rowNumber+",col:"+colNumber,1); // this 总是指向调用该方法的对象,而该方法总是被window调用,故用this.A
	// 将错误信息上传到服务器,以待分析错误原因;
	aqier.executeAjax('jsErrorHandle',{msg:msg,url:url,rowNumber:rowNumber,colNumner:colNumber,exception:exception});
	return false;
},
/**
 * @description 绑定document初始化后执行的函数
 * @param {Function} fn 要添加绑定的函数
 */
onDocumentReady : function(fn) {
	this.documentReady.push(fn);
},

/**
 * @description 绑定当condition为true时执行函数fn
 * @param {String} condition 条件,要有唯一性(做JSON的key用的),将使用eval来处理
 * @param {Function} fn 要添加绑定的函数,如果条件满足时要执行多个函数,可在该函数内调用一系列其他的函数
 */
onCondition : function(condition,fn) {
	this.satisfyCondition.push({'condition':condition,'fn':fn,'executed':false});
},
/**
 * 
 * @param {Object} o
 * @returns {String} 详情参见 {@link Aqier.TYPE}
 */
getType : function(o) {
	var tp = typeof o;
	if(tp == 'undefined') {
		return this.TYPE.Null;
	}else if(tp == 'string') {
		return this.TYPE.String;
	}else if(tp == 'number') {
		return o.toString().indexOf(".")==-1?this.TYPE.Integer:this.TYPE.Double;
	}else if(tp == 'boolean') {
		return this.TYPE.Boolean;
	}else if(tp == 'function') {
		return this.TYPE.Function;
	}else if(tp == 'object') {
		if(o instanceof Array) {
			if(o.toString().lastIndexOf('[object Object]') == 0) {
				return this.TYPE.JSONArray;
			}else return this.TYPE.Array;
		}
		if(o.toString().lastIndexOf('[object Object]') == 0) { // JSON 或 JSON 数组
			if(o.length) {
				return this.TYPE.JSONArray;
			}else return this.TYPE.JSON;
		}else if(o.toString().indexOf('[object HTML') == 0) { // HTML 元素
			if(o.toString() == '[object HTMLInputElement]') { // Input 元素
				tp = o.type;
				if(tp == 'checkbox') {
					return this.TYPE.Element.CheckBox;
				}else if(tp == 'button') {
					return this.TYPE.Element.Button;
				}else if(tp == 'file') {
					return this.TYPE.Element.File;
				}else if(tp == 'text') {
					return this.TYPE.Element.Text;
				}else if(tp == 'hidden') {
					return this.TYPE.Element.HiddenText;
				}else if(tp == 'radio') {
					return this.TYPE.Element.Radio;
				}else if(tp == 'password') {
					return this.TYPE.Element.Radio;
				}else if(tp == 'image') {
					return this.TYPE.Element.Image;
				}else if(tp == 'reset') {
					return this.TYPE.Element.Reset;
				}else if(tp == 'submit') {
					return this.TYPE.Element.Submit;
				}
		    }else if(o.toString() == '[object HTMLSelectElement]') {
		    	return this.TYPE.Element.Select;
		    }else if(o.toString() == '[object HTMLTextAreaElement]') {
		    	return this.TYPE.Element.TextArea;
		    }
			return this.TYPE.Element.toString();
		} // 
		
	}
	return this.TYPE.Object;
},
/**
 * @param {Aqier.TYPE} type 参数的类型,详情参见Aqier.TYPE;比如"String"
 * @param argument 提供用来筛选的数据,默认使用调用者函数的参数列表;比如[2,3.14,'someString',{JSON:JSON}]
 * @returns {Array} 返回找到所有符合类型type的数据;比如['someString']
 */
getParamenter : function(type,argument) {
	// 此处有些元素为Null,但是使用for任然可以访问
	if(argument == null) try{if(argument.length);}catch(e){argument = A.getParamenter.caller.arguments;}
	var result = new Array();
	if(type == 'Message') type = this.TYPE.Element.Message;
	var types = type.split(",");
	for(var i in argument) {
		var arg = argument[i];
		var tp = this.getType(arg);
		for(var j in types) {
			if(types[j] == tp) {
				result.push(arg);
				break;
			}
		}
	}
	return result;
},
/**
 * 根据id或name获取相应的document元素
 * @param idOrName
 * @returns {Element}
 */
element : function(idOrName) {
	var obj = this.d.getElementById(idOrName);
	if(obj == null) {
		obj = this.d.getElementsByName(idOrName);
		if(obj != null && obj.length>0) obj = obj[0];
	}
	return obj;
},
/**
 * 
 */
elements : function(element) {
	
},
/**
 * @param {String} idOrName 要获取值元素的id或者name
 * @param {String} value 要设置的值
 * @return {Boolean} 是否设置成功(处决与是否找到该元素)
 */
s : function(idOrName,value) {
	var obj = this.element(idOrName);
	if(obj !=null) obj.value = value;
	return obj != null;
},
/**
 * @param {String} idOrName 要获取值元素的id或者name
 * @return {String} 获取的 value
 */
g : function(idOrName) {
	var obj = this.element(idOrName);
	if(obj !=null) return obj.value;
},
/**
 * 获取可靠的URL路径,当 URL 以'/'或者'http://' 开头时此函数不起任何作用
 * @param {String} URL
 */
ensureURL : function(URL) {
	return URL.indexOf('/')==0 || URL.indexOf('http://')==0?URL:this.basePath+"/"+URL;
},
/**
 * 去掉text两边的s字符串
 * @param {String} text 要调整的字符串
 * @param {String} char 要将哪些字符串去掉,默认为空格
 * @returns 调整后的字符串
 */
trim : function(text,s) {
	if(s == null) s = " ";
	if(typeof text == 'string') {
		while(text.indexOf(s) == 0) {
			text = text.substring(s.length);
		}
		while(text.lastIndexOf(s) == text.length - s.length) {
			text = text.substring(0,text.length-s.length);
		}
	}
	return text;
},
/**
 * 与 jQuery.getJSON 不同,jQuery.getJSON采用的是异步方式加载
 * @param {String} url 可跨域请求
 * @param {JSON} data
 * @returns {JSON} 同步方式向服务器查询的数据
 */
executeAjax : function(url,data) {
	var json = null;
	if(url.indexOf("http://")==0 && url.indexOf(basePath)!=6) { // 满足条件的就是跨域请求
		
	}else {
		this.win.$.ajax({
			type : "post",
			url : this.ensureURL(url),
			data : data,
			async : false,
			cache : false,
			dataType : "json",
			error : function() {
				Log("向服务器查询数据出错!",Log.LEVEL.DEBUG);
			},
		    success : function(data) {
		    	json = data;
		    }
		});
		return json;
	}
},
getJSON : function(url, param, success, options) {
    var result = null;
    if(param != null && typeof param != "string") {
    	param = JSON.stringify(param);
    }
    var opts = $.extend({
        url : url,
        async : (success != null),
        type : "GET",
        contentType : "application/json;charset=UTF-8",
        data : param,
        success : success || function(data) {
            result = data;
        }
    }, options || {});
    if(success != null) {
    	result = $.ajax(opts);
    }else {
    	$.ajax(opts);
    }
    return result;
},
postJSON : function(url, param, fn, options) {
	options = $.extend(options, {type:"POST"});
	return this.getJSON(url, param, fn, options);
},
/**
 * @param form 可以是form对象,也可以是form元素的id
 * @return {JSON} 获取表单下所有 有name或id属性元素的JSON集合,若name和id有冲突,以name优先
 */
getFormValue : function(form) {
	var json = {};
	form = form==null?this.d.getElementsByTagName("form")[0]:(typeof form=='string'?this.d.getElementById(form):form);
	for(var i=0;i<form.length;i++) {
		var obj = form.elements[i];
		var value = obj.value;
		if(obj.type == 'checkbox') value = obj.checked?'true':'false';
    	if(value != null) { // 值不为空
    		if(obj.name != null) {
    			json[obj.name] = value;// 优先使用name做键
    		}else if(obj.id != null && json[obj.id] == null) { // 再使用id
    			json[obj.id] = value;
    		}
    	}
	}
	return json;
},
/**
 * 给form表单赋值
 * @param {JSON} json 值
 * @param form 表单
 * @param {Function} fn({Element} obj,{String} value) 设置值时的回掉函数
 */
setFormValue : function(json,form,fn) {
	if(form != null && form instanceof Function) { // 参数位置可变动
		var tmp = form;
		form = fn;
		fn = tmp;
	}
	form = form==null?this.win.$("form"):this.win.$(typeof form=='string'?'#'+form:form);
    form.children().each(function(i,obj) {
    	var value = json[obj.name] || json[obj.id];
    	if(value != null) { // 找到值,开始赋值
    		obj.value = json[obj.name]; // 直接赋值
    		if(fn) fn(obj,value); // 如果不能通过直接赋值解决问题,这里调用传入的回掉函数
    	}
	});
},
/**
 * 给定的表达式 express 是否是可访问的,如果不能访问则返回false,如果能访问则返回true
 * @param {String} express
 * @returns {Boolean}
 */
accessable : function(express) {
	try{
		eval(express.toString());
		return true;
	}catch(e){
		return false;
	}
},
/**
 * @param expOrFn 表达式{String}或者方法{Function}
 * @param args 如果 expOrFn 是方法,则 args 是传递给方法的参数
 * @returns 执行方法或函数的返回值,如果出错则返回该错误
 */
tryAccess : function(expOrFn,args) {
	if(expOrFn == null) return null;
	var type = typeof expOrFn;
	try{
		if(type == "function") {
			return expOrFn(args);
		}else {
			return eval(expOrFn.toString());
		}
	}catch(e){
		return e;
	};
},
/**
 * @param id 要生成树的DIV的id属性值
 * @param url 继承 TreeController类的@RequestMapping注解值
 * @param data ligerUiTree 的附加参数
 * @return ligerUiTree 对象
 */
tree : function(id,url,data) {
	var gc = "load"; // TreeController 类中的根据父节点id加载子节点的方法对应的 @RequestMapping 值
	data = data || {};
	if(typeof url == 'string') {
		var index = url.lastIndexOf("."); // 可能 URL 带有如 .action 的后缀
		var loadAll = url.indexOf(gc) != url.length-gc.length-(index==-1?0:index); // 是否要延迟加载处决与调用的方法是否是 gc 对应的方法
		data.url = url;
		data.delay = loadAll?false:function(node) {
			var p = node.data;
			return {'url':url+'?id='+p.id+'&level='+p.level};
		};
		data.idFieldName = "id";
		data.parentIDFieldName = "pid";
		data.isLeaf = function(data) {
			if(!data) return false;
			return data.hasChild==false;
		};
	}else if(typeof url == 'object') {
		data.data = url;
	}else return null;
	return this.win.$("#"+id).ligerTree(data);
}
// end of Aqier
};
window.A = new Aqier();
A.win.onerror = A.errorHandle; // 指定window的错误捕获器
//document 加载完成后执行绑定在Aqier上的方法
A.onCondition("A.d.readyState && A.d.readyState=='complete'", function() {
	for(var i in A.documentReady) {
		A.documentReady[i]();
	}
});
// document 和 jQuery 加载完成后,初始化一些监听器
A.onDocumentReady(function() { // document,加载完成
	A.onCondition("typeof A.win.$ != 'undefined'", function() { // jQuery 加载完成
		
		A.win.$("input[id^='checkAll-']").click(function() { // 全选监听,比如定义了<input type='checkBox' id='checkAll-CK'/> 那么所有input元素的id以CK开头的都将加入到全选中
	        var obj = this;
	        var id = this.id;
	        if(obj.type != 'checkbox') obj.checked = !obj.checked;
        	A.win.$("input[id^='"+id.substring(9)+"']").each(function() {
        		this.checked = obj.checked;
        	});
	    });
	    
		A.win.$("input[id^='checkCounter-']").click(function() { // 反选监听,同全选
	    	var id = this.id;
	    	A.win.$("input[id^='"+id.substring(13)+"']").each(function() {
	    		this.checked = !this.checked;
	    	});
	    });
		
	});
});
//当条件满足时执行对应的函数
(function(){
	for(var i in A.satisfyCondition) {
		var json = A.satisfyCondition[i];
		if(!json['executed']) {
			if (eval(json['condition'])) {
				json['executed'] = true;
				A.tryAccess(json['fn']);
			}
		}
	}
	if(!A.endMonitor) setTimeout(arguments.callee, 300);  // 每300毫秒检查一次条件是否满足
})();