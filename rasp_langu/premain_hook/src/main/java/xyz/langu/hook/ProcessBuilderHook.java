package xyz.langu.hook;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import javassist.*;
//Javaassist 就是一个用来 处理 Java 字节码的类库。它可以在一个已经编译好的类中添加新的方法，或者是修改已有的方法，并且不需要对字节码方面有深入的了解。同时也可以去生成一个新的类对象，通过完全手动的方式。

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
