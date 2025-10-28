package tinyjvm.classfile.constantpool;

/**
 * Defines the tags for different types of constants in the constant pool.
 * See: https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4
 */
public class ConstantTag {
    public static final int CLASS = 7;
    public static final int FIELD_REF = 9;
    public static final int METHOD_REF = 10;
    public static final int INTERFACE_METHOD_REF = 11;
    public static final int STRING = 8;
    public static final int INTEGER = 3;
    public static final int FLOAT = 4;
    public static final int LONG = 5;
    public static final int DOUBLE = 6;
    public static final int NAME_AND_TYPE = 12;
    public static final int UTF8 = 1;
    public static final int METHOD_HANDLE = 15;
    public static final int METHOD_TYPE = 16;
    public static final int INVOKE_DYNAMIC = 18;
}