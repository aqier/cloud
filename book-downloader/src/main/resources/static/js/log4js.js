/**
 * 
 */
function Log() {
	if(this.toString()=='[object Window]' && arguments.length != 0) {
		this.L._log(arguments[0], arguments[1], arguments[2], this.Log.caller);
		return ;
	}
	
	/**
	 * log4js 的初始化方法,让其在不支持console对象的平台上不抛错
	 */
	this.init = function() {
		if(typeof console == 'undefined') console = function(){};
		if(console.init) return true;
		console.log   = console.log   || console.debug || console.info || function() {};
		console.debug = console.debug || console.log;
		console.info  = console.info  || console.debug;
		console.warn  = console.warn  || console.info;
		console.error = console.error || console.warn;
		console.fatal = console.fatal || console.error;
		console.init = true; // 记录初始化完成
	};
	
	var _LEVEL = function() {
		this.DEBUG = 1;
		this.INFO  = 2;
		this.WARN  = 3;
		this.ERROR = 4;
		this.FATAL = 5;
    };
    /**
     * Log 等级
     */
	this.LEVEL = new _LEVEL();
	
	
    var _STYLE = function() {
    	/**
         * 在一行上打印一条日志
         */
        this.ONE_LOG_ONE_LINE = true;
    };
    /**
     * 日志风格设置
     */
    this.STYLE = new _STYLE();
    
	/**
	 * 调用者层次,在Log记录调用者信息时,可能你想要记录的并不是直接调用Log方法的人,而是其更高层次的调用者
	 */
	this.CALLER_LEVEL = 0;
	/**
	 * Log 默认等级
	 */
	this.DEFAULT_LEVEL = this.LEVEL.DEBUG;
	/**
	 * 调用初始化方法初始化Log
	 */
	this.init();
	/**
	 * @param {String} msg 信息
	 * @param {Number} level 级别,参见Log.LEVEL
	 * @param {Error} exception 错误对象
	 * @param {Function} {Number} appointCaller 指定调用者,或者其在当前调用者的层次
	 * @param {Function} caller 当前调用者
	 * 
	 */
	this._log = function(msg,level,exception,appointCaller,caller) {
        level = level || this.LEVEL.DEBUG;
        if(level < this.DEFAULT_LEVEL) return ; // 日志级别低于默认级别则返回.
        msg = msg || "";
        exception = exception || "no exception detail";
        
        if(appointCaller == null) {// 没有指定调用者使用默认层次的调用者
        	for(var i=0;i<this.CALLER_LEVEL;i++) caller = caller && caller.caller;
        }else{ // 指定了调用者
        	if(typeof appointCaller == 'number') // 指定了调用者的层次
        		for(var i=0;i<appointCaller;i++) caller = caller && caller.caller;
        	else
        		caller = appointCaller;
        }
        var callerMsg =  caller==null?"no caller detail":"At:"+caller.toString(); // 调用日志者信息
        if(this.STYLE.ONE_LOG_ONE_LINE) callerMsg = callerMsg.replace(/\n|\r|\t|    /g, ' ');
        var date = typeof new Date().format == 'undefined'?new Date().getTime():new Date().format("yyyy-MM-dd hh:mm:ss fff"); // 日志时间信息
        switch(level) {
		    case this.LEVEL.DEBUG : console.debug("["+date+"][debug]["+msg+"]["+exception+"]["+callerMsg+"]");break;
		    case this.LEVEL.INFO  : console.info ("["+date+"][info ]["+msg+"]["+exception+"]["+callerMsg+"]");break;
		    case this.LEVEL.WARN  : console.warn ("["+date+"][warn ]["+msg+"]["+exception+"]["+callerMsg+"]");break;
		    case this.LEVEL.ERROR : console.error("["+date+"][error]["+msg+"]["+exception+"]["+callerMsg+"]");break;
		    case this.LEVEL.FATAL : console.fatal("["+date+"][fatal]["+msg+"]["+exception+"]["+callerMsg+"]");break;
		    default               : console.debug("["+date+"][debug]["+msg+"]["+exception+"]["+callerMsg+"]");break;
        }
	};
}
Log.prototype = {
		/**
		 * @param {String} msg 信息
		 * @param {Error} exception 异常
		 * @param {Function} caller 调用者,默认为当前调用者
		 */
		debug : function(msg,exception,caller) {
		    this._log(msg,this.LEVEL.DEBUG,exception,caller,this.debug.caller);
	    },
	    /**
		 * @param {String} msg 信息
		 * @param {Error} exception 异常
		 * @param {Function} caller 调用者,默认为当前调用者
		 */
	    info : function(msg,exception,caller) {
	    	this._log(msg,this.LEVEL.INFO,exception,caller,this.info.caller);
	    },
	    /**
		 * @param {String} msg 信息
		 * @param {Error} exception 异常
		 * @param {Function} caller 调用者,默认为当前调用者
		 */
	    warn : function(msg,exception,caller) {
	    	this._log(msg,this.LEVEL.WARN,exception,caller,this.warn.caller);
	    },
	    /**
		 * @param {String} msg 信息
		 * @param {Error} exception 异常
		 * @param {Function} caller 调用者,默认为当前调用者
		 */
	    error : function(msg,exception,caller) {
	    	this._log(msg,this.LEVEL.ERROR,exception,caller,this.error.caller);
	    	alert(msg);
	    },
	    /**
		 * @param {String} msg 信息
		 * @param {Error} exception 异常
		 * @param {Function} caller 调用者,默认为当前调用者
		 */
	    fatal : function(msg,exception,caller) {
	    	this._log(msg,this.LEVEL.FATAL,exception,caller,this.fatal.caller);
	    	alert(msg);
	    },
	    /**
		 * 将Object对象转成String类型
		 * @returns {String}
		 */
	    toString : function () {
	    	if(arguments.length == 0) return "Aqier Log for JavaScript";
	        var arr = [];
	        var fn = function(s) {
	            if (typeof s == 'object' && s != null) return this.toString(s);
	            return /^(string|number)$/.test(typeof s) ? "'" + s + "'" : s;
	        };
	        for(var n in arguments) {
	        	for (var i in arguments[n]) {
	        		arr.push("'" + i + "':" + fn(arguments[n][i]));
	        	}
	        }
	        return '{' + arr.join(',') + '}';
	    }
};
window.L = new Log();