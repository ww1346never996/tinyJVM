package tinyjvm;

import tinyjvm.classfile.ClassFile;
import tinyjvm.classfile.MemberInfo;
import tinyjvm.classpath.Classpath;
import tinyjvm.instructions.BytecodeReader;
import tinyjvm.instructions.Instruction;
import tinyjvm.instructions.InstructionFactory;
import tinyjvm.runtime.ClassLoader;
import tinyjvm.runtime.Frame;
import tinyjvm.runtime.JvmThread;

public class JVM {
    public static void main(String[] args) {
        // 1. 解析命令行参数
        Cmd cmd = Cmd.parse(args);
        if (cmd == null || cmd.getMainClass() == null) {
            Cmd.printUsage();
            return;
        }

        new JVM().start(cmd);
    }

    private void start(Cmd cmd) {
        // 2. 初始化 Classpath 和 ClassLoader
        Classpath cp = new Classpath(cmd.getXjreOption(), cmd.getCpOption());
        ClassLoader classLoader = new ClassLoader(cp);

        // 3. 加载主类
        String mainClassName = cmd.getMainClass().replace('.', '/'); // 将 a.b.c 格式转换为 a/b/c
        ClassFile mainClassFile = classLoader.loadClass(mainClassName);
        if (mainClassFile == null) {
            System.err.println("Could not load main class: " + cmd.getMainClass());
            return;
        }

        // 4. 查找 main 方法
        MemberInfo mainMethod = mainClassFile.getMainMethod();
        if (mainMethod == null) {
            System.err.println("Main method not found in class " + cmd.getMainClass());
            return;
        }

        // --- 整合所有组件，启动解释器 ---
        System.out.println("Starting JVM to run: " + cmd.getMainClass());

        // 5. 创建一个线程和初始栈帧
        JvmThread thread = new JvmThread();
        Frame initialFrame = new Frame(thread, mainMethod);
        thread.pushFrame(initialFrame);

        // 6. 开始解释执行
        // 注意：在实际应用中，Interpreter 应该是被注入或创建的，而不是直接在 loop 中写逻辑
        // 但为了简单，我们假设 Interpreter 的逻辑在某个地方被调用
        // 我们直接在这里调用解释循环
        loop(thread);

        System.out.println("JVM finished execution.");
    }

    // 这个 loop 方法就是我们之前在 Interpreter.java 中设计的核心循环
    private void loop(JvmThread thread) {
        // 实现与阶段四中 Interpreter.loop() 完全相同
        // 你可以把那里的代码直接复制过来，或者创建一个 Interpreter 对象并调用它的 interpret 方法
        // 这里为了完整性，我们再写一遍
        BytecodeReader reader = new BytecodeReader();

        while (!thread.isStackEmpty()) {
            Frame frame = thread.currentFrame();
            int pc = frame.getNextPC();
            thread.setPC(pc);

            reader.reset(frame.getMethod().getCodeAttribute().getCode(), pc);
            int opcode = reader.readUint8();

            Instruction instruction =
                    InstructionFactory.create(opcode);

            instruction.fetchOperands(reader);
            frame.setNextPC(reader.getPC());

            System.out.printf("PC: %2d | Opcode: 0x%02x | Inst: %-15s | OperandStack: %-30s | LocalVars: %s\n",
                    pc, opcode, instruction.getClass().getSimpleName(),
                    frame.getOperandStack(), frame.getLocalVars());

            instruction.execute(frame);
        }
    }
}
