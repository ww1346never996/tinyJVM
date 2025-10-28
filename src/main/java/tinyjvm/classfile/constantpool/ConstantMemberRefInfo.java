package tinyjvm.classfile.constantpool;

import tinyjvm.classfile.ClassReader;

import tinyjvm.classfile.ClassReader;

// This is a base class for Fieldref, Methodref, and InterfaceMethodref
public class ConstantMemberRefInfo implements ConstantInfo {
    public int classIndex;
    public int nameAndTypeIndex;

    @Override
    public void readInfo(ClassReader reader) {
        this.classIndex = reader.readU2();
        this.nameAndTypeIndex = reader.readU2();
    }
}