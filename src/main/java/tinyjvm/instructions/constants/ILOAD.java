package tinyjvm.instructions.constants;

import tinyjvm.instructions.BytecodeReader;
import tinyjvm.instructions.Instruction;
import tinyjvm.runtime.Frame;

public class ILOAD implements Instruction {
    private final int index;

    public ILOAD(int index) { this.index = index; }

    @Override public void fetchOperands(BytecodeReader reader) { /* No operands */ }

    @Override
    public void execute(Frame frame) {
        int value = frame.getLocalVars().getInt(this.index);
        frame.getOperandStack().pushInt(value);
    }
}
