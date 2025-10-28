package tinyjvm.classfile.constantpool;

import tinyjvm.classfile.ClassReader;

import java.nio.charset.StandardCharsets;

public class ConstantUtf8Info implements ConstantInfo {
    private String value;


    @Override
    public void readInfo(ClassReader reader) {
        int length = reader.readU2();
        byte[] bytes = reader.readBytes(length);
        this.value = new String(bytes, StandardCharsets.UTF_8);
    }

    public String getValue(){
        return value;
    }
}
