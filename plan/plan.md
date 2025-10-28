太棒了！从零开始用 Java 实现一个 TinyJVM 是一个非常有挑战性也极具回报的学习项目。它能让你深入理解 Java 语言的底层运行机制、内存管理和字节码执行过程。

我将为你提供一个详尽的、分阶段的 step-by-step 指南。我们会从最核心、最简单的功能开始，逐步扩展，最终实现一个可以运行简单 Java 程序的迷你虚拟机。

### 核心理念

我们的 TinyJVM 不会实现完整的 JVM 规范（那太庞大了），但会实现其核心组件，让你明白一个程序是如何被加载和执行的。我们将遵循以下简化原则：

*   **解释执行**：我们只实现一个字节码解释器，不涉及 JIT（即时编译）。
*   **单线程**：我们假设程序只有一个主线程。
*   **有限的指令集**：我们只实现运行简单程序所必需的字节码指令。
*   **简化的内存模型**：我们将用 Java 对象来模拟 JVM 的运行时数据区。

---

### 项目准备

1.  **开发环境**：
    *   Java JDK (8 或更高版本)
    *   一个好的 IDE (推荐 IntelliJ IDEA 或 VS Code)，它们对 Java 项目管理非常友好。
    *   Maven 或 Gradle (推荐)，用于项目结构管理和依赖管理（虽然早期我们不需要外部依赖，但良好的项目结构很重要）。
2.  **必备知识**：
    *   熟练掌握 Java 编程。
    *   了解基本的数据结构，如栈（Stack）、哈希表（Map）、数组（Array）。
    *   （强烈推荐）准备一份 **《Java 虚拟机规范》** (The Java® Virtual Machine Specification) 的电子版在手边。这是你的“圣经”。你不需要读完它，但需要随时查阅。
3.  **一个简单的目标程序**：
    这是我们 TinyJVM 要执行的目标。把它编译好，我们会用它来测试我们的 JVM。

    ```java
    // 保存为 a/b/c/a.b.c.SimpleTest.java
    package a.b.c;

    public class a.b.c.SimpleTest {
        public static void main(String[] args) {
            int a = 100;
            int b = 200;
            int c = a + b;
            // 我们暂时无法实现 System.out.println，所以先到这里
        }
    }
    ```

    使用 `javac` 命令编译它： `javac a/b/c/a.b.c.SimpleTest.java`。你会得到一个 `a.b.c.SimpleTest.class` 文件。

---

### Step-by-Step 指南

#### 阶段一：启动器和类路径 (Cmd & Classpath)

**目标**：让我们的 TinyJVM 能够接收命令行参数，并根据类路径找到目标 `.class` 文件。

1.  **创建主类 `Main.java`**：
    这是我们 TinyJVM 的入口。它需要解析命令行参数。
    一个典型的启动命令是 `java MyTinyJvm -cp /path/to/classes com.example.MyClass`。
    *   `-cp` 或 `-classpath` 是类路径。
    *   `com.example.MyClass` 是主类名。

    ```java
    public class Main {
        public static void main(String[] args) {
            Cmd cmd = Cmd.parse(args);
            if (cmd.isHelpFlag()) {
                System.out.println("Usage: java [-options] class [args...]");
                return;
            }
            if (cmd.isVersionFlag()) {
                System.out.println("java version \"1.8.0\""); // 假装是 1.8
                return;
            }
            startJvm(cmd);
        }

        private static void startJvm(Cmd cmd) {
            System.out.printf("classpath: %s class: %s args: %s\n",
                    cmd.getClasspath(), cmd.getMainClass(), cmd.getAppArgs());
            // TODO: 在这里启动真正的 JVM 逻辑
        }
    }
    ```

2.  **创建 `Cmd.java` 类**：
    用于解析命令行参数。可以使用简单的 `for` 循环或者第三方库（如 JCommander），但为了从零开始，我们手动解析。

3.  **创建 `Classpath.java` 类**：
    这个类的核心职责是：**根据类名读取字节码**。
    *   它包含一个 `readClass(String className)` 方法。
    *   `className` 的格式是 `java/lang/String`。你需要把它转换成文件路径 `java/lang/String.class`。
    *   类路径可能包含目录、JAR 文件或 ZIP 文件。初期，我们只实现**目录**形式的类路径。
    *   `readClass` 方法会遍历类路径中的所有条目，尝试找到对应的 `.class` 文件，并将其以字节数组 `byte[]` 的形式读入内存。

**本阶段完成标志**：运行 `java Main -cp . a/b/c/a.b.c.SimpleTest`，你的程序能够正确打印出类路径和主类名，并且 `Classpath` 类能够成功读取 `a.b.c.SimpleTest.class` 文件的字节码。

---

#### 阶段二：解析 Class 文件 (Class File Parser)

**目标**：将上一阶段读到的 `byte[]` 字节码，解析成一个结构化的 `ClassFile` 对象。

这是非常关键的一步。你需要参考《Java 虚拟机规范》中关于 "The `class` File Format" 的章节。一个 Class 文件有严格的结构：

```
ClassFile {
    u4             magic;
    u2             minor_version;
    u2             major_version;
    u2             constant_pool_count;
    cp_info        constant_pool[constant_pool_count-1];
    u2             access_flags;
    u2             this_class;
    u2             super_class;
    u2             interfaces_count;
    u2             interfaces[interfaces_count];
    u2             fields_count;
    field_info     fields[fields_count];
    u2             methods_count;
    method_info    methods[methods_count];
    u2             attributes_count;
    attribute_info attributes[attributes_count];
}
```

1.  **创建 `ClassReader.java`**：
    这个类包装了 `byte[]`，并提供 `readU1()`, `readU2()`, `readU4()`, `readBytes(int n)` 等方法，方便按字节、短整数、整数来读取数据。

2.  **创建 `ClassFile.java`**：
    这是 Class 文件的内存表示。它应该包含上述结构中的所有字段（如 `magic`, `majorVersion`, `constantPool`, `methods` 等）。

3.  **创建常量池 (`ConstantPool`) 相关的类**：
    常量池是 Class 文件中最复杂的部分。你需要为每种常量类型（`CONSTANT_Utf8`, `CONSTANT_Class`, `CONSTANT_Methodref` 等）创建一个对应的 Java 类。它们可以继承自一个公共的 `ConstantInfo` 接口/抽象类。

4.  **创建 `MemberInfo.java` (用于字段和方法) 和 `AttributeInfo.java`**：
    方法和字段有相似的结构（`access_flags`, `name_index`, `descriptor_index`）。可以创建一个 `MemberInfo` 作为它们的基类。
    属性（Attribute）是可扩展的，其中最重要的一个是 `Code` 属性，因为它包含了方法的**字节码指令**！

5.  **实现解析逻辑**：
    在 `ClassFile` 的构造函数或一个静态工厂方法中，接收一个 `ClassReader` 对象，然后严格按照 Class 文件格式的顺序进行解析，填充 `ClassFile` 对象的各个字段。

**本阶段完成标志**：能够成功解析 `a.b.c.SimpleTest.class` 文件，并能从解析出的 `ClassFile` 对象中，正确地找到 `main` 方法，以及 `main` 方法的 `Code` 属性，并打印出其中的字节码（以十六进制形式）。

---

#### 阶段三：运行时数据区 (Runtime Data Areas)

**目标**：用 Java 对象来模拟 JVM 的内存结构。

根据规范，每个线程都有自己的 PC 寄存器和 JVM 栈。所有线程共享堆和方法区。

1.  **方法区 (Method Area)**：
    最简单的实现：一个全局的 `static HashMap<String, ClassFile>`。当一个类被加载时，就把它解析好的 `ClassFile` 对象放进去。Key 是类名，Value 是 `ClassFile` 对象。

2.  **JVM 栈 (JVM Stack)**：
    *   创建一个 `JvmStack` 类，内部可以持有一个 `java.util.Stack<Frame>`。
    *   它需要 `push(Frame)` 和 `pop()` 方法。

3.  **栈帧 (Frame)**：
    这是**最核心**的数据结构。每当一个方法被调用，JVM 就会创建一个新的栈帧。
    *   创建一个 `Frame` 类。
    *   它内部包含两个关键部分：
        *   **局部变量表 (Local Variable Array)**：可以用一个 `Object[]` 数组模拟。
        *   **操作数栈 (Operand Stack)**：可以用一个 `java.util.Stack<Object>` 或自定义的栈结构模拟。
    *   `Frame` 还应该持有对当前方法 `MethodInfo` 的引用，以便访问字节码和常量池。

**本阶段完成标志**：创建了 `JvmStack` 和 `Frame` 类，它们的结构清晰，方法定义完整。

---

#### 阶段四：执行引擎 (Execution Engine / Interpreter)

**目标**：实现一个可以解释并执行字节码指令的解释器。

解释器的本质是一个大循环，循环内部是一个巨大的 `switch-case` 语句。

1.  **创建 `Interpreter.java`**：
    它有一个 `interpret(MethodInfo method)` 方法。

2.  **实现解释器主循环**：
    ```java
    public void interpret(MethodInfo method) {
        // ... 准备工作，如获取字节码数组 code[] ...
        int pc = 0; // PC 寄存器
        while (true) {
            int opcode = code[pc] & 0xFF; // 读取指令, 转为无符号 byte
            pc++;
            switch (opcode) {
                // 在这里实现各种指令
                case 0x03: // iconst_0
                    // ...
                    break;
                case 0x60: // iadd
                    // ...
                    break;
                case 0xb1: // return
                    return; // 方法执行结束

                // ... 其他指令 ...
                default:
                    throw new RuntimeException("Unsupported opcode: " + String.format("0x%x", opcode));
            }
        }
    }
    ```

3.  **实现第一批指令**：
    为了运行我们的 `a.b.c.SimpleTest.java`，你需要实现以下指令：
    *   `bipush` (0x10): 将一个 byte 值推入操作数栈。
    *   `istore_0`, `istore_1`, `istore_2` (0x3b, 0x3c, 0x3d): 将操作数栈顶的 int 值存入局部变量表的 0, 1, 2 号位置。
    *   `iload_0`, `iload_1`, `iload_2` (0x1a, 0x1b, 0x1c): 将局部变量表的 0, 1, 2 号位置的 int 值推入操作数栈。
    *   `iadd` (0x60): 从操作数栈弹出两个 int 值，相加，然后将结果推入栈顶。
    *   `return` (0xb1): 表示 void 方法返回。

    **实现每条指令的逻辑**：
    *   **阅读规范**：查阅 JVM 规范，了解每条指令的确切功能（它如何与操作数栈和局部变量表交互）。
    *   **操作 Frame**：指令的实现就是对当前 `Frame` 的局部变量表和操作数栈进行操作。
        *   `bipush 100` -> `frame.getOperandStack().push(100)`。
        *   `istore_1` -> `int val = frame.getOperandStack().pop(); frame.getLocalVars().set(1, val);`
        *   `iadd` -> `int val2 = frame.getOperandStack().pop(); int val1 = frame.getOperandStack().pop(); frame.getOperandStack().push(val1 + val2);`

**本阶段完成标志**：有一个可以工作的解释器循环，并且已经实现了上述几条核心指令。

---

#### 阶段五：整合与运行

**目标**：将所有部分串联起来，真正执行 `a.b.c.SimpleTest` 的 `main` 方法。

1.  **回到 `startJvm` 方法**：
    *   使用 `Classpath` 加载主类 (`a.b.c.SimpleTest`)。
    *   在加载的 `ClassFile` 对象中找到 `main` 方法。`main` 方法的描述符是 `([Ljava/lang/String;)V`。
    *   创建一个新的 `JvmStack`。
    *   为 `main` 方法创建一个新的 `Frame`，并将其压入 `JvmStack`。
    *   创建一个 `Interpreter` 实例，调用 `interpret(mainMethod)`。

2.  **调试！**
    这几乎是整个项目中最重要的一步。你的程序很可能会出错。
    *   **打印日志**：在解释器循环中，每执行一条指令，就打印出 PC 值、指令名称、执行后的操作数栈和局部变量表的状态。这是你最重要的调试工具。
    *   **对照 `javap`**：使用 `javap -v a/b/c/a.b.c.SimpleTest.class` 命令，你可以看到 Java 编译器生成的真实字节码。将你的执行过程与 `javap` 的输出进行对比，检查是否一致。

**本阶段完成标志**：你的 TinyJVM 能够完整地、正确地执行完 `a.b.c.SimpleTest.main` 方法中的所有字节码指令，没有抛出任何异常。在调试日志中，你可以看到 `c` 的值（300）被正确计算出来并存储在局部变量表中。

---

### 后续扩展 (进阶)

当你完成了以上五个阶段，你就拥有了一个可以工作的、最基础的 JVM！接下来，你可以像打怪升级一样添加新功能：

*   **实现方法调用**：
    *   实现 `invokestatic` 指令。这需要你在解释器中创建新的栈帧，并将参数从调用者的操作数栈传递到被调用者（新栈帧）的局部变量表中。
    *   实现 `ireturn` (返回 int 值)，`areturn` (返回引用值) 等。这需要将返回值从被调用者的操作数栈弹出，然后压入调用者的操作数栈。
*   **实现对象创建**：
    *   实现一个简单的 **堆 (Heap)**，比如用一个 `ArrayList<Object>`。
    *   实现 `new` 指令：在堆上创建一个对象实例，并将其引用（比如在 `ArrayList` 中的索引）压入操作数栈。
    *   实现 `invokespecial` (用于构造函数 `<init>`) 和 `invokevirtual` (用于实例方法)。
*   **实现 `System.out.println`**：
    这是一个 `native` 方法。你需要在你的 JVM 中“作弊”，当遇到 `invokevirtual` 调用 `println` 时，特殊处理它：从操作数栈中弹出参数，并使用 Java 的 `System.out.println` 打印出来。
*   **实现更多指令**：如分支指令 (`if_icmp<cond>`, `goto`)、数学运算指令、数组相关指令等。

### 推荐资源

*   **自己动手写Java虚拟机 (作者：张秀宏)**：这本书是这个主题的经典之作，提供了完整的代码和详细的讲解，强烈推荐。本指南的思路很大程度上参考了这本书。
*   **《Java 虚拟机规范》**：官方文档，最权威的资料。
*   **Hex Editor (如 HxD)**：一个十六进制编辑器，可以让你直观地查看 `.class` 文件的二进制内容，加深理解。

这个旅程会很漫长，但每完成一个阶段，你都会获得巨大的成就感和对 Java 底层无与伦比的洞察力。祝你好运，编码愉快！