package xyz.langu;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;


//javaagent提供了两种模式：
//        premain：允许在main开始前修改字节码，也就是在大部分类加载前对字节码进行修改。
//        agentmain：允许在main执行后通过com.sun.tools.attach的Attach API attach到程序运行时中，通过retransform的方式修改字节码，也就是在类加载后通过类重新转换（定义）的方式在方法体中对字节码进行修改，其本质还是在类加载前对字节码进行修改。
//                  通过 Java Tool API 中的 attach 方式，我们可以很方便地在运行过程中动态地设置加载代理类，以达到 instrumentation 的目的。

public class PreMain {
    public static void premain(String agentArgs, Instrumentation inst){
        //JDK™5.0中引入包java.lang.instrument。 该包提供了一个Java编程API，可以用来开发增强Java应用程序的工具，例如监视它们或收集性能信息。 使用 Instrumentation，开发者可以构建一个独立于应用程序的代理程序（Agent），用来监测和协助运行在 JVM 上的程序，甚至能够替换和修改某些类的定义。
        //Java Instrument 工作原理
        //在 JVM 启动时，通过 JVM 参数 -javaagent，传入 agent jar，Instrument Agent 被加载；
        //在 Instrument Agent 初始化时，注册了 JVMTI 初始化函数 eventHandlerVMinit；（JVMTI（JVM Tool Interface）是 Java 虚拟机所提供的 native 编程接口）
        //在 JVM 启动时，会调用初始化函数 eventHandlerVMinit，启动了 Instrument Agent，用 sun.instrument.instrumentationImpl 类里的方法 loadClassAndCallPremain 方法去初始化 Premain-Class 指定类的 premain 方法；
        //初始化函数 eventHandlerVMinit，注册了 class 解析的 ClassFileLoadHook 函数；
        //在解析 Class 之前，JVM 调用 JVMTI 的 ClassFileLoadHook 函数，钩子函数调用 sun.instrument.instrumentationImpl 类里的 transform 方法，通过 TransformerManager 的 transformer 方法最终调用我们自定义的 Transformer 类的 transform 方法；
        //因为字节码在解析 Class 之前改的，直接使用修改后的字节码的数据流替代，最后进入 Class 解析，对整个 Class 解析无影响；
        System.out.println("agentArgs:" + agentArgs);
        inst.addTransformer(new DefineTransformer(), true);
        //addTransformer(ClassFileTransformer transformer, boolean canRetransform);功能是注册一个 ClassFileTransformer 类的实例，在类加载的时候被调用。
    }

    static class DefineTransformer implements ClassFileTransformer{
        @Override
        //ClassFileTransformer 当中的 transform 方法可以对类定义进行操作修改；
        //在类字节码载入 JVM 前，JVM 会调用 ClassFileTransformer.transform 方法，从而实现对类定义进行操作修改，实现 AOP 功能；相对于 JDK  动态代理、CGLIB 等 AOP 实现技术，不会生成新类，也不需要原类有接口；
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            System.out.println("premain load class:" + className);
            return classfileBuffer;
        }
    }
}


//Premain-Class	指定代理类
//Agent-Class	指定代理类
//Boot-Class-Path	指定bootstrap类加载器的搜索路径，在平台指定的查找路径失败的时候生效， 可选
//Can-Redefine-Classes	是否需要重新定义所有类，默认为false，可选。
//Can-Retransform-Classes	是否需要retransform，默认为false,可选
