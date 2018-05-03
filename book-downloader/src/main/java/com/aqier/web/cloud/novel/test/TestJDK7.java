/*
 * Domain Aqier.com Reserve Copyright
 * @author yuloang.wang@Aqier.com
 * @since 2018年5月2日
 */
package com.aqier.web.cloud.novel.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 
 * @author yulong.wang@aqier.com
 * @since 2018年5月2日
 */
public class TestJDK7 {
	
	public static void main(String[] args) {
		Map<String, Object> map = new HashMap<>();
		map.put(null, null);
		System.out.println(map);
		
		Set<String> set = new HashSet<>();
		set.add(null);
		System.out.println(set);
		
		System.out.println(Arrays.stream(new String[] {"fff", "ff"}).findAny());
		System.out.println(Arrays.stream(new String[] {"fff", "ff"}).findFirst());
		
		System.out.println(Arrays.stream(new String[] {"gg"}).isParallel());
		//System.out.println(Arrays.stream(new String[] {null}).findFirst());
	}
	
	/**
	 * @author yulong.wang@aqier.com
	 * @since 2018年5月2日
	 */
	public void testTryCatch() {
		try/*(FileInputStream fis = new FileInputStream(""))*/ {
			// fis.read(null);
			Map<String, Object> map = new HashMap<>();
			Object value = map.getOrDefault("ss", "default");
			if(map.containsKey("ss")) {
				value = map.get("ss");
			}else {
				value = "default";
			}
			Collections.sort(Arrays.asList(), (String a, String b) -> {
				return 0;
			});
			
			List<String> list = new ArrayList<>();
			
			list.add("d");
			list.add("v");
			
			list.forEach(list::add);
			
			byte[] bs = new byte[] {1,2,3};
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			File f = new File("C://dddd");
			// String name
			// File f
			// Stream
			// byte
			
			String ss = "";
			byte[] b = null;
			b = ss.getBytes();
			
			FileInputStream fis = new FileInputStream(f);
			FileOutputStream fos = new FileOutputStream(f);
			
			int n = -1;
			while((n = fis.read(bs)) != -1) {
				bos.write(bs, 0, n);
			}
			byte[] fileBytes = bos.toByteArray();
			
			ByteArrayInputStream bis = new ByteArrayInputStream(fileBytes);
			
			while((n = bis.read(bs)) != -1) {
				fos.write(bs, 0, n);
			}
			
			fis.read(bs);
			String fffs = Base64.getEncoder().encodeToString(bs); // 
			
			Stream<String> stream = Stream.of("good","good","stady", "day",  "day",  "up");
			
			list = stream.skip(1).filter(e->e.equals("good")).map(e->{return e.toUpperCase();}).distinct().collect(Collectors.toList());
			
			Set<String> set = list.stream().collect(Collectors.toSet());
		}catch(Exception e) {
			
		}
	}
	
	public int  a = 100_000;
	
	private List<String> list1 = new ArrayList<String>();
	
	private List<String> list2 = new ArrayList<>();
	
	
	public interface inter {
		
		
		
		default void test() {
			
		}
		
	}
}
