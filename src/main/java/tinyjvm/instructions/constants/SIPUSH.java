package tinyjvm.instructions.constants;

import tinyjvm.instructions.BytecodeReader;
import tinyjvm.instructions.Instruction;
import tinyjvm.runtime.Frame;

public class SIPUSH implements Instruction {
    private int value;

    @Override
    public void fetchOperands(BytecodeReader reader) {
        this.value = reader.readInt16();
    }

    @Override
    public void execute(Frame frame) {
        frame.getOperandStack().pushInt(this.value);
    }
}
