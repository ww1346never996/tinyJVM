package tinyjvm.classfile.constantpool;

import tinyjvm.classfile.ClassReader;

public interface ConstantInfo {
    void readInfo(ClassReader reader);
}
