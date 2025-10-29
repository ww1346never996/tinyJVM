package tinyjvm.instructions.constants;

import tinyjvm.instructions.BytecodeReader;
import tinyjvm.instructions.Instruction;
import tinyjvm.rtda.OperandStack;
import tinyjvm.runtime.Frame;

public class IADD implements Instruction {
    @Override public void fetchOperands(BytecodeReader reader) { /* No operands */ }

    @Override
    public void execute(Frame frame) {
        OperandStack stack = frame.getOperandStack();
        int val2 = stack.popInt();
        int val1 = stack.popInt();
        int result = val1 + val2;
        stack.pushInt(result);
    }
}
