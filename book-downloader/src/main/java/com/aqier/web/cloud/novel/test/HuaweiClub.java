package com.aqier.web.cloud.novel.service.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class HuaweiClub extends NovelDownloader {

	public static void main(String[] args) throws IOException {
		new HuaweiClub().get();
	}
	
	private String url = "https://club.huawei.com/viewthreaduni-15625982-filter-dateline-orderby-lastpost-dateline-86400-page-1-";
	
	public HuaweiClub() {
		this.searchUrl = url;
	}
	
	public void get() throws IOException {
		StringBuilder excel  = new StringBuilder("<table>");
		excel.append("<tr><td>楼层</td><td>昵称</td><td>接入城市</td>"
				+ "<td>接入终端</td><td>带宽</td><td>时延</td>"
				+ "<td>接入体验-评分</td><td>接入体验-感受</td><td>普通办公-评分</td>"
				+ "<td>普通办公-感受</td><td>网页浏览-评分</td><td>网页浏览-感受</td>"
				+ "<td>主要用途</td><td>总体感受-评分</td><td>总体感受-感受</td>"
				+ "</tr>");
		for (int i=1; i<100; i++) {
			System.out.println("抓取第"+i+"页");
			String content = loadPageContent(searchUrl + i + ".html");
			List<String> result = pairMatch("<table", "</table>", true, content);
			for (String string : result) {
				StringBuilder tr = new StringBuilder();
				tr.append("<tr>");
				List<String> louceng = pairMatch("<em>", "</em>", true, string);
				louceng.stream().limit(1).forEach(e -> {tr.append("<td>"+getText(e)+"</td>");});
				
				List<String> neirong = pairMatch("<td class=\"t_f\"", "</td>", true, string);
				neirong.forEach(e -> {
					e = getText(e);
					String str = StringUtils.substringBetween(e, "1、测试环境", "2、接入网络条件：");
					if(str != null) {
						tr.append("<td>"+StringUtils.substringBetween(str, "花粉昵称：", "接入城市")+"</td>");
						tr.append("<td>"+StringUtils.substringBetween(str, "接入城市：", "接入终端")+"</td>");
						tr.append("<td>"+StringUtils.substringAfter(str, "接入终端：")+"</td>");
					}
					
					str = StringUtils.substringBetween(e, "2、接入网络条件：", "3、接入体验：");
					if(str != null) {
						tr.append("<td>"+StringUtils.substringBetween(str, "带宽：", "时延")+"</td>");
						tr.append("<td>"+StringUtils.substringAfter(str, "时延：")+"</td>");
					}
					
					str = StringUtils.substringBetween(e, "3、接入体验：", "4、普通办公：");
					if(str != null) {
						tr.append("<td>"+StringUtils.substringBetween(str, "评分：", "感受")+"</td>");
						tr.append("<td>"+StringUtils.substringAfter(str, "感受：")+"</td>");
					}
					
					str = StringUtils.substringBetween(e, "4、普通办公：", "5、网页浏览：");
					if(str != null) {
						tr.append("<td>"+StringUtils.substringBetween(str, "评分：", "感受")+"</td>");
						tr.append("<td>"+StringUtils.substringAfter(str, "感受：")+"</td>");
					}
					
					str = StringUtils.substringBetween(e, "5、网页浏览：", "总体感受");
					if(str != null) {
						if(str.contains("7、")) {
							tr.append("<td>"+StringUtils.substringBetween(str, "评分：", "感受")+"</td>");
							tr.append("<td>"+StringUtils.substringBetween(str, "感受：", "6、主要用途")+"</td>");
							tr.append("<td>"+StringUtils.substringBetween(str, "主要用途：", "7、")+"</td>");
						}else {
							tr.append("<td>"+StringUtils.substringBetween(str, "评分：", "感受")+"</td>");
							String s = StringUtils.substringAfter(str, "感受：");
							if(!StringUtils.isBlank(s)) {
								tr.append("<td>"+s.substring(0, s.length() -1 )+"</td>");
							}else {
								tr.append("<td></td>");
							}
							tr.append("<td></td>");
						}
					}
					
					str = StringUtils.substringAfter(e, "、总体感受");
					if(str != null) {
						tr.append("<td>"+StringUtils.substringBetween(str, "评分：", "感受")+"</td>");
						tr.append("<td>"+StringUtils.substringAfter(str, "感受：")+"</td>");
					}
				});
				tr.append("</tr>\n");
				excel.append(tr);
				System.out.println(tr);
			}
		}
		excel.append("</table>");
		try(PrintWriter pw = new PrintWriter("C:/xxx.xls")) {
			pw.println(excel);
		} 
	}
		
}
