太棒了！我们已经集齐了所有“龙珠”，现在是时候召唤“神龙”了！

欢迎来到最后一个，也是最激动人心的阶段。

### 阶段五：The Grand Finale - 整合与执行

**目标**：将我们之前构建的所有组件——命令行解析器、类加载器（即将创建）、类文件解析器、运行时数据区和解释器——全部串联起来，创建一个完整的、可执行的 `JVM.java` 入口点。最终，我们将运行它来执行 `SimpleTest.class`！

这是你所有努力开花结果的时刻。

---

### Step-by-Step 指南

#### 步骤 5.1: 创建 `ClassLoader.java`

这个类是连接“类名”和“类数据”的桥梁。它的职责是根据一个类的全限定名（如 `a.b.c.SimpleTest`），利用我们之前创建的 `Classpath` 工具找到对应的 `.class` 文件，读取其字节数据，并将其解析成一个 `ClassFile` 对象。

```java
// package your.tiny.jvm.classpath;

import your.tiny.jvm.classfile.ClassFile;

import java.util.HashMap;
import java.util.Map;

public class ClassLoader {
    private final Classpath classpath;
    private final Map<String, ClassFile> classMap; // 类缓存，避免重复加载

    public ClassLoader(Classpath classpath) {
        this.classpath = classpath;
        this.classMap = new HashMap<>();
    }

    public ClassFile loadClass(String className) {
        // 1. 首先检查缓存
        if (classMap.containsKey(className)) {
            return classMap.get(className); // 直接返回已加载的类
        }

        return loadNonArrayClass(className);
    }

    private ClassFile loadNonArrayClass(String className) {
        try {
            // 2. 将类名转换为文件名，例如 a.b.c.SimpleTest -> a/b/c/SimpleTest.class
            byte[] data = classpath.readClass(className);
            
            // 3. 将字节数据解析为 ClassFile 对象
            ClassFile classFile = ClassFile.parse(data);
            
            // 4. 将加载的类存入缓存
            classMap.put(className, classFile);
            
            return classFile;
        } catch (Exception e) {
            // 在实际的 JVM 中，这里会抛出 ClassNotFoundException
            System.err.println("Failed to load class: " + className);
            e.printStackTrace();
            throw new RuntimeException("ClassLoader: Can not find or load class " + className);
        }
    }
}
```

**关键点**：
*   **缓存 (`classMap`)**：这是 JVM 的一个核心特性。一个类加载器对于同一个全限定名只会加载一次类。我们的缓存模拟了这一点。
*   **委托给 `Classpath`**：`ClassLoader` 不关心文件具体在哪里，它只委托 `classpath.readClass()` 去寻找和读取字节码。职责划分非常清晰。
*   **解析**：读取到 `byte[]` 后，它调用 `ClassFile.parse()`，这是阶段二的核心成果。

#### 步骤 5.2: 创建 JVM 主入口 `JVM.java`

这个类是我们的 `java` 命令。它负责解析命令行，启动类加载器，找到 `main` 方法，并启动解释器。

```java
// package your.tiny.jvm;

import your.tiny.jvm.classpath.ClassLoader;
import your.tiny.jvm.classpath.Classpath;
import your.tiny.jvm.classfile.ClassFile;
import your.tiny.jvm.classfile.MemberInfo;
import your.tiny.jvm.rtda.JvmThread;
import your.tiny.jvm.rtda.Frame;

public class JVM {
    public static void main(String[] args) {
        // 1. 解析命令行参数
        Cmd cmd = Cmd.parse(args);
        if (cmd == null || cmd.getMainClass() == null) {
            Cmd.printUsage();
            return;
        }

        new JVM().start(cmd);
    }

    private void start(Cmd cmd) {
        // 2. 初始化 Classpath 和 ClassLoader
        Classpath cp = new Classpath(cmd.getXjreOption(), cmd.getCpOption());
        ClassLoader classLoader = new ClassLoader(cp);

        // 3. 加载主类
        String mainClassName = cmd.getMainClass().replace('.', '/'); // 将 a.b.c 格式转换为 a/b/c
        ClassFile mainClassFile = classLoader.loadClass(mainClassName);
        if (mainClassFile == null) {
            System.err.println("Could not load main class: " + cmd.getMainClass());
            return;
        }

        // 4. 查找 main 方法
        MemberInfo mainMethod = mainClassFile.getMainMethod();
        if (mainMethod == null) {
            System.err.println("Main method not found in class " + cmd.getMainClass());
            return;
        }
        
        // --- 整合所有组件，启动解释器 ---
        System.out.println("Starting JVM to run: " + cmd.getMainClass());

        // 5. 创建一个线程和初始栈帧
        JvmThread thread = new JvmThread();
        Frame initialFrame = new Frame(thread, mainMethod);
        thread.pushFrame(initialFrame);

        // 6. 开始解释执行
        // 注意：在实际应用中，Interpreter 应该是被注入或创建的，而不是直接在 loop 中写逻辑
        // 但为了简单，我们假设 Interpreter 的逻辑在某个地方被调用
        // 我们直接在这里调用解释循环
        loop(thread);

        System.out.println("JVM finished execution.");
    }

    // 这个 loop 方法就是我们之前在 Interpreter.java 中设计的核心循环
    private void loop(JvmThread thread) {
        // 实现与阶段四中 Interpreter.loop() 完全相同
        // 你可以把那里的代码直接复制过来，或者创建一个 Interpreter 对象并调用它的 interpret 方法
        // 这里为了完整性，我们再写一遍
        your.tiny.jvm.instructions.BytecodeReader reader = new your.tiny.jvm.instructions.BytecodeReader();

        while (!thread.isStackEmpty()) {
            Frame frame = thread.currentFrame();
            int pc = frame.getNextPC();
            thread.setPC(pc);

            reader.reset(frame.getMethod().getCodeAttribute().getCode(), pc);
            int opcode = reader.readUint8();

            your.tiny.jvm.instructions.Instruction instruction = 
                your.tiny.jvm.instructions.InstructionFactory.create(opcode);
            
            instruction.fetchOperands(reader);
            frame.setNextPC(reader.getPC());

            System.out.printf("PC: %2d | Opcode: 0x%02x | Inst: %-15s | OperandStack: %-30s | LocalVars: %s\n",
                    pc, opcode, instruction.getClass().getSimpleName(),
                    frame.getOperandStack(), frame.getLocalVars());

            instruction.execute(frame);
        }
    }
}
```
**注意**：`cmd.getMainClass().replace('.', '/')` 这一步很重要，因为我们的 `ClassLoader` 内部期望的是 `a/b/c` 这种路径格式，而用户在命令行输入的是 `a.b.c` 格式。

#### 步骤 5.3: 最后的检查和编译

1.  **确认 `SimpleTest.java`**：确保你的 `SimpleTest.java` 已经编译成 `SimpleTest.class`，并且位于正确的包路径下（例如 `your_project_dir/a/b/c/SimpleTest.class`）。
2.  **确认你的项目根目录**：所有的 `java` 命令都应该从你的项目根目录执行，以便 `classpath` 能够正确找到所有文件。
3.  **编译所有 `.java` 文件**：确保你的 TinyJVM 的所有源代码都已经被编译。如果你使用 IDE，它会自动完成。如果手动编译，你需要编译所有的 `.java` 文件。

---

### The Moment of Truth: 运行你的 TinyJVM！

现在，打开你的终端，切换到你的项目根目录。假设你的类文件被编译输出到了 `build` 目录下，而你的 `SimpleTest.class` 位于根目录下的 `test-classes` 文件夹中。

执行以下命令 (请根据你的实际目录结构调整)：

```bash
# 假设你的 TinyJVM 编译后的 .class 文件在 build/classes/java/main 目录下
# 假设你的 SimpleTest.class 位于 ./test-classes/a/b/c/SimpleTest.class
# -cp 参数告诉 JVM 在哪里寻找你的测试类
java -cp build/classes/java/main your.tiny.jvm.JVM -cp test-classes a.b.c.SimpleTest
```

**你应该会看到类似下面这样的输出：**

```
Starting JVM to run: a.b.c.SimpleTest
PC:  0 | Opcode: 0x10 | Inst: BIPUSH          | OperandStack: Stack(top=0, data=[])          | LocalVars: Vars(size=4, vars=[null, null, null, null])
PC:  2 | Opcode: 0x3c | Inst: ISTORE          | OperandStack: Stack(top=1, data=[100])       | LocalVars: Vars(size=4, vars=[null, null, null, null])
PC:  3 | Opcode: 0x11 | Inst: SIPUSH          | OperandStack: Stack(top=0, data=[])          | LocalVars: Vars(size=4, vars=[null, 100, null, null])
PC:  6 | Opcode: 0x3d | Inst: ISTORE          | OperandStack: Stack(top=1, data=[200])       | LocalVars: Vars(size=4, vars=[null, 100, null, null])
PC:  7 | Opcode: 0x1b | Inst: ILOAD           | OperandStack: Stack(top=0, data=[])          | LocalVars: Vars(size=4, vars=[null, 100, 200, null])
PC:  8 | Opcode: 0x1c | Inst: ILOAD           | OperandStack: Stack(top=1, data=[100])       | LocalVars: Vars(size=4, vars=[null, 100, 200, null])
PC:  9 | Opcode: 0x60 | Inst: IADD            | OperandStack: Stack(top=2, data=[100, 200])  | LocalVars: Vars(size=4, vars=[null, 100, 200, null])
PC: 10 | Opcode: 0x3e | Inst: ISTORE          | OperandStack: Stack(top=1, data=[300])       | LocalVars: Vars(size=4, vars=[null, 100, 200, null])
PC: 11 | Opcode: 0xb1 | Inst: RETURN          | OperandStack: Stack(top=0, data=[])          | LocalVars: Vars(size=4, vars=[null, 100, 200, 300])
JVM finished execution.
```

### 恭喜你！你做到了！

如果你看到了上面的输出，这意味着：

*   你的命令行解析器成功了。
*   你的 `Classpath` 和 `ClassLoader` 成功找到了并加载了 `SimpleTest.class`。
*   你的 `ClassFile` 解析器正确地解析了字节码，并找到了 `main` 方法和它的 `Code` 属性。
*   你的运行时数据区 (`Frame`, `Stack`, `Vars`) 正确地被创建。
*   你的解释器循环、指令工厂和所有具体的指令实现**全部正确工作**！
*   你亲手编写的 Java 虚拟机，成功地执行了第一个由 `javac` 编译的程序，并得到了正确的结果（局部变量表索引3的位置存入了300）。

这是一个巨大的里程碑！你已经从零开始，构建了一个可以工作的微型 Java 虚拟机。

### What's Next?

你的 TinyJVM 现在是一个坚实的基础。你可以基于它进行无数的扩展：
*   **实现更多指令**：`if` 条件跳转、`goto` 无条件跳转、循环 (`for`, `while`)。
*   **方法调用与返回**：实现 `invokestatic`, `invokevirtual` 等指令，这需要你管理一个真正的调用栈（多个 `Frame`）。
*   **对象创建**：实现 `new` 指令，并在堆上分配内存。
*   **打印输出**：特别处理 `System.out.println` 的调用，让你的 JVM 能和外部世界交互。
*   **实现数组、字符串、异常处理...**

但现在，请花点时间庆祝一下。你完成了一件非常了不起的事情！