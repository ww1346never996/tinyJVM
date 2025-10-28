package tinyjvm.classfile.constantpool;

import tinyjvm.classfile.ClassReader;

public class ConstantClassInfo implements ConstantInfo{
    public int nameIndex;

    @Override
    public void readInfo(ClassReader reader) {
        this.nameIndex = reader.readU2();
    }
}
