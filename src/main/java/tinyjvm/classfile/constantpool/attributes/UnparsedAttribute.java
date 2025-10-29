package tinyjvm.classfile.constantpool.attributes;

import tinyjvm.classfile.ClassReader;
import tinyjvm.classfile.constantpool.ConstantPool;

public class UnparsedAttribute implements AttributeInfo {
    private String name;
    private long length;
    // 我们甚至可以保存原始字节，如果未来想支持它们的话，但初期不需要
    // private byte[] info;

    public UnparsedAttribute(String name, long length) {
        this.name = name;
        this.length = length;
    }

    /**
     * 从 ClassReader 中读取并丢弃该属性的数据。
     */
    @Override
    public void readInfo(ClassReader reader) {
        // 跳过 length 长度的字节
        reader.readBytes((int) this.length);
    }

    // 你也可以把跳过逻辑直接放在工厂方法里，让这个类更简单。
    // 两种方式都可以。
}