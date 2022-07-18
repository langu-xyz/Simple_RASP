package xyz.langu;

import xyz.langu.attach.hook.ProcessBuilderHook;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class AgentMain {
    //jdk1.6之后在Instrumentation中添加了一种agentmain的代理方法，可以在main函数执行之后再运行。和premain函数一样，开发者可以编写一个包含agentmain函数的Java类，它也有两种写法：
    //public static void agentmain (String agentArgs, Instrumentation inst)
    //public static void agentmain (String agentArgs)
    //带有Instrumentation的方法会被优先调用，开发者必须再MANIFEST.MF文件中设置Agent-Class来指定包含agentmain函数的类。
    //由于 JVM类加载机制的限制，同一个 jar 包无法被 AppClassLoader对象加载第二次。
    public static void agentmain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException {
        System.out.println("agentmain start");
        System.out.println(inst.toString());
        ProcessBuilderHook processBuilderHook = new ProcessBuilderHook(inst);
        inst.addTransformer(processBuilderHook, true);

        //CustomClassTransformer transformer = new CustomClassTransformer(inst);
        //transformer.retransform();

        //获取所有jvm中加载过的类
        Class[] allLoadedClasses = inst.getAllLoadedClasses();
        for (Class aClass : allLoadedClasses){
            if (inst.isModifiableClass(aClass) && !aClass.getName().startsWith("java.lang.invoke.LambdaForm")){
                //确定一个类是否可以被 retransformation 或 redefinition 修改。
                inst.retransformClasses(new Class[]{aClass});
                //retransformClasses（）会让类重新加载，从而使得注册的类修改器能够重新修改类的字节码。
            }
        }
        System.out.println("++++++++++++++++++++++++++++hook finished+++++++++++++++++++\n");
    }
}
