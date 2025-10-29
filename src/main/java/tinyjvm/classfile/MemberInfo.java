package tinyjvm.classfile;

import tinyjvm.classfile.constantpool.ConstantPool;
import tinyjvm.classfile.constantpool.attributes.AttributeInfo;
import tinyjvm.classfile.constantpool.attributes.CodeAttribute;

public class MemberInfo {
    private ConstantPool constantPool; // 持有常量池的引用，方便查找
    private int accessFlags;
    private int nameIndex;
    private int descriptorIndex;
    private AttributeInfo[] attributes;

    public MemberInfo(ClassReader reader, ConstantPool constantPool) {
        this.constantPool = constantPool;
        this.accessFlags = reader.readU2();
        this.nameIndex = reader.readU2();
        this.descriptorIndex = reader.readU2();
        this.attributes = AttributeInfo.readAttributes(reader, constantPool); // 委托给 AttributeInfo 去解析
    }

    // 提供一些方便的 getter 方法
    public int getAccessFlags() { return accessFlags; }
    public String getName() { return constantPool.getUtf8(nameIndex); }
    public String getDescriptor() { return constantPool.getUtf8(descriptorIndex); }

    // 这个方法是连接阶段二和阶段三的桥梁！
    public CodeAttribute getCodeAttribute() {
        for (AttributeInfo attr : attributes) {
            if (attr instanceof CodeAttribute) {
                return (CodeAttribute) attr;
            }
        }
        return null; // 抽象方法或 native 方法没有 CodeAttribute
    }

    // 静态工厂方法，用于在 ClassFile 解析器中调用
    public static MemberInfo[] readMembers(ClassReader reader, ConstantPool constantPool) {
        int memberCount = reader.readU2();
        MemberInfo[] members = new MemberInfo[memberCount];
        for (int i = 0; i < memberCount; i++) {
            members[i] = new MemberInfo(reader, constantPool);
        }
        return members;
    }
}
