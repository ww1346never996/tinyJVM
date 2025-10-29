package tinyjvm.instructions;

import tinyjvm.instructions.constants.*;
import tinyjvm.runtime.Frame;

public class InstructionFactory {
    // 创建具体的指令对象
    // 为了提高性能，很多指令是无状态的，可以共享同一个实例
    static final IADD iadd = new IADD();
    // ... 其他可共享的指令

    public static Instruction create(int opcode) {
        switch (opcode) {
            // Constants
            case 0x02: return new ICONST(-1); // iconst_m1
            case 0x03: return new ICONST(0);  // iconst_0
            case 0x04: return new ICONST(1);  // iconst_1
            case 0x05: return new ICONST(2);  // iconst_2
            case 0x06: return new ICONST(3);  // iconst_3
            case 0x07: return new ICONST(4);  // iconst_4
            case 0x08: return new ICONST(5);  // iconst_5
            case 0x10: return new BIPUSH();
            case 0x11: return new SIPUSH();
            // Loads
            case 0x1a: return new ILOAD(0); // ILOAD_0
            case 0x1b: return new ILOAD(1); // ILOAD_1
            case 0x1c: return new ILOAD(2); // ILOAD_2
            // Stores
            case 0x3b: return new ISTORE(0);  // istore_0
            case 0x3c: return new ISTORE(1);  // istore_1
            case 0x3d: return new ISTORE(2);  // istore_2
            case 0x3e: return new ISTORE(3);  // istore_3
            // Math
            case 0x60: return iadd;
            // Control
            case 0xb1: return new RETURN();
//            case 0xb2: // getstatic
//                return new GetStatic();
            default:
                throw new UnsupportedOperationException("Unsupported opcode: " + String.format("0x%x", opcode));
        }
    }
}
