package tinyjvm.instructions.constants;

import tinyjvm.instructions.BytecodeReader;
import tinyjvm.instructions.Instruction;
import tinyjvm.runtime.Frame;

public class RETURN implements Instruction {
    @Override public void fetchOperands(BytecodeReader reader) { /* No operands */ }

    @Override
    public void execute(Frame frame) {
        // 从当前线程的虚拟机栈中弹出当前帧
        frame.getThread().popFrame();
    }
}
