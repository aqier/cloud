/*
 * Domain Aqier.com Reserve Copyright
 * 
 * @author yulong.wang@Aqier.com
 * 
 * @since 2020-10-17 19:38:29
 */
package com.aqier.web.cloud.novel.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aqier.web.cloud.core.dto.MappingPage;
import com.aqier.web.cloud.core.dto.Page;
import com.aqier.web.cloud.core.dto.TableMapping;
import com.aqier.web.cloud.core.utils.database.SQLExecutor;

/**
 * @author yulong.wang@Aqier.com
 * @since 2020-10-17 19:38:29
 */
@RestController
@RequestMapping("/a3e7246150d64609b2e9fc7c0f3591c9/sql")
public class SqlConsoleController {

    @Autowired
    private DataSource dataSource;

    @PostMapping("/execute")
    public Page<Map<String, String>> exeuctor(String sql, @RequestBody Page<Map<String, String>> page)
        throws SQLException {
        if (page.getPageSize() > 100) {
            page.setPageSize(100);
        }
        if (StringUtils.isBlank(sql)) {
            page.setRows(Collections.emptyList());
            return page;
        }
        sql = sql.trim();
        try (Connection con = dataSource.getConnection()) {
            List<Map<String, String>> result;
            if (sql.toLowerCase().startsWith("select")) {
                MappingPage<Map<String, String>> mappingPage = new MappingPage<>();
                mappingPage.setMapping(TableMapping.upperCaseNoNullStringMapping);
                result = SQLExecutor.execute(con, sql, page);
            } else {
                int updateRows = con.createStatement().executeUpdate(sql);
                result = Collections.singletonList(Collections.singletonMap("UPDATE_ROWS", String.valueOf(updateRows)));
            }
            Page<Map<String, String>> resultPage = new Page<Map<String, String>>();
            resultPage.setTotal(page.getTotal());
            resultPage.setPageNo(page.getPageNo());
            resultPage.setPageSize(page.getPageSize());
            resultPage.setRows(result);
            return resultPage;
        }
    }
}
