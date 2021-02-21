$.extend($.fn.bootstrapTable.defaults, $.fn.bootstrapTable.locales[Aqier.env.lang.replace("_", "-")]);

$.extend($.fn.bootstrapTable.defaults, {
	method : "POST",
	contentType : "application/json;charset=utf8",
	striped : true, /*设置为 true 会有隔行变色效果*/
    sortName : "last_update_date desc,id", /*默认排序字段*/
    sortOrder : "asc", /*定义排序方式，'asc' 或者 'desc'*/
    sortStable : true, /*设置为 true 将获得稳定的排序，我们会添加\_position属性到 row 数据中*/
    queryParamsType : "pageNo", /*设置为 'limit' 则会发送符合 RESTFul 格式的参数。limit' ,返回参数必须包含limit, offset, search, sort, order 否则, 需要包含: pageSize, pageNumber, searchText, sortName, sortOrder. */
    adapterQueryParams : function(params) { // 适配前台框架 BootstrapTable的分页参数, 使之兼容后台Page分页参数
    	if($.isPlainObject(params)) {
    		console.log("Before adapter page query params:"+JSON.stringify(params));
    		// 适配 orderBy
    		if(!Aqier.isBlank(params.sortName)) {
    			params.orderBy = $.trim($.trim(params.sortName) + " " + $.trim(params.sortOrder));
    		}else {
    			params.orderBy = "last_update_date desc,id asc"; // 默认排序, 排序加入 id 防止last_update_date一样, 分页混乱
    		}
    		delete params.sortName;
    		delete params.sortOrder;
    		// 查找 BootstrapTable对象
    		var table = this.tableJq;
    		if(table == null && this.selector != null) {
    			table = $(this.selector);
    		}
    		if(table == null) {
    			table = $(".bootstrap-table .table");
    		}
    		var search = null;
    		if(table != null && table.length > 0) {
    			this.tableJq = table;
    			search = table.parents(".table-area").find(".search-area");
    		}
    		if(search != null && search.length > 0) {
    			params.rows = [Aqier.getFormJson(search)];
    		}else {
    			params.rows = [{}];
    		}
    		// 适配  pageNumber
    		if(params.pageNumber != null) {
    			params.pageNo = params.pageNumber;
    			delete params.pageNumber;
    		}
    		console.log("After adapter page query params:" + JSON.stringify(params));
    	}
    	return params;
    },
    queryParams :  function(params) { // 默认为 window.queryParams
    	var params = this.adapterQueryParams(params);
    	if(typeof this.getQueryParams == "function") {
    		params = this.getQueryParams(params);
    	}else if(typeof window.getQueryParams == "function") {
    		params = window.getQueryParams(params);
    	}
    	var isPost = this.method != null && this.method.toLowerCase() == "post";
    	if(isPost && $.isPlainObject(params)) {
    		params = JSON.stringify(params);
    	}
    	return params;
    },
    pagination : true, /*是否分页*/
    paginationLoop : false, /*设置为 true 启用分页条无限循环的功能*/
    onlyInfoPagination : false, /*设置为 true 只显示总数据数，而不显示分页按钮。需要设置 pagination='true'*/
    sidePagination : "server", /*设置在哪里进行分页，可选值为 'client' 或者 'server'。设置 'server'时，必须设置服务器数据地址（url）或者重写ajax方法*/
    pageNumber : 1, /*[1]:如果设置了分页，首页页码*/
    pageSize : 10, /*如果设置了分页，页面数据条数*/
    pageList : [5, 10, 20, 50], /*如果设置了分页，设置可供选择的页面数据条数。设置为 All 或者 Unlimited，则显示所有记录*/
    escape : true, /*true: 转义HTML字符串，替换 &, <, >, ", \`, 和 ' 字符。*/
    search : true, /*是否启用搜索框*/
    searchOnEnterKey : true, /*设置为 true时，按回车触发搜索方法，否则自动触发搜索方法*/
    strictSearch : false, /*设置为 true启用全匹配搜索，否则为模糊搜索*/
    searchText : "", /*初始化搜索文字, bug: 值为null时, 导致初始化时查询两次*/
    showRefresh : true, /*是否显示刷新按钮*/
    showToggle : true, /*是否显示切换视图（table/card）按钮*/
    showPaginationSwitch : false, /*是否显示切换分页按钮*/
    showFullscreen : true, /*是否显示全屏按钮*/
    cardView : false, /*设置为 true将显示card视图，更适用于移动设备。否则为table视图*/
    detailView : false, /*设置为 true 可以显示详细页面模式*/
    toolbar : "#table-toolbar", /*一个jQuery 选择器，指明自定义的 toolbar。例如:#toolbar, .toolbar.*/
    silentSort : true /*设置为 false 将在点击分页按钮时，自动记住排序项。仅在 sidePagination设置为 server时生效。*/
});

$.extend($.fn.bootstrapTable.columnDefaults, {
	
});

$.fn.initPage = $.extend($.fn.initPage||{}, { /*给aqierJParse初始化页面用*/
	bootstrapTable : function(jq) {
		// 初始化 表格
		jq.find("table[data-toggle='table']").each(function(e, index) {
			var talbeJq = $(e);
			tableJq.bootstrapTable({ // option 中保存
				table : tableJq
			});
		});
		jq.find(".table-area .search-btn").on("click", function(event) {
			var btnJq = $(event.target);
			var tableJq = btnJq.parents(".table-area").find(".bootstrap-table .table");
			var bootstrapTable = tableJq.bootstrapTable("refresh");
		});
		
	}
});
