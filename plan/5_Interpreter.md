太棒了！我们已经搭建好了舞台 (`Frame`) 和后台 (`ClassLoader`)，现在是时候让演员（字节码指令）登场表演了。欢迎来到项目中最激动人心的部分——**阶段四：执行引擎 (Execution Engine / Interpreter)**。

### 阶段四：执行引擎 (Execution Engine / Interpreter)

**目标**：创建一个可以读取、解码并执行字节码的解释器。解释器将逐条执行 `main` 方法中的指令，并精确地操作我们在阶段三中创建的运行时数据区（局部变量表和操作数栈）。

这是让你的 JVM “活”起来的一步。

### 字节码的“大阅兵”

在我们开始写代码之前，让我们先看看我们的目标程序 `SimpleTest.java` 编译后的字节码是什么样子的。这是我们的“作战地图”。

在你的项目目录下运行： `javap -c a/b/c/SimpleTest.class` (如果你的类有包名，确保在根目录下运行)

你应该会看到类似这样的输出：

```
public static void main(java.lang.String[]);
  Code:
     0: bipush        100      // 将 100 推入操作数栈
     2: istore_1               // 从操作数栈弹出一个 int，存入局部变量表索引为 1 的位置 (a)
     3: sipush        200      // 将 200 推入操作数栈 (short push, for values needing 2 bytes)
     6: istore_2               // 存入局部变量表索引为 2 的位置 (b)
     7: iload_1                // 从局部变量表索引 1 加载 int (a) 到操作数栈
     8: iload_2                // 从局部变量表索引 2 加载 int (b) 到操作数栈
     9: iadd                   // 从操作数栈弹出两个 int，相加，结果推回操作数栈
    10: istore_3               // 存入局部变量表索引为 3 的位置 (c)
    11: return                 // 方法返回
```
*(注：局部变量表索引 0 通常留给 `main` 方法的 `String[] args` 参数。)*

我们的任务就是用 Java 代码模拟上面每一步的过程。

---

### Step-by-Step 指南

#### 步骤 4.1: 创建解释器类 `Interpreter.java`

这是我们执行引擎的主体。它接收一个方法，然后驱动整个执行过程。

```java
// package your.tiny.jvm;

import your.tiny.jvm.rtda.JvmThread;
import your.tiny.jvm.rtda.Frame;
import your.tiny.jvm.classfile.MemberInfo;

public class Interpreter {

    public void interpret(MemberInfo method) {
        // 创建一个线程
        JvmThread thread = new JvmThread();
        
        // 为要执行的方法创建一个栈帧
        Frame frame = new Frame(thread, method);
        
        // 将栈帧推入线程的虚拟机栈
        thread.pushFrame(frame);
        
        // 开始执行循环
        loop(thread);
    }

    private void loop(JvmThread thread) {
        // 这是一个简化的实现，真正的实现应该在 loop 方法内部
        // BytecodeReader reader = new BytecodeReader();
        // ... loop logic ...
    }
}
```
*注意：我们在这里稍微调整了设计，`Frame` 的构造函数现在接收一个 `JvmThread`。这更符合规范，因为栈帧确实与线程绑定。请相应地更新你的 `Frame` 构造函数。*

`new Frame(JvmThread thread, MemberInfo method)`

#### 步骤 4.2: 引入 `BytecodeReader`，简化指令读取

直接操作 `byte[]` 和 `pc` 容易出错。我们可以创建一个辅助类来封装字节码的读取，让 PC 的管理变得自动化和安全。

```java
// package your.tiny.jvm.instructions;

public class BytecodeReader {
    private byte[] code;
    private int pc;

    public void reset(byte[] code, int pc) {
        this.code = code;
        this.pc = pc;
    }

    public int getPC() {
        return pc;
    }

    public int readUint8() {
        // Java 的 byte 是有符号的，通过 & 0xFF 转换为无符号的 int
        return code[pc++] & 0xFF;
    }

    public int readInt8() {
        // 直接读取一个有符号 byte
        return code[pc++];
    }

    public int readInt16() {
        int high = readInt8(); // 注意是有符号读取
        int low = readUint8(); // 无符号读取
        return (high << 8) | low;
    }
    
    // 如果需要跳过 N 个字节的 padding
    public void skipPadding(int n) {
        pc += n;
    }
}
```

#### 步骤 4.3: 实现核心解释器循环 `loop()`

这是解释器的“心跳”。它是一个 `while` 循环，永不停止，直到遇到 `return` 指令或者 JVM 退出。

修改你的 `Interpreter.java` 中的 `loop` 方法：

```java
// 在 Interpreter.java 中

private void loop(JvmThread thread) {
    BytecodeReader reader = new BytecodeReader();

    while (true) {
        // 获取当前栈帧
        Frame frame = thread.currentFrame();
        int pc = frame.getNextPC();
        thread.setPC(pc); // 设置线程的PC

        // 设置 reader 以读取当前方法的字节码
        reader.reset(frame.getMethod().getCodeAttribute().getCode(), pc);
        
        // 读取操作码
        int opcode = reader.readUint8();
        
        // 根据操作码创建对应的指令实例
        Instruction instruction = InstructionFactory.create(opcode);
        
        // 从字节码流中读取操作数
        instruction.fetchOperands(reader);
        
        // 更新PC
        frame.setNextPC(reader.getPC());

        // 【调试黄金点】在这里打印日志
        System.out.printf("PC: %2d, Opcode: 0x%02x, Inst: %s, OperandStack: %s, LocalVars: %s\n",
            pc, opcode, instruction.getClass().getSimpleName(),
            frame.getOperandStack(), frame.getLocalVars());

        // 执行指令
        instruction.execute(frame);
        
        // 检查虚拟机栈是否为空，如果为空，说明主线程执行完毕
        if (thread.isStackEmpty()) {
            break;
        }
    }
}
```
*这个设计引入了 `Instruction` 接口和 `InstructionFactory`，这是实现所有指令的优雅模式。我们接下来就创建它们。*

#### 步骤 4.4: 设计 `Instruction` 接口和工厂

为每个字节码指令创建一个类，它们都实现一个公共的 `Instruction` 接口。

1.  **`Instruction.java` 接口**:

    ```java
    // package your.tiny.jvm.instructions;
    import your.tiny.jvm.rtda.Frame;

    public interface Instruction {
        /**
         * 从字节码中读取操作数
         * @param reader 字节码读取器
         */
        void fetchOperands(BytecodeReader reader);

        /**
         * 执行指令
         * @param frame 当前栈帧
         */
        void execute(Frame frame);
    }
    ```

2.  **`InstructionFactory.java`**:
    这个工厂类根据 `opcode` 返回对应的指令对象。

    ```java
    // package your.tiny.jvm.instructions;

    import your.tiny.jvm.instructions.loads.*;
    import your.tiny.jvm.instructions.stores.*;
    import your.tiny.jvm.instructions.math.*;
    import your.tiny.jvm.instructions.constants.*;
    import your.tiny.jvm.instructions.control.*;

    public class InstructionFactory {
        // 创建具体的指令对象
        // 为了提高性能，很多指令是无状态的，可以共享同一个实例
        static final ICONST_0 iconst_0 = new ICONST_0();
        static final IADD iadd = new IADD();
        static final ISTORE_1 istore_1 = new ISTORE_1();
        // ... 其他可共享的指令

        public static Instruction create(int opcode) {
            switch (opcode) {
                // Constants
                case 0x03: return iconst_0;
                case 0x10: return new BIPUSH(); // 有操作数，不能共享
                case 0x11: return new SIPUSH(); // 有操作数，不能共享
                // Loads
                case 0x1a: return new ILOAD(0); // ILOAD_0
                case 0x1b: return new ILOAD(1); // ILOAD_1
                case 0x1c: return new ILOAD(2); // ILOAD_2
                // Stores
                case 0x3b: return new ISTORE(0); // ISTORE_0
                case 0x3c: return new ISTORE(1); // ISTORE_1
                case 0x3d: return new ISTORE(2); // ISTORE_2
                case 0x3e: return new ISTORE(3); // ISTORE_3
                // Math
                case 0x60: return iadd;
                // Control
                case 0xb1: return new RETURN();
                default:
                    throw new UnsupportedOperationException("Unsupported opcode: " + String.format("0x%x", opcode));
            }
        }
    }
    ```
    *这里我们把 `iload_0`, `iload_1` 等都归纳为通用的 `ILOAD` 指令，通过构造函数传入索引，代码更简洁。`istore` 同理。*

#### 步骤 4.5: 实现第一批指令！

现在，为我们 `SimpleTest` 程序需要的每个指令创建类。建议将它们放在 `instructions` 包下的子包中，如 `constants`, `loads`, `stores`, `math`。

1.  **`BIPUSH.java`** (将 byte 推入栈)

    ```java
    // package your.tiny.jvm.instructions.constants;
    public class BIPUSH implements Instruction {
        private int value;

        @Override
        public void fetchOperands(BytecodeReader reader) {
            this.value = reader.readInt8();
        }

        @Override
        public void execute(Frame frame) {
            frame.getOperandStack().pushInt(this.value);
        }
    }
    ```
2.  **`SIPUSH.java`** (将 short 推入栈)
    ```java
    // package your.tiny.jvm.instructions.constants;
    public class SIPUSH implements Instruction {
        private int value;

        @Override
        public void fetchOperands(BytecodeReader reader) {
            this.value = reader.readInt16();
        }

        @Override
        public void execute(Frame frame) {
            frame.getOperandStack().pushInt(this.value);
        }
    }
    ```
3.  **`ISTORE.java`** (通用 `istore` 指令)
    ```java
    // package your.tiny.jvm.instructions.stores;
    public class ISTORE implements Instruction {
        private final int index;

        public ISTORE(int index) { this.index = index; }

        @Override public void fetchOperands(BytecodeReader reader) { /* No operands to fetch */ }

        @Override
        public void execute(Frame frame) {
            int value = frame.getOperandStack().popInt();
            frame.getLocalVars().setInt(this.index, value);
        }
    }
    ```
4.  **`ILOAD.java`** (通用 `iload` 指令)
    ```java
    // package your.tiny.jvm.instructions.loads;
    public class ILOAD implements Instruction {
        private final int index;

        public ILOAD(int index) { this.index = index; }

        @Override public void fetchOperands(BytecodeReader reader) { /* No operands */ }

        @Override
        public void execute(Frame frame) {
            int value = frame.getLocalVars().getInt(this.index);
            frame.getOperandStack().pushInt(value);
        }
    }
    ```
5.  **`IADD.java`** (整数加法)
    ```java
    // package your.tiny.jvm.instructions.math;
    public class IADD implements Instruction {
        @Override public void fetchOperands(BytecodeReader reader) { /* No operands */ }

        @Override
        public void execute(Frame frame) {
            OperandStack stack = frame.getOperandStack();
            int val2 = stack.popInt();
            int val1 = stack.popInt();
            int result = val1 + val2;
            stack.pushInt(result);
        }
    }
    ```
6.  **`RETURN.java`** (void 方法返回)
    ```java
    // package your.tiny.jvm.instructions.control;
    public class RETURN implements Instruction {
        @Override public void fetchOperands(BytecodeReader reader) { /* No operands */ }

        @Override
        public void execute(Frame frame) {
            // 从当前线程的虚拟机栈中弹出当前帧
            frame.getThread().popFrame();
        }
    }
    ```
    *在`Frame`的构造函数中需要保存thread的引用 `private final JvmThread thread;`，并提供一个getter。*

---

### 阶段四完成标志

1.  你已经创建了 `Interpreter.java`，并实现了核心的 `loop` 循环。
2.  你拥有了 `BytecodeReader` 辅助类。
3.  你设计了 `Instruction` 接口、`InstructionFactory` 工厂，并为 `SimpleTest` 所需的**所有**指令（`bipush`, `sipush`, `istore_x`, `iload_x`, `iadd`, `return`）创建了具体的实现类。
4.  你的 `loop` 方法中有详细的日志打印，这是你最强大的调试武器。

当你完成这些，你就可以进入最后一个、也是最令人兴奋的阶段：整合与运行！你离亲眼看到自己的 TinyJVM 执行第一个 Java 程序只有一步之遥了！