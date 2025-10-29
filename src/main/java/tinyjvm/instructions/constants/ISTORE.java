package tinyjvm.instructions.constants;

import tinyjvm.instructions.BytecodeReader;
import tinyjvm.instructions.Instruction;
import tinyjvm.runtime.Frame;

public class ISTORE implements Instruction {
    private final int index;

    public ISTORE(int index) { this.index = index; }

    @Override public void fetchOperands(BytecodeReader reader) { /* No operands to fetch */ }

    @Override
    public void execute(Frame frame) {
        int value = frame.getOperandStack().popInt();
        frame.getLocalVars().setInt(this.index, value);
    }
}
