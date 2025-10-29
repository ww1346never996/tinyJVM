package tinyjvm.classfile.constantpool.attributes;

import tinyjvm.classfile.ClassReader;
import tinyjvm.classfile.constantpool.ConstantPool;

public class CodeAttribute implements AttributeInfo {
    private ConstantPool constantPool;
    private int maxStack;
    private int maxLocals;
    private byte[] code;
    // 还可以有 exceptionTable, attributes 等，初期可忽略

    // 注意构造函数的变化，它在工厂方法中被调用
    public CodeAttribute(ConstantPool cp, int attrNameIndex, long attrLen, ClassReader reader) {
        this.constantPool = cp;
        // 传统的 readInfo 逻辑放在这里
        this.maxStack = reader.readU2();
        this.maxLocals = reader.readU2();
        int codeLength = (int) reader.readU4();
        this.code = reader.readBytes(codeLength);

        // 跳过异常表和子属性
        skipExceptionTable(reader);
        skipAttributes(reader);
    }

    // readInfo 方法可以空着，因为逻辑移到了构造函数里
    @Override public void readInfo(ClassReader reader) { /* no-op */ }

    private void skipExceptionTable(ClassReader reader) {
        int exceptionTableLength = reader.readU2();
        reader.readBytes(exceptionTableLength * 8); // 每个异常表项占8字节
    }

    private void skipAttributes(ClassReader reader) {
        int attributesCount = reader.readU2();
        for (int i = 0; i < attributesCount; i++) {
            int nameIndex = reader.readU2();
            long len = reader.readU4();
            reader.readBytes((int) len);
        }
    }

    // Getters
    public int getMaxStack() { return maxStack; }
    public int getMaxLocals() { return maxLocals; }
    public byte[] getCode() { return code; }
}
