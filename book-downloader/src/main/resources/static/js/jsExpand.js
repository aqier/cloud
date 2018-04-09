/**
 * Object 克隆方法
 * @returns {Object}
 */
Object.prototype.Clone = function(){
    var objClone;
    if (this.constructor == Object){
        objClone = new this.constructor(); 
    }else{
        objClone = new this.constructor(this.valueOf()); 
    }
    for(var key in this){
        if (objClone[key] != this[key] ) {
            if ( typeof(this[key]) == 'object' ) {
                objClone[key] = this[key].Clone();
            }else{
                objClone[key] = this[key];
            }
        }
    }
    objClone.toString = this.toString;
    objClone.valueOf = this.valueOf;
    return objClone; 
};
/**
 * 时间格式转换函数 
 * @param format  格式自定义 例如：yyyyMMdd、yyyy-MM-dd hh:mm:ss等等
 * 调用过程：mydate = new Date('2012-05-10 10:10:21');  alert(mydate.format("yyyy-MM-dd hh:mm:ss fff"));
 */
Date.prototype.format = function(format) {
	if(!(this instanceof Date)) {
		Log("Date.format 出错:'this instanceof Date' 返回值为false");
		return null;
	}
    var o = {
            "M+" : this.getMonth()+1, //month
            "d+" : this.getDate(), //day
            "h+" : this.getHours(), //hour
            "m+" : this.getMinutes(), //minute
            "s+" : this.getSeconds(), //second
            "q+" : Math.floor((this.getMonth()+3)/3), //quarter
            "f+" : this.getMilliseconds() //millisecond
    };
    if(/(y+)/.test(format))
    format=format.replace(RegExp.$1,(this.getFullYear()+"").substr(4 - RegExp.$1.length));
    for(var k in o)
    if(new RegExp("("+ k +")").test(format))
    format = format.replace(RegExp.$1,RegExp.$1.length==1 ? o[k] : (RegExp.$1.length==2 ? ("00"+ o[k]).substr((""+ o[k]).length) : ("000"+ o[k]).substr((""+ o[k]).length)));
    return format;
};