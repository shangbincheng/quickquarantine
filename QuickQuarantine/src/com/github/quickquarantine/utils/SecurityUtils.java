package com.github.quickquarantine.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 安全工具类
 * @author shangbinchenbg001
 * @date 2018-03-10
 */
public class SecurityUtils {
	/**
	 * 匹配类的全限命名例如"com.sunyard.tzbank.commons.AA.testBB"中的".testBB"的正则表达式
	 */
	public static final String RGX_CLASSNAME = "(\\.[a-zA-Z_][0-9a-zA-Z_]*)$";
	/**
	 * 匹配类的全限命名例如"com.sunyard.tzbank.commons.AA.testBB"中的".testBB"的匹配器
	 */
	public static final Pattern PTN_CLASSNAME = Pattern.compile(RGX_CLASSNAME);
	/**
	 * 匹配类的忽略形参列表方法名例如"com.sunyard.tzbank.commons.AA.testBB"中的"testBB"的正则表达式
	 */
	public static final String RGX_METHODNAME = "([a-zA-Z_][0-9a-zA-Z_]*)$";
	/**
	 * 匹配类的忽略形参列表方法名例如"com.sunyard.tzbank.commons.AA.testBB"中的"testBB"的匹配器
	 */
	public static final Pattern PTN_METHODNAME = Pattern.compile(RGX_METHODNAME);
	/**
	 * 匹配表示任意字符,包含转义字符例如"(xxx)"的正则表达式
	 */
	public static final String RGX_RD_LETTER = "\\([\\s\\S]*?\\)";
	/**
	 * 匹配例如"{33}","{99}","{1}"的正则表达式
	 */
	public static final String RGX_SALT = "\\{([0-9]|[0-9]{2})\\}";
	/**
	 * 匹配左大括号或右大括号的正则表达式
	 */
	public static final String RGX_BRACE = "\\{|\\}";
	/**
	 * 匹配表示任意字符,包含转义字符例如"(xxx)"的匹配器
	 */
	public static final Pattern PTN_RD_LETTER = Pattern.compile(RGX_RD_LETTER);
	/**
	 * 匹配类的全限命名和忽略形参列表方法名例如"com.sunyard.tzbank.commons.AA.testBB"的正则表达式
	 */
	public static final String RGX_CLASSNAME_METHOD = "^([a-zA-Z_][0-9a-zA-Z_]*)(\\.[a-zA-Z_][0-9a-zA-Z_]*)*(\\.[A-Z][0-9a-zA-Z_]*\\.[a-zA-Z_][0-9a-zA-Z_]*)$";
	/**
	 * 匹配99位随机盐的匹配器
	 */
	public static final Pattern PTN_SALT = Pattern.compile(RGX_SALT);
	/**
	 * 大括号匹配器
	 */
	public static final Pattern PTN_BRACE = Pattern.compile(RGX_BRACE);
	/**
	 * 逗号匹配器
	 */
	public static final Pattern PTN_COMMA = Pattern.compile(",");
	/**
	 * 竖线
	 */
	public static final String VERTICAL_LINE = "\\|";
	/**
	 * 竖线匹配器
	 */
	public static final Pattern PTN_VERTICAL_LINE = Pattern.compile("\\|");
	/**
	 * 匹配类的全限命名和忽略形参列表方法名例如"com.sunyard.tzbank.commons.AA.testBB"的匹配器
	 */
	public static final Pattern PTN_CLASSNAME_METHODNAME = Pattern.compile(RGX_CLASSNAME_METHOD);
	/**
	 * 0-9范围
	 */
	private static final int RANGE_ARABIC_NUM = 0;
	/**
	 * A-Z范围
	 */
	private static final int RANGE_UP_LETTER = 1;
	/**
	 * a-z范围
	 */
	private static final int RANGE_LOW_LETTER = 2;
	/**
	 * 根据正则表达式生成随机盐
	 * @author shangbinchenbg001
	 * @param regex 指定随机盐的正则表达式 "[0-9A-Za-z]{4}", "[0-9]{6}","[A-Za-z]{7}"...
	 * @return 随机盐
	 */
	public static String getSalt(String regex) {
		Matcher salt_m = PTN_SALT.matcher(regex);
		salt_m.find();
		String s_digit = salt_m.group();
		Matcher brace_m = PTN_BRACE.matcher(s_digit);
		s_digit = brace_m.replaceAll("");
		int i_digit = Integer.parseInt(s_digit);
		List<Integer> rangeList = new ArrayList<Integer>();
		if (regex.contains("0-9")) {
			rangeList.add(RANGE_ARABIC_NUM);
		}
		if (regex.contains("A-Z")) {
			rangeList.add(RANGE_UP_LETTER);
		}
		if (regex.contains("a-z")) {
			rangeList.add(RANGE_LOW_LETTER);
		}
		int range = rangeList.size();
		
		Random rd = new Random();
		StringBuilder salt = new StringBuilder("");
		for (int i = 0;i < i_digit; i++) {
			int letter = getLetter(rd, rangeList.get(rd.nextInt(range)));
			if (letter > 9) {
				salt.append((char)letter);
			} else {
				salt.append(letter);
			}
		}
		return salt.toString();
	}
	/**
	 * 随机获取一个阿拉伯数字和英文字母的ASCII码
	 * @author shangbinchenbg001
	 * @param rd 
	 * @param s 
	 * @return
	 */
	private static int getLetter(Random rd, int s) {
		if (s == RANGE_ARABIC_NUM) {
			return rd.nextInt(9);
		} else if (s == RANGE_UP_LETTER) {
			return rd.nextInt(90) % 26 + 65;
		} else if (s == RANGE_LOW_LETTER) {
			return rd.nextInt(122) % 26 + 97;
		} else {
			throw new RuntimeException("从未知的范围获取字母!");
		}
	}
	/**
	 * 根据原字符串/正则表达式获取目标中匹配的的字符串,英文字母大小写不敏感
	 * @param s 原字符串/正则表达式
	 * @param t 目标字符串
	 * @return null 找不到
	 */
	public static String getFirstStrIgnoreCase(String s, String t) {
		return contains(s, t, true);
	}
	/** 根据原字符串/正则表达式获取目标中匹配的的字符串
	 * @param s 原字符串/正则表达式
	 * @param t 目标字符串
	 * @return null 找不到
	 */
	public static String getFirstStr(String s, String t) {
		return contains(s, t, false);
	}
	/**
	 * 判断目标字符串中是否包含原字符串
	 * @param s 原字符串
	 * @param t 目标字符串
	 * @return
	 */
	public static boolean contains(String s, String t) {
		if (contains(s, t, false) == null) {
			return false;
		} else {
			return true;
		}
	}
	/**
	 * 判断目标字符串中是否包含原字符串,英文字母大小写不敏感
	 * @param s 原字符串
	 * @param t 目标字符串
	 * @return
	 */
	public static boolean containsIgnoreCase(String s, String t) {
		if (contains(s, t, true) == null) {
			return false;
		} else {
			return true;
		}
	}
	/**
	 * 判断目标字符串中是否包含原字符串
	 * @param s 原字符串
	 * @param t 目标字符串
	 * @param i 是否忽略大小写
	 * @return
	 */
	private static String contains(String s, String t, boolean i) {
		Pattern p = null;
		if (i) {
			p = Pattern.compile(s, Pattern.CASE_INSENSITIVE);
		} else {
			p = Pattern.compile(s);
		}
		Matcher m = p.matcher(t);
		if (m.find()) {
			return m.group();
		} else {
			return null;
		}
	}
	
	/**
	 * 从目标字符串中取出指定模式的指定字符串
	 * @param p 指定模式
	 * @param t 目标字符串
	 * @return 指定字符串 返回null,代表获取失败
	 */
	public static String getFirstStr(Pattern p, String t) {
		Matcher m = p.matcher(t);
		if (m.find()) {
			return m.group();
		} else {
			return null;
		}
	}
	
	/**
	 * 安全使用旧对象调用指定对象方法
	 * @author shangbinchenbg001
	 * @param o 继续使用旧对象
	 * @param className 指定类的全限命名
	 * @param methodName 指定调用类的方法名
	 * @param paramTypes 方法的参数类型列表
	 * @param params 方法的参数列表
	 * @param returnType 期望方法返回值的类型 String.class,Object.class,null:无返回值,HashMap.class
	 * @return 返回值 null：代表被调用方法无返回值
	 * @throws Exception 
	 */
	public static Object echoMethodByObject(Object o, String className, String methodName, Class<?>[] paramTypes, Object[] params, Class<?> returnType) throws Exception {
		return echoMethodByClass(o, className, methodName, false, paramTypes, params, returnType);
	}
	/**
	 * 安全创建指定类下的新对象,再调用指定对象方法
	 * @author shangbinchenbg001
	 * @param className 指定类的全限命名
	 * @param methodName 指定调用类的方法名
	 * @param paramTypes 方法的参数类型列表
	 * @param params 方法的参数列表
	 * @param returnType 期望方法返回值的类型 String.class,Object.class,null:无返回值,HashMap.class
	 * @return 返回值 null：代表被调用方法无返回值
	 * @throws Exception 
	 */
	public static Object echoObjectMethodByClass(String className, String methodName, Class<?>[] paramTypes, Object[] params, Class<?> returnType) throws Exception {
		return echoMethodByClass(null, className, methodName, false, paramTypes, params, returnType);
	}
	/**
	 * 安全调用指定类下的静态方法
	 * @author shangbinchenbg001
	 * @param className 指定类的全限命名
	 * @param methodName 指定调用类的方法名
	 * @param paramTypes 方法的参数类型列表
	 * @param params 方法的参数列表
	 * @param returnType 期望方法返回值的类型 String.class,Object.class,null:无返回值,HashMap.class
	 * @return 返回值 null：代表被调用方法无返回值
	 * @throws Exception 
	 */
	public static Object echoStaticMethodByClass(String className, String methodName, Class<?>[] paramTypes, Object[] params, Class<?> returnType) throws Exception {
		return echoMethodByClass(null, className, methodName, true, paramTypes, params, returnType);
	}
	/**
	 * 安全调用指定类下的指定方法,如果是对象方法,则会创建新对象
	 * @author shangbinchenbg001
	 * @param className 指定类的全限命名
	 * @param methodName 指定调用类的方法名
	 * @param isStatic 期望调用方法类型  true:指定方法是个类方法,false:指定方法是对象方法,null:不指定方法类型
	 * @param paramTypes 方法的参数类型列表
	 * @param params 方法的参数列表
	 * @param returnType 期望方法返回值的类型 String.class,Object.class,null:无返回值,HashMap.class
	 * @return 返回值 null：代表被调用方法无返回值
	 * @throws Exception 
	 */
	public static Object echoMethodByClass(String className, String methodName, Boolean isStatic, Class<?>[] paramTypes, Object[] params, Class<?> returnType) throws Exception {
		return echoMethodByClass(null, className, methodName, isStatic, paramTypes, params, returnType);
	}
	/**
	 * 安全调用指定类下的指定方法,可使用旧对象
	 * @author shangbinchenbg001
	 * @param old 继续使用旧对象
	 * @param className 指定类的全限命名
	 * @param methodName 指定调用类的方法名
	 * @param isStatic 期望调用方法类型  true:指定方法是个类方法,false:指定方法是对象方法,null:不指定方法类型
	 * @param paramTypes 方法的参数类型列表
	 * @param params 方法的参数列表
	 * @param returnType 期望方法返回值的类型 String.class,Object.class,null:无返回值,HashMap.class
	 * @return 返回值 null：代表被调用方法无返回值
	 * @throws Exception 
	 */
    public static Object echoMethodByClass(Object old, String className, String methodName, Boolean isStatic, Class<?>[] paramTypes, Object[] params, Class<?> returnType) throws Exception {
		Class<?> c = Class.forName(className);
		
		boolean have_method = false;
		for (Method m: c.getDeclaredMethods()) {
			if (methodName.equals(m.getName())) {
				have_method = true;
				break;
			};
		};
		if (have_method == false) {
			throw new RuntimeException(String.format("当前%s类,不存在%s方法!", className, methodName));
		}
		
		Method method = c.getDeclaredMethod(methodName, paramTypes);
		if (isStatic == null) {
			isStatic = Modifier.isStatic(method.getModifiers());
		} else if (isStatic == true) {
			isStatic = Modifier.isStatic(method.getModifiers());
			if (!isStatic) {
				throw new RuntimeException(String.format("当前%s类的%s方法实际是对象的方法,不是期望的类的方法!", className, methodName));
			};
		} else {
			isStatic = Modifier.isStatic(method.getModifiers());
			if (isStatic) {
				throw new RuntimeException(String.format("当前%s类的%s方法实际是类的方法,不是期望的对象方法!", className, methodName));
			};
		}
		Object ro = null;
		Object oc = null;
		method.setAccessible(true);
		int length = paramTypes.length;
		if (isStatic) {
			if (old == null) {
				oc = c;
			} else {
				oc = old;
			}
			if (length == 0) {
				ro = method.invoke(oc);
			} else {
				ro = method.invoke(oc, params);
			}
		} else {
			if (old == null) {
				oc = c.newInstance();
			} else {
				oc = old;
			}
			if (length == 0) {
				ro = method.invoke(oc);
			} else {
				ro = method.invoke(oc, params);
			}
		}
		if (returnType == null && ro != null) {
				throw new RuntimeException(String.format("当前%s类的%s方法实际是有返回值的方法,不是期望的返回null的方法!", className, methodName));
		}
		if (returnType != null && ro == null) {
			throw new RuntimeException(String.format("当前%s类的%s方法实际是返回null的方法,不是期望的有返回值的方法!", className, methodName));
		}
		if (returnType != null && ro != null) {
			String expectName = returnType.getName();
			String realName = ro.getClass().getName();
			if (!expectName.equals(realName)) {
				throw new RuntimeException(String.format("当前%s类的%s方法返回值实际是[ %s ]类型,不是期望的[ %s ]类型!", new Object[]{className, methodName, realName, expectName}));
			}
		}
		return ro;
	}
    /**
     * 安全创建对象
     * @param className
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    public static Object getObject(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    	return Class.forName(className).newInstance();
    }
    /**
     * 安全访问指定对象下的指定属性
     * @author shangbinchenbg001
     * @param o
     * @param isStatic 期望获取何种属性  true:指定属性是个类属性,false:指定属性是对象属性
     * @param fieldName
     * @return
     */
    public static Object getFieldValue(Object o, boolean isStatic, String fieldName) {
    	Class<?> c = null;
    	if (!isStatic) {
    		c = o.getClass();
    	} else {
    		c = (Class<?>) o;
    	}
		Field field;
		Object fieldValue = null;
		try {
			field = c.getDeclaredField(fieldName);
			field.setAccessible(true);
			fieldValue = field.get(o);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	    if (fieldValue == null) {
	    	throw new RuntimeException(String.format("Failed to get fieldValue [ %s ] from [ %s ].", fieldName, o.getClass().getName()));
        }
		return fieldValue;
	}
    /**
     * 安全修改指定对象下的指定属性
     * @author shangbinchenbg001
     * @param o
     * @param isStatic 期望修改何种属性  true:指定属性是个类属性,false:指定属性是对象属性
     * @param fieldName
     * @param fieldValue
     */
    public static void setFieldValue(Object o, boolean isStatic, String fieldName, Object fieldValue) {
    	Class<?> c = null;
    	if (!isStatic) {
    		c = o.getClass();
    	} else {
    		c = (Class<?>) o;
    	}
		try {
			Field field = c.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(o, fieldValue);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Failed to set %s [ %s ] from [ %s ].", new Object[]{fieldValue, fieldName, o.getClass().getName()}));	
		} 
	}
}