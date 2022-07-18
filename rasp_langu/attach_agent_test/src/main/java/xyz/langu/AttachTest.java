package xyz.langu;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;

public class AttachTest {

    public static void main(String[] args) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException{
        VirtualMachine attach = VirtualMachine.attach("28554"); //运行中的jvm的进程号
        System.out.println(attach.toString());
        //VirtualMachine 字面意义表示一个Java 虚拟机，也就是程序需要监控的目标虚拟机，提供了获取系统信息(比如获取内存dump、线程dump，类信息统计(比如已加载的类以及实例个数等)， loadAgent，Attach 和 Detach （Attach 动作的相反行为，从 JVM 上面解除一个代理）等方法，可以实现的功能可以说非常之强大 。该类允许我们通过给attach方法传入一个jvm的pid(进程id)，远程连接到jvm上
        //VirtualMachineDescriptor 则是一个描述虚拟机的容器类，配合 VirtualMachine 类完成各种功能
        //具体实现过程：通过VirtualMachine类的attach(pid)方法，便可以attach到一个运行中的java进程上，之后便可以通过loadAgent(agentJarPath)来将agent的jar包注入到对应的进程，然后对应的进程会调用agentmain方法。
        attach.loadAgent("/Users/langu/5-javasec/codeaudit/rasp_langu/attach_agent/target/attach_agent-1.0-SNAPSHOT-jar-with-dependencies.jar");//要加载的attach路径
        attach.detach(); //attack解除
    }
}
