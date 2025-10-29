package tinyjvm.classfile.constantpool.attributes;

import tinyjvm.classfile.ClassReader;
import tinyjvm.classfile.constantpool.ConstantPool;

public interface AttributeInfo {
    void readInfo(ClassReader reader);

    // 静态工厂方法，这是解析属性的核心逻辑
    static AttributeInfo[] readAttributes(ClassReader reader, ConstantPool cp) {
        int attributesCount = reader.readU2();
        AttributeInfo[] attributes = new AttributeInfo[attributesCount];
        for (int i = 0; i < attributesCount; i++) {
            attributes[i] = readAttribute(reader, cp);
        }
        return attributes;
    }

    static AttributeInfo readAttribute(ClassReader reader, ConstantPool cp) {
        int attrNameIndex = reader.readU2();
        String attrName = cp.getUtf8(attrNameIndex);
        long attrLen = reader.readU4(); // 注意是 u4

        switch (attrName) {
            case "Code":
                return new CodeAttribute(cp, attrNameIndex, attrLen, reader);
            // case "ConstantValue": return new ConstantValueAttribute(...);
            // ... 其他属性
            default:
                // 对于我们不支持的属性，直接跳过
                return new UnparsedAttribute(attrName, attrLen);
        }
    }
}