package xyz.langu;

import xyz.langu.hook.ProcessBuilderHook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

//javaagent是java命令提供的一个参数，这个参数可以指定一个jar包，在真正的程序没有运行之前先运行指定的jar包。并且对jar包有两个要求：
//jar包的MANIFEST.MF文件必须指定Premain-Class
//Premain-Class指定的类必须实现premain()方法。
//premain方法会在java命令行指定的main函数之前运行。
//-javaagent:<jarpath>[=<选项>] 加载 Java 编程语言代理

public class PreMain {
    public static void premain(String agentArgs, Instrumentation inst) throws IOException, UnmodifiableClassException {
        /*System.out.println("++++++++++++++PreAgent Start++++++++++++++++/n");
        ProcessBuilder processBuilder = new ProcessBuilder();
        //ProcessBuilder用来创建一个操作系统进程。
        processBuilder.command("ping", "www.baidu.com");
        Process process = processBuilder.start();
        //start()方法开启进程会调用command命令列表和相关参数，这个函数会检测command的正确性以及做系统安全性检查
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
        //BufferedReader缓冲区读取内容，避免中文乱码
        System.out.println(bufferedReader.readLine());*/

        ProcessBuilderHook processBuilderHook = new ProcessBuilderHook(inst);
        inst.addTransformer(processBuilderHook, true);

        //获取所有jvm中加载过的类
        Class[] allLoadedClasses = inst.getAllLoadedClasses();
        for (Class aClass : allLoadedClasses){
            System.out.println(aClass.toString());
            //if (inst.isModifiableClass(aClass) && !aClass.getName().startsWith("java.lang.invoke.LambdaForm")){
            //if (inst.isModifiableClass(aClass)){
            if ("java.lang.ProcessBuilder".equals(aClass.getName())) {
                inst.retransformClasses(new Class[]{aClass});
            }
        }
        System.out.println("++++++++++++++++++++++++++++hook finished+++++++++++++++++++\n");
    }
}
