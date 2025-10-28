package tinyjvm.classfile.constantpool;

import tinyjvm.classfile.ClassReader;

public class ConstantPool {

    private ConstantInfo[] infos;

    private ConstantPool(ConstantInfo[] infos) {
        this.infos = infos;
    }

    public static ConstantPool read(ClassReader reader) {
        int cpCount = reader.readU2();
        // The constant_pool table is indexed from 1 to constant_pool_count - 1.
        ConstantInfo[] infos = new ConstantInfo[cpCount];

        for (int i = 1; i < cpCount; i++) {
            infos[i] = readConstantInfo(reader);
            // Long and Double take up two slots
            int tag = infos[i].getClass().getSimpleName().equals("ConstantLongInfo") ||
                    infos[i].getClass().getSimpleName().equals("ConstantDoubleInfo") ? 1 : 0;
            if(tag == 1) {
                i++;
            }
        }
        return new ConstantPool(infos);
    }

    private static ConstantInfo readConstantInfo(ClassReader reader) {
        int tag = reader.readU1();
        ConstantInfo info = newConstantInfo(tag);
        info.readInfo(reader);
        return info;
    }

    private static ConstantInfo newConstantInfo(int tag) {
        switch (tag) {
            case ConstantTag.CLASS: return new ConstantClassInfo();
            case ConstantTag.FIELD_REF: return new ConstantFieldRefInfo();
            case ConstantTag.METHOD_REF: return new ConstantMethodRefInfo();
            case ConstantTag.INTERFACE_METHOD_REF: return new ConstantInterfaceMethodRefInfo();
            case ConstantTag.STRING: return new ConstantStringInfo();
            case ConstantTag.NAME_AND_TYPE: return new ConstantNameAndTypeInfo();
            case ConstantTag.UTF8: return new ConstantUtf8Info();
            // We will add other types like Integer, Long, etc. later
            default:
                // For now, let's create a dummy for unsupported types
                return new UnsupportedConstantInfo(tag);
        }
    }

    // A little helper class for constants we don't support yet
    static class UnsupportedConstantInfo implements ConstantInfo {
        private int tag;
        UnsupportedConstantInfo(int tag) { this.tag = tag; }
        @Override public void readInfo(ClassReader reader) {
            // For now, we do nothing. A more robust implementation might skip bytes
            // based on the constant type, but this is simpler for now.
            System.err.println("Warning: Unsupported constant pool tag: " + tag);
        }
    }

    // Utility method to get a UTF8 string from the pool
    public String getUtf8(int index) {
        if (index == 0 || index >= infos.length) {
            throw new IllegalArgumentException("Invalid constant pool index: " + index);
        }
        ConstantUtf8Info utf8Info = (ConstantUtf8Info) infos[index];
        return utf8Info.getValue();
    }

    // Add more getters as needed, for example:
    public String getClassName(int index) {
        ConstantClassInfo classInfo = (ConstantClassInfo) infos[index];
        return getUtf8(classInfo.nameIndex);
    }

    public ConstantInfo[] getInfos() {
        return infos;
    }
}