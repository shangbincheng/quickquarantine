package com.github.quickquarantine;

import java.io.File;


public class FactoryBuilder {
	
    private static String dirName = "quickquarantine";
	
	private static String fileName = "quickquarantine.properties";
	/** 
	 * 默认QuickQuarantine配置清单文件路径.
	 * 比如Linux下,根路径/quickquarantine/quickquarantine.properties 
	 */
	private static String path = String.format("%s%s%s", dirName, File.separator, fileName);
	
	/**
	 * 重新设置配置文件位置,从根路径起始
	 * @param dirName
	 * @param fileName
	 */
	public static void setPath(String dirName, String fileName) {
		FactoryBuilder.dirName = dirName;
		FactoryBuilder.fileName = fileName;
		FactoryBuilder.path = String.format("%s%s%s", dirName, File.separator, fileName);
	}
	
	public static String getDirName() {
		return FactoryBuilder.dirName;
	}
	
	public static String getFileName() {
		return FactoryBuilder.fileName;
	}
	
	public static String getPath() {
		return FactoryBuilder.path;
	}
}