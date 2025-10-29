package tinyjvm.instructions;

import tinyjvm.runtime.Frame;
import tinyjvm.runtime.JvmThread;

public class BytecodeReader {
    private byte[] code;
    private int pc;

    public void reset(byte[] code, int pc) {
        this.code = code;
        this.pc = pc;
    }

    public int getPC() {
        return pc;
    }

    public int readUint8() {
        // Java 的 byte 是有符号的，通过 & 0xFF 转换为无符号的 int
        return code[pc++] & 0xFF;
    }

    public int readInt8() {
        // 直接读取一个有符号 byte
        return code[pc++];
    }

    public int readInt16() {
        int high = readInt8(); // 注意是有符号读取
        int low = readUint8(); // 无符号读取
        return (high << 8) | low;
    }

    // 如果需要跳过 N 个字节的 padding
    public void skipPadding(int n) {
        pc += n;
    }

    private void loop(JvmThread thread) {
        BytecodeReader reader = new BytecodeReader();

        while (true) {
            // 获取当前栈帧
            Frame frame = thread.currentFrame();
            int pc = frame.getNextPC();
            thread.setPC(pc); // 设置线程的PC

            // 设置 reader 以读取当前方法的字节码
            reader.reset(frame.getMethod().getCodeAttribute().getCode(), pc);

            // 读取操作码
            int opcode = reader.readUint8();

            // 根据操作码创建对应的指令实例
            Instruction instruction = InstructionFactory.create(opcode);

            // 从字节码流中读取操作数
            instruction.fetchOperands(reader);

            // 更新PC
            frame.setNextPC(reader.getPC());

            // 【调试黄金点】在这里打印日志
            System.out.printf("PC: %2d, Opcode: 0x%02x, Inst: %s, OperandStack: %s, LocalVars: %s\n",
                    pc, opcode, instruction.getClass().getSimpleName(),
                    frame.getOperandStack(), frame.getLocalVars());

            // 执行指令
            instruction.execute(frame);

            // 检查虚拟机栈是否为空，如果为空，说明主线程执行完毕
            if (thread.isStackEmpty()) {
                break;
            }
        }
    }
}
