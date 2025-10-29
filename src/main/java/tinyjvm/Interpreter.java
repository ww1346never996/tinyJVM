package tinyjvm;

import tinyjvm.classfile.MemberInfo;
import tinyjvm.runtime.Frame;
import tinyjvm.runtime.JvmThread;

public class Interpreter {
    public void interpret(MemberInfo method) {
        // 创建一个线程
        JvmThread thread = new JvmThread();

        // 为要执行的方法创建一个栈帧
        Frame frame = new Frame(thread, method);

        // 将栈帧推入线程的虚拟机栈
        thread.pushFrame(frame);

        // 开始执行循环
        loop(thread);
    }

    private void loop(JvmThread thread) {
        // 这是一个简化的实现，真正的实现应该在 loop 方法内部
        // BytecodeReader reader = new BytecodeReader();
        // ... loop logic ...
    }
}
