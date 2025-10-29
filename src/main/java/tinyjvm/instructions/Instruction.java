package tinyjvm.instructions;

import tinyjvm.runtime.Frame;

public interface Instruction {
    /**
     * 从字节码中读取操作数
     * @param reader 字节码读取器
     */
    void fetchOperands(BytecodeReader reader);

    /**
     * 执行指令
     * @param frame 当前栈帧
     */
    void execute(Frame frame);
}