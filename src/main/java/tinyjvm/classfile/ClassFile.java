package tinyjvm.classfile;

// We will create these classes in the next steps
import tinyjvm.classfile.constantpool.ConstantPool;
import tinyjvm.classfile.member.MemberInfo;

public class ClassFile {
    // Magic number, must be 0xCAFEBABE
    public int magic;
    public int minorVersion;
    public int majorVersion;

    public ConstantPool constantPool;
    public int accessFlags;
    public int thisClass; // index into constant pool
    public int superClass; // index into constant pool
    public int[] interfaces; // array of indices into constant pool

    public MemberInfo[] fields;
    public MemberInfo[] methods;

    // We'll skip attributes at the ClassFile level for now to keep it simple
    private ClassFile() {}

    /**
     * The main parsing method. Takes raw bytecode and returns a structured ClassFile object.
     * @param classData The byte array from the .class file.
     * @return A parsed ClassFile object.
     */
    public static ClassFile parse(byte[] classData) {
        ClassReader reader = new ClassReader(classData);
        ClassFile cf = new ClassFile();

        // 1. Read Magic Number & Versions
        cf.readAndCheckMagic(reader);
        cf.readAndCheckVersion(reader);

        // 2. Read Constant Pool (The most complex part)
        // This will be implemented in the next steps
         cf.constantPool = ConstantPool.read(reader);

        // 3. Read Access Flags, This/Super Class, Interfaces
        // cf.accessFlags = reader.readU2();
        // cf.thisClass = reader.readU2();
        // cf.superClass = reader.readU2();
        // cf.interfaces = reader.readU2Array();

        // 4. Read Fields and Methods
        // These will be implemented in later steps
        // cf.fields = MemberInfo.readMembers(reader, cf.constantPool);
        // cf.methods = MemberInfo.readMembers(reader, cf.constantPool);

        // For now, let's just parse the magic and version to test
        System.out.println("Magic: " + String.format("0x%X", cf.magic));
        System.out.println("Major Version: " + cf.majorVersion);
        System.out.println("Minor Version: " + cf.minorVersion);
        // ---- NEW TEST CODE ----
        System.out.println("Constant pool count: " + cf.constantPool.getInfos().length);
        return cf;
    }

    private void readAndCheckMagic(ClassReader reader) {
        this.magic = reader.readU4();
        if (this.magic != 0xCAFEBABE) {
            throw new ClassFormatError("Invalid magic number!");
        }
    }

    private void readAndCheckVersion(ClassReader reader) {
        this.minorVersion = reader.readU2();
        this.majorVersion = reader.readU2();
        // You can add checks here, e.g., majorVersion must be between 45 and 61 (Java 1.1 to 17)
    }

    // Helper methods to get class names, etc. (we'll add these later)
    public String getClassName() {
        // return this.constantPool.getClassName(this.thisClass);
        return "Not implemented yet";
    }
}
