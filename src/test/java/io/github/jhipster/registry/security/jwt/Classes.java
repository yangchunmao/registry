package io.github.jhipster.registry.security.jwt;

import java.io.InputStream;

import org.mockito.internal.creation.instance.InstantiationException;
import org.omg.CORBA.portable.UnknownException;

import io.jsonwebtoken.lang.UnknownClassException;

public class Classes {


    private static final ClassLoaderAccessor THREAD_CL_ACCESSOR = new ExceptionIgnoringAccessor(){
    
            @Override
            protected ClassLoader doGetClassLoader() throws Throwable {
                return Thread.currentThread().getClass().getClassLoader();
            }
    };

    private static final ClassLoaderAccessor CLASS_CL_ACCESSOR = new ExceptionIgnoringAccessor(){
    
            @Override
            protected ClassLoader doGetClassLoader() throws Throwable {
                return Classes.class.getClassLoader();
            }
    };

    private static final ClassLoaderAccessor SYSTEM_CL_ACCESSOR = new ExceptionIgnoringAccessor(){
    
            @Override
            protected ClassLoader doGetClassLoader() throws Throwable {
                return ClassLoader.getSystemClassLoader();
            }
    };
    /**
     * 通过字符串类名， 加载类
     * @param fqzn
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> forname(String fqcn){

        // 如何加載？ 类加载机制
        // 在当前环境下找到当前类的文件位置
        Class clazz = THREAD_CL_ACCESSOR.loadClazz(fqcn);
        if(null == clazz){
            clazz = CLASS_CL_ACCESSOR.loadClazz(fqcn);
        }
        if(null == clazz){
            clazz = SYSTEM_CL_ACCESSOR.loadClazz(fqcn);
        }

        if (clazz == null) {
            String msg = "Unable to load class named [" + fqcn + "] from the thread context, current, or " +
                    "system/application ClassLoaders.  All heuristics have been exhausted.  Class could not be found.";

            if (fqcn != null && fqcn.startsWith("com.stormpath.sdk.impl")) {
                msg += "  Have you remembered to include the stormpath-sdk-impl .jar in your runtime classpath?";
            }

            throw new UnknownClassException(msg);
        }

        return clazz;
    } 


    /**
     * 通过字符串类名，生成类的实例
     * @param fqcn
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String fqcn){
        return (T)newInstance(forname(fqcn));
    }


    /**
     * 通过类，生成类的实例
     * @param clazz
     * @return
     */
    public static <T> T newInstance(Class<T> clazz){
        if(clazz == null){
            String msg = "Class message parameter cannot be null";
            throw new IllegalArgumentException(msg);
        }

        try{
            return clazz.newInstance();
        }catch(Exception e){
            throw new InstantiationException("Unable to instantiate class [" + clazz.getName() + "]", e);
        }
        
    }


    /**
     * 类加载接口
     * 内部静态接口方式
     */
    private static interface ClassLoaderAccessor {
    
        Class loadClazz(String fqcn);

        InputStream getResourceAsStream(String name);
    }

    /**
     * 忽略异常的实现
     */
    private static abstract class ExceptionIgnoringAccessor implements ClassLoaderAccessor {

        @Override
        public Class loadClazz(String fqcn) {

            Class clazz = null;
            ClassLoader cl = getClassLoader();
            if(cl != null){
                try {
                    clazz =  cl.loadClass(fqcn);
                } catch (ClassNotFoundException e) {
                    //class not found 
                }
            }
            return clazz;
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            InputStream in = null;
            ClassLoader cl = getClassLoader();
            if(cl != null){
                in = cl.getResourceAsStream(name);
            }
            return in;
        }

        protected final ClassLoader getClassLoader(){
            try {
                return doGetClassLoader();
            } catch (Throwable e) {
                //不能获取 ClassLoader
            }
            return null;
        }

        protected  abstract ClassLoader doGetClassLoader() throws Throwable;
    }
}