# Simple_RASP
RASP的DEMO实现，通过该项目可以非常容易的理解PreMain和AgentMain两种注入方式。

之前学习RASP时候的代码，部分代码参考自网络，现在分享出来，原理就不赘述了，代码中注释写的非常清楚。

**目录**

<img width="340" alt="image" src="https://user-images.githubusercontent.com/12745454/179512802-527e65eb-18c6-4073-b869-e6a559ca520b.png">


**部分代码**

AgentMain

```
public class ProcessBuilderHook implements ClassFileTransformer {
    private Instrumentation instrumentation;
    private ClassPool classPool;
    public ProcessBuilderHook(Instrumentation instrumentation){
        this.instrumentation = instrumentation;
        this.classPool = new ClassPool(true);
    }


    //@Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.equals("java/lang/ProcessBuilder")){
            CtClass ctClass = null;
            //在 Javassist 中，类 Javaassit.CtClass 表示 class 文件。一个 GtClass (编译时类）对象可以处理一个 class 文件
            try {
                ctClass = this.classPool.get("java.lang.ProcessBuilder");
                //ClassPool是 CtClass 对象的容器。它按需读取类文件来构造 CtClass 对象，并且保存 CtClass 对象以便以后使用。
                CtMethod[] methods = ctClass.getMethods();
                //CtMthod代表类中的某个方法，可以通过CtClass提供的API获取或者CtNewMethod新建，通过CtMethod对象可以实现对方法的修改。
                String src = "if ($0.command.get(0).equals(\"ping\"))" +
                        "{System.out.println(\"detect ping command Warning\");" +
                        "System.out.println();" +
                        "return null;}";
                //javassist中写入函数体中含有范型时时：
                //1.对于范型符号需要特殊处理
                //2.对饮用的外部类显式声明包路径

                //$0代表this，这里this = 用户创建的ProcessBuilder实例对象
                for (CtMethod method : methods){
                    System.out.println(method.getName());
                    if (method.getName().equals("start")){
                        System.out.println("++++++start insert+++++++=");
                        method.insertBefore(src);
                        break;
                    }
                }
                classfileBuffer = ctClass.toBytecode();
            }
            catch (NotFoundException | CannotCompileException | IOException e) {
                e.printStackTrace();
            }
            finally {
                if(ctClass != null){
                    ctClass.detach();
                    //当 CtClass 数量过多时，会占用大量的内存，API中给出的解决方案是 有意识的调用CtClass的detach()方法以释放内存。
                }
            }
        }
        return classfileBuffer;
    }
}
```
```
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
```


PreMain

```
public class ProcessBuilderHook implements ClassFileTransformer {
    private Instrumentation instrumentation;
    private ClassPool classPool;
    public ProcessBuilderHook(Instrumentation instrumentation){
        this.instrumentation = instrumentation;
        this.classPool = new ClassPool(true);
    }


    //@Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.equals("java/lang/ProcessBuilder")){
            CtClass ctClass = null;
            //在 Javassist 中，类 Javaassit.CtClass 表示 class 文件。一个 GtClass (编译时类）对象可以处理一个 class 文件
            try {
                ctClass = this.classPool.get("java.lang.ProcessBuilder");
                //ClassPool是 CtClass 对象的容器。它按需读取类文件来构造 CtClass 对象，并且保存 CtClass 对象以便以后使用。
                CtMethod[] methods = ctClass.getMethods();
                //CtMthod代表类中的某个方法，可以通过CtClass提供的API获取或者CtNewMethod新建，通过CtMethod对象可以实现对方法的修改。
                String src = "if ($0.command.get(0).equals(\"ping\"))" +
                        "{System.out.println(\"detect ping command Warning\");" +
                        "System.out.println();" +
                        "return null;}";
                //javassist中写入函数体中含有范型时时：
                //1.对于范型符号需要特殊处理
                //2.对饮用的外部类显式声明包路径

                //$0代表this，这里this = 用户创建的ProcessBuilder实例对象
                for (CtMethod method : methods){
                    System.out.println(method.getName());
                    if (method.getName().equals("start")){
                        System.out.println("++++++start insert+++++++=");
                        method.insertBefore(src);
                        break;
                    }
                }
                classfileBuffer = ctClass.toBytecode();
            }
            catch (NotFoundException | CannotCompileException | IOException e) {
                e.printStackTrace();
            }
            finally {
                if(ctClass != null){
                    ctClass.detach();
                    //当 CtClass 数量过多时，会占用大量的内存，API中给出的解决方案是 有意识的调用CtClass的detach()方法以释放内存。
                }
            }
        }
        return classfileBuffer;
    }
}
```

```
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
```

