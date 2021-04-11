/*
 * Domain Aqier.com Reserve Copyright
 * @author yulong.wang@Aqier.com
 * @since 2021年4月8日
 */
package com.aqier.web.cloud.novel.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author yulong.wang@Aqier.com
 * @since 2021年4月8日
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile multipartFile) throws IllegalStateException, IOException {
        String fileName = multipartFile.getOriginalFilename();
        multipartFile.transferTo(new File("D:/Downloads/"+fileName));
        return fileName;
    }
}
