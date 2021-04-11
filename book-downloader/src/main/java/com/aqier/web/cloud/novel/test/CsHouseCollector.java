/*
 * Domain Aqier.com Reserve Copyright
 * 
 * @author yulong.wang@Aqier.com
 * 
 * @since 2020-05-04 20:58:55
 */
package com.aqier.web.cloud.novel.test;

import java.io.PrintWriter;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

/**
 * @author yulong.wang@Aqier.com
 * @since 2019年10月25日
 */
public class CsHouseCollector {

    private static final String URL = "http://www.cszjxx.net/geth1";

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");

    public static void main(String[] args) throws Exception {
        writeHouseInfo2File();
    }

    /**
     * @author yulong.wang@Aqier.com
     * @throws Exception
     * @since 2019年10月26日
     */
    private static void writeHouseInfo2File() throws Exception {
        House[] houses = House.values();
        StringBuilder strs = new StringBuilder("<table>");
        strs.append(
            "<tr><td>户室号</td><td>楼层</td><td>房屋用途</td><td>房屋类型</td><td>装修状态</td><td>建筑面积</td><td>套内面积</td><td>分摊面积</td><td>销售状态</td><td>所属楼栋</td></tr>");
        for (House house : houses) {
            strs.append(getHoseInfo(house));
        }
        strs.append("</table>");
        try (PrintWriter out = new PrintWriter("D:/建发泱著_" + dateTimeFormatter.format(LocalDateTime.now()) + ".xls")) {
            FileCopyUtils.copy(strs.toString(), out);
        }
    }

    public static String getHoseInfo(House house) throws Exception {
        int pageNo = 1;
        StringBuilder strs = new StringBuilder();
        while (true) {
            String hoseInfo = getHoseInfo(house, pageNo++);
            if (!hoseInfo.contains("住宅")) {
                System.err.println(hoseInfo);
                break;
            }
            Thread.sleep(1000);
            System.out.println(trimHtml(hoseInfo));
            strs.append(hoseInfo);
        }
        return strs.toString();
    }

    private static String getHoseInfo(House house, int pageNo) throws Exception {
        String url = URL + "?ywzh=" + house.getCode() + "&n=" + pageNo;
        // 最多重试 5 次
        String html = retryable(url, new AtomicInteger(5));
        Pattern p = Pattern.compile("\\\\u[a-z0-9]{4}");
        Matcher m = p.matcher(html);
        while (m.find()) {
            String s = m.group();
            html = html.replace(s, unicode2String(s));
        }
        html = html.replaceAll("<tr id=(.+)点击加载更多(.+)/tr>", "").replace("\"", "").trim();
        html = html.replace("<\\/tr>", "<td>" + house.name() + "<\\/td><\\/tr>");
        return html;
    }

    /**
     * @param url 网址
     * @param retryNum 重试次数
     * @return
     * @author yulong.wang@Aqier.com
     * @throws InterruptedException
     * @since 2019年10月26日
     */
    private static String retryable(String url, AtomicInteger retryNum) throws Exception {
        String response = null;
        try {
            URI urlObj = new URI(url);
            HttpHeaders headers = new HttpHeaders();
            int indexa = url.indexOf("://");
            String origin = url.substring(0, url.indexOf("/", indexa + 3));
            headers.add(HttpHeaders.ORIGIN, origin);
            headers.add(HttpHeaders.HOST, urlObj.getHost());
            headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36");
            headers.add(HttpHeaders.REFERER, "http://www.cszjxx.net/floorinfo/201710110616");
            headers.add(HttpHeaders.COOKIE, "XSRF-TOKEN=eyJpdiI6IlhhNnhuR3E5WXUrMEh3M3BhbHpKdVE9PSIsInZhbHVlIjoiUjZcL1EwZmhnUHZ4WHhOXC9OT1pOYlptWlwvaGEzT3ZEb3RySndVSG9oaVpzOWVMNzRDczlpODFpUzhZYVpOSG1cL1pHVEd4Q1BoY1UwSTNIRHM3NWh6YmF3PT0iLCJtYWMiOiI2ZDZiMzdkYzVkZGM1NzhhZmU2Y2ZlMWVhYjQ4MTY1ZmQzZTlmMGViZWY1MWI5Y2Y3ODBjMTk3MWMyOTNlZjM4In0%3D; laravel_session=eyJpdiI6InVtbG45UU9uMDYwMVhQK1dabFRsRVE9PSIsInZhbHVlIjoiQldjcmVydGh2aFl2d2dYNUowWFpjOHJCVGRDVWlwYU13dTgwcWVubkVmVVcxeCs5U1pZMzVxYWhQVFh2QzBKVEF2VlB2ZU5xd3ZRMSs1Nm9jMnhkZGc9PSIsIm1hYyI6ImU2M2Y2OGM5YjEyYTliNzI5YjczZmQyMjMyYzhlNDQ2NzAzMGIzMmVjODM0ZjJlYjE1NTFlN2EzYzQyYzQxMTgifQ%3D%3D");
            headers.add("X-Requested-With", "XMLHttpRequest");
            headers.add("Accept", "application/json, text/javascript, */*; q=0.01");
            headers.add("Accept-Encoding", "gzip, deflate");
            headers.add("Accept-Language", "zh-CN,zh;q=0.9");
            headers.add("Connection", "keep-alive");
            headers.add("Content-Length", "21");
            headers.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            RequestEntity<?> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, urlObj);
            response = REST_TEMPLATE.exchange(requestEntity, String.class).getBody();
            return response;
            
        } catch (RuntimeException e) {
            System.err.println("request url [ " + url + " ] error:" + e.getMessage());
            if (retryNum.decrementAndGet() < 0) {
                throw e;
            } else {
                Thread.sleep(1000);
                return retryable(url, retryNum);
            }
        }
    }

    private static String trimHtml(String html) {
        html = html.replace("<td>", "");
        html = html.replace("<\\/td>", "\t");
        html = html.replace("<tr>", "");
        html = html.replace("<\\/tr>", "\r\n");
        return html;
    }

    /**
     * @param unicode
     * @return
     * @author yulong.wang@Aqier.com
     * @since 2019年10月26日
     */
    private static String unicode2String(String unicode) {
        return String.valueOf((char) Integer.parseInt(unicode.substring(2), 16));
    }

    enum House {

        建发泱著1栋("KF1807120767"),

        建发泱著2栋("KF1807120811"),

        建发泱著3栋("KF1807120813"),

        建发泱著5栋("KF1807120823"),

        建发泱著6栋("KF1809180687"),

        建发泱著8栋("KF1809180695"),

        建发泱著10栋("KF1809290827"),

        建发泱著11栋("KF1809291139");

        private String code;

        House(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
