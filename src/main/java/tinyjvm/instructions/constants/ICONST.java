package tinyjvm.instructions.constants;

import tinyjvm.instructions.BytecodeReader;
import tinyjvm.instructions.Instruction;
import tinyjvm.runtime.Frame;

public class ICONST implements Instruction {
    private final int value;

    // 构造函数接收要推入栈的常量值
    public ICONST(int value) {
        this.value = value;
    }

    // 这些指令后面没有操作数，所以此方法为空
    @Override
    public void fetchOperands(BytecodeReader reader) {
        // No operands to fetch
    }

    @Override
    public void execute(Frame frame) {
        frame.getOperandStack().pushInt(this.value);
    }
}
