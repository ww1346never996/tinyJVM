package tinyjvm.runtime;

import tinyjvm.classfile.MemberInfo;
import tinyjvm.rtda.LocalVars;
import tinyjvm.rtda.OperandStack;

public class Frame {
    private Frame lower; // 指向调用者栈帧的指针，用于实现链式栈
    private LocalVars localVars; // 局部变量表
    private OperandStack operandStack; // 操作数栈
    private MemberInfo method; // 对当前方法的引用
    private int nextPC; // 下一条要执行的指令的地址
    private JvmThread thread;

    public Frame(JvmThread thread, MemberInfo method) {
        this.method = method;
        // 从方法的 Code 属性中获取 maxLocals 和 maxStack
        this.localVars = new LocalVars(method.getCodeAttribute().getMaxLocals());
        this.operandStack = new OperandStack(method.getCodeAttribute().getMaxStack());
        this.thread = thread;
    }

    // Getters for all fields
    public LocalVars getLocalVars() {
        return localVars;
    }

    public OperandStack getOperandStack() {
        return operandStack;
    }

    public MemberInfo getMethod() {
        return method;
    }

    public int getNextPC() {
        return nextPC;
    }

    public void setNextPC(int nextPC) {
        this.nextPC = nextPC;
    }

    public JvmThread getThread() {
        return thread;
    }

    public void setThread(JvmThread thread) {
        this.thread = thread;
    }
}
