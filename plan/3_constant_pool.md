非常好！常量池是 Class 文件的心脏，也是解析过程中最复杂但最有价值的部分。一旦我们攻克了它，剩下的部分就相对简单了。

常量池是一个由 `cp_info` 结构组成的表（数组）。每种常量都有一个 `tag` 字节来标识其类型，后面跟着该类型对应的数据。

我们将采用一种**工厂模式**来读取常量池。我们会有一个主方法 `readConstantPool`，它会读取 `tag`，然后根据 `tag` 的值，调用不同的方法来创建相应类型的常量对象。

---

### 第 3 步：解析常量池 (`ConstantPool`)

这个步骤比较长，我们将分几步来完成。

#### 3.1 创建常量池的“通用接口”和“标签”

首先，我们需要一个所有常量类型都能实现的接口，以及一个定义所有常量 `tag` 的地方。

**行动**：创建 `src/main/java/tinyjvm/classfile/constantpool/ConstantInfo.java`

```java
package tinyjvm.classfile.constantpool;

import tinyjvm.classfile.ClassReader;

/**
 * Base interface for all constant pool entries.
 */
public interface ConstantInfo {
    /**

     * Reads the constant info from the ClassReader.
     * Note: The tag byte has already been read.
     * @param reader The ClassReader to read data from.
     */
    void readInfo(ClassReader reader);
}
```

**行动**：创建 `src/main/java/tinyjvm/classfile/constantpool/ConstantTag.java`

```java
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
```

#### 3.2 创建各种常量类型的具体实现

现在，我们需要为每一种 `tag` 创建一个对应的类。为了简化，我们先实现几个最核心的：`Utf8`，`Class`，`NameAndType`，以及各种 `Ref` 类型。

**注意**：很多常量类型只包含指向常量池其他项的索引。例如，`CONSTANT_Class_info` 只包含一个指向 `CONSTANT_Utf8_info` 的索引。

**行动：创建 `CONSTANT_Utf8_info` 的类**
`src/main/java/tinyjvm/classfile/constantpool/ConstantUtf8Info.java`

```java
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
    
    public String getValue() {
        return value;
    }
}
```

**行动：创建 `CONSTANT_Class_info` 的类**
`src/main/java/tinyjvm/classfile/constantpool/ConstantClassInfo.java`

```java
package tinyjvm.classfile.constantpool;

import tinyjvm.classfile.ClassReader;

public class ConstantClassInfo implements ConstantInfo {
    public int nameIndex;

    @Override
    public void readInfo(ClassReader reader) {
        this.nameIndex = reader.readU2();
    }
}
```

**行动：创建 `CONSTANT_NameAndType_info` 的类**
`src/main/java/tinyjvm/classfile/constantpool/ConstantNameAndTypeInfo.java`

```java
package tinyjvm.classfile.constantpool;

import tinyjvm.classfile.ClassReader;

public class ConstantNameAndTypeInfo implements ConstantInfo {
    public int nameIndex;
    public int descriptorIndex;

    @Override
    public void readInfo(ClassReader reader) {
        this.nameIndex = reader.readU2();
        this.descriptorIndex = reader.readU2();
    }
}
```

**行动：创建表示引用的常量（`Fieldref`, `Methodref` 等）的基类**
这些结构都非常相似，我们可以用一个基类来表示。
`src/main/java/tinyjvm/classfile/constantpool/ConstantMemberRefInfo.java`

```java
package tinyjvm.classfile.constantpool;

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
```

**行动：创建具体的引用类**
这些类非常简单，只需要继承基类即可。
`src/main/java/tinyjvm/classfile/constantpool/ConstantFieldRefInfo.java`
`src/main/java/tinyjvm/classfile/constantpool/ConstantMethodRefInfo.java`
`src/main/java/tinyjvm/classfile/constantpool/ConstantInterfaceMethodRefInfo.java`

```java
// Example for ConstantFieldRefInfo, the other two are identical
package tinyjvm.classfile.constantpool;

public class ConstantFieldRefInfo extends ConstantMemberRefInfo {
    // Inherits everything from ConstantMemberRefInfo
}
```
(请自行创建另外两个文件 `ConstantMethodRefInfo.java` 和 `ConstantInterfaceMethodRefInfo.java`)

**行动：创建 `CONSTANT_String_info` 的类**
`src/main/java/tinyjvm/classfile/constantpool/ConstantStringInfo.java`
```java
package tinyjvm.classfile.constantpool;

import tinyjvm.classfile.ClassReader;

public class ConstantStringInfo implements ConstantInfo {
    public int stringIndex;

    @Override
    public void readInfo(ClassReader reader) {
        this.stringIndex = reader.readU2();
    }
}
```
*(为了保持本阶段的简洁，我们暂时不实现 `Integer`, `Float`, `Long`, `Double` 等。它们在 `SimpleTest` 中用不到。)*

#### 3.3 创建常量池的容器和工厂方法 - `ConstantPool.java`

这是常量池部分的核心。它负责读取常量池大小，然后循环创建所有常量对象。

**行动**：创建 `src/main/java/tinyjvm/classfile/constantpool/ConstantPool.java`

```java
package tinyjvm.classfile.constantpool;

import tinyjvm.classfile.ClassReader;

import java.util.HashMap;
import java.util.Map;

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
```
*请注意上面代码中的 `UnsupportedConstantInfo`，这是一个很好的实践，可以让我们在不完全支持所有常量类型的情况下继续解析。*

---

### 第 4 步：整合到 `ClassFile.java` 中

现在，回到我们的 `ClassFile.java`，把常量池解析的逻辑加进去。

**行动**：修改 `src/main/java/tinyjvm/classfile/ClassFile.java`

```java
package tinyjvm.classfile;

// ... (existing imports)
import tinyjvm.classfile.constantpool.ConstantPool; // Add this

public class ClassFile {
    // ... (existing fields)
    
    // Change this field's type
    public ConstantPool constantPool;
    
    // ... (constructor)

    public static ClassFile parse(byte[] classData) {
        ClassReader reader = new ClassReader(classData);
        ClassFile cf = new ClassFile();

        // 1. Read Magic Number & Versions
        cf.readAndCheckMagic(reader);
        cf.readAndCheckVersion(reader);

        // 2. Read Constant Pool
        cf.constantPool = ConstantPool.read(reader); // <--- UNCOMMENT AND ACTIVATE THIS

        // We'll leave the rest commented out for now
        // cf.accessFlags = reader.readU2();
        // cf.thisClass = reader.readU2();
        // cf.superClass = reader.readU2();
        // ...

        System.out.println("Magic: " + String.format("0x%X", cf.magic));
        System.out.println("Major Version: " + cf.majorVersion);
        System.out.println("Minor Version: " + cf.minorVersion);
        
        // ---- NEW TEST CODE ----
        System.out.println("Constant pool count: " + cf.constantPool.getInfos().length);
        
        return cf;
    }

    // ... (rest of the class)
}
```

---

### 第 5 步：编译和运行

1.  **编译所有 Java 文件**。由于我们添加了大量新文件，请确保全部编译。
    ```sh
    find src/main/java -name "*.java" | xargs javac -d out/tiny-jvm
    ```

2.  **运行**
    ```sh
    java -cp out/tiny-jvm tinyjvm.Main -cp test-classes a.b.c.SimpleTest
    ```

**预期输出**：
你应该看到类似这样的输出。`constant pool count` 的具体数字取决于你的 JDK 版本，但它应该是一个大于 10 的数字。

```
Starting TinyJVM...
classpath: test-classes | mainClass: a.b.c.SimpleTest | args: null
[Classpath] Found class: a.b.c.SimpleTest at test-classes/a/b/c/SimpleTest.class
Magic: 0xCAFEBABE
Major Version: 52
Minor Version: 0
Constant pool count: 19  // Or some other number
```

如果看到这个，恭喜你！你已经成功攻克了 Class 文件解析中最困难的部分！我们现在有了一个内存中的常量池表示，下一步就可以利用它来解析类的元数据、字段和方法了。