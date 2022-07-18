package xyz.langu.attach.hook;

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

//ASM 是一个 Java 字节码操控框架。它能够以二进制形式修改已有类或者动态生成类。ASM 可以直接产生二进制 class 文件，也可以在类被加载入 Java 虚拟机之前动态改变类行为。ASM 从类文件中读入信息后，能够改变类行为，分析类信息，甚至能够根据用户要求生成新类。
//
//不过ASM在创建class字节码的过程中，操纵的级别是底层JVM的汇编指令级别，这要求ASM使用者要对class组织结构和JVM汇编指令有一定的了解。

//Javassist是一个开源的分析、编辑和创建Java字节码的类库。其主要的优点，在于简单，而且快速。直接使用java编码的形式，而不需要了解虚拟机指令，就能动态改变类的结构，或者动态生成类。

