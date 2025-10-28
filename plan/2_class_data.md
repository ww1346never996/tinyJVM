好的，我们正式进入最激动人心的阶段！如果说阶段一是为造车准备好了引擎（字节码 `byte[]`），那么阶段二就是**精密地拆解和理解这台引擎的每一个零件**。

你提供的提纲非常完美，完全概括了这一阶段的核心任务。现在，我们来把这个提纲扩展成一个详细的、可操作的行动计划。

---

### 阶段二：解析 Class 文件 (Class File Parser) - 详细行动计划

**核心目标**：将一个扁平的字节数组 `byte[]`，转换成一个包含丰富结构化信息、易于查询的 `ClassFile` Java 对象。这就像是把一本用二进制语言写的书，翻译成了我们能理解的章节、段落和词语。

#### 前置工作：创建包结构

为了让代码结构清晰，我们为所有与 Class 文件解析相关的类创建一个新的包。

**行动**：在 `src/main/java/tinyjvm` 目录下，创建一个新的子目录 `classfile`。之后我们创建的所有解析相关的类，都将放在 `tinyjvm.classfile` 包下。

---

### 第 1 步：创建我们的“字节阅读器” - `ClassReader.java`

这是我们的基础工具，它的职责是像一个带有游标的阅读器，精确地在 `byte[]` 上移动，并按需读取 1、2、4 或 N 个字节。

**为什么需要它？** Class 文件格式对数据类型的大小有严格规定（u1, u2, u4），我们不能简单地将整个 `byte[]` 当作字符串来处理。直接操作字节数组和位移运算（`<<`, `|`）会非常繁琐且容易出错。我们可以借助 Java 的 `DataInputStream` 来极大地简化这个过程。

**行动**：创建 `src/main/java/tinyjvm/classfile/ClassReader.java`

```java
package tinyjvm.classfile;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * A wrapper for byte[] to read data in u1, u2, u4, etc.
 * It simplifies reading from the byte array by using DataInputStream.
 */
public class ClassReader {
    private DataInputStream dis;

    public ClassReader(byte[] classData) {
        // Wrap the byte array in streams for easy reading
        this.dis = new DataInputStream(new ByteArrayInputStream(classData));
    }

    // Read an unsigned 8-bit integer (u1)
    public int readU1() {
        try {
            return dis.readUnsignedByte();
        } catch (IOException e) {
            throw new ClassFormatError("Failed to read U1", e);
        }
    }

    // Read an unsigned 16-bit integer (u2)
    public int readU2() {
        try {
            return dis.readUnsignedShort();
        } catch (IOException e) {
            throw new ClassFormatError("Failed to read U2", e);
        }
    }

    // Read a signed 32-bit integer (used for u4 as well)
    // Java doesn't have an unsigned int, but readInt() reads 4 bytes, which is what we need.
    // For values > 2^31-1, it will be negative, but for offsets and counts, this is fine.
    public int readU4() {
        try {
            return dis.readInt();
        } catch (IOException e) {
            throw new ClassFormatError("Failed to read U4", e);
        }
    }

    // Read a specified number of bytes
    public byte[] readBytes(int length) {
        try {
            byte[] bytes = new byte[length];
            dis.readFully(bytes);
            return bytes;
        } catch (IOException e) {
            throw new ClassFormatError("Failed to read bytes", e);
        }
    }

    // A helper for reading arrays like interfaces, fields, methods
    public int[] readU2Array() {
        int count = readU2();
        int[] array = new int[count];
        for (int i = 0; i < count; i++) {
            array[i] = readU2();
        }
        return array;
    }
}
```

---

### 第 2 步：构建“蓝图”的骨架 - `ClassFile.java`

这个类是所有解析结果的最终容器。它将严格按照 JVM 规范中 `ClassFile` 的结构来定义。它的核心任务是驱动整个解析流程。

**行动**：创建 `src/main/java/tinyjvm/classfile/ClassFile.java`

```java
package tinyjvm.classfile;

// We will create these classes in the next steps
import tinyjvm.classfile.constantpool.ConstantPool;
import tinyjvm.classfile.member.MemberInfo;

/**
 * Represents the in-memory structure of a Java .class file.
 */
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

    private ClassFile() {
        // private constructor
    }

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
        // cf.constantPool = ConstantPool.read(reader);

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
```

---

### 阶段性测试

现在，我们先不急着实现常量池、字段和方法。我们先来验证一下目前的代码是否能工作。

**行动**：修改 `tinyjvm.Main` 中的 `startJvm` 方法。

```java
// In tinyjvm/Main.java

import tinyjvm.classfile.ClassFile; // <--- Add this import

private static void startJvm(Cmd cmd) {
    System.out.printf("Starting TinyJVM...\n");
    System.out.printf("classpath: %s | mainClass: %s | args: %s\n",
            cmd.getClasspath(), cmd.getMainClass(), cmd.getAppArgs());

    Classpath classpath = new Classpath(cmd.getClasspath());
    byte[] classData = classpath.readClass(cmd.getMainClass());

    if (classData == null) {
        System.err.println("Error: Could not find or load main class " + cmd.getMainClass());
        return;
    }

    // ---- NEW PART ----
    // Parse the bytecode into a ClassFile object
    ClassFile classFile = ClassFile.parse(classData);

    // This will print the magic number and version for now
    // ---- END NEW PART ----
}
```

**编译并运行**：

1.  重新编译所有代码：
    ```sh
    # Make sure to include the new classfile package
     javac -d out/tiny-jvm src/main/java/tinyjvm/**/*.java     
    ```

2.  运行你的 TinyJVM：
    ```sh
    java -cp out/tiny-jvm tinyjvm.Main -cp test-classes a.b.c.SimpleTest
    ```

**预期输出**：
你应该能看到类似这样的输出，证明你的 `ClassReader` 和 `ClassFile` 骨架已经能正确读取文件头了！

```
Starting TinyJVM...
classpath: test-classes | mainClass: a.b.c.SimpleTest | args: null
[Classpath] Found class: a.b.c.SimpleTest at test-classes/a/b/c/SimpleTest.class
Magic: 0xCAFEBABE
Major Version: 52   (or another number depending on your JDK version, 52 corresponds to Java 8)
Minor Version: 0
```

看到这个输出，就说明你的解析器已经开了个好头！接下来的步骤就是按照 `ClassFile` 的结构，一步步地去实现常量池、字段、方法和属性的解析。这个过程会很有趣，就像在玩一个大型的二进制解谜游戏。

**下一步就是最关键、也是最复杂的常量池解析。准备好了吗？**