package com.zzzi.springframework.util;
/**@author zzzi
 * @date 2023/11/1 14:34
 * 在这里实现一个工具类
 */
public class ClassUtils {

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        }
        catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back to system class loader...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClassUtils.class.getClassLoader();
        }
        return cl;
    }
    /**@author zzzi
     * @date 2023/11/8 13:39
     * 判断当前类是不是Cglib生成的类
     */
    public static boolean isCglibProxyClass(Class<?> clazz) {
        return (clazz != null && isCglibProxyClassName(clazz.getName()));
    }

    /**@author zzzi
     * @date 2023/11/8 13:40
     * 判断当前类名是不是由Cglib生成的类
     * 也就是看当前类名带不带"$$"
     */
    public static boolean isCglibProxyClassName(String className) {
        return (className != null && className.contains("$$"));
    }

}
