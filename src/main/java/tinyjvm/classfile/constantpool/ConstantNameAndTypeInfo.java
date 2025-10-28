package tinyjvm.classfile.constantpool;

import tinyjvm.classfile.ClassReader;

public class ConstantNameAndTypeInfo implements ConstantInfo{
    public int nameIndex;
    public int descriptorIndex;

    @Override
    public void readInfo(ClassReader reader) {
        this.nameIndex = reader.readU2();
        this.descriptorIndex = reader.readU2();
    }
}
