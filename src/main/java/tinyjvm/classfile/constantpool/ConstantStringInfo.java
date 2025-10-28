package tinyjvm.classfile.constantpool;

import tinyjvm.classfile.ClassReader;

public class ConstantStringInfo implements ConstantInfo{
    public int stringIndex;

    @Override
    public void readInfo(ClassReader reader) {
        this.stringIndex = reader.readU2();
    }
}
