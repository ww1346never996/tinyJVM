# TinyJVM 项目总结

本项目的目标是从零开始构建一个精简版 Java 虚拟机，并在此过程中深入理解 JVM 的核心原理。项目划分为多个阶段，每个阶段都聚焦构建 JVM 的某个组件或功能。下面对各阶段做简要总结。

---

## 阶段一：启动器和类路径 (Cmd & Classpath)
1. 创建可执行的主类 Main。能解析命令行参数，识别 -cp/-classpath。
2. 编写 Cmd 类，以封装命令行解析逻辑。
3. 实现 Classpath 类，能根据类名 (如 a/b/c/SimpleTest) 寻找并读取对应的 .class 文件至内存 (byte[])，只支持目录形式的类路径。

完成标志：  
通过参数指定类路径与主类后，程序可以正确输出类路径、主类、以及能顺利读取 .class 字节码。

---

## 阶段二：解析 Class 文件 (Class File Parser)
1. 创建 ClassReader 以读取 byte[] 中的无符号/有符号数值 (u1, u2, u4)。
2. 创建 ClassFile 结构对应的类与字段 (magic, minorVersion, majorVersion, constantPool, methods 等)。
3. 解析常量池 (ConstantPool)，包括每个常量的各种具体类型，如 CONSTANT_Class_info、CONSTANT_Utf8_info 等。
4. 解析字段 (FieldInfo)、方法 (MethodInfo) 及其属性 (AttributeInfo, CodeAttribute 等)。

完成标志：  
能够将 .class 文件字节流完整地解析成 ClassFile 对象，并可读取 main 方法相应的字节码属性。

---

## 阶段三：运行时数据区 (Runtime Data Areas)
1. 建立局部变量表 (LocalVars) 与操作数栈 (OperandStack) 的数据结构，用于存储和操作各类数值与对象引用。
2. 创建栈帧 (Frame)。每个方法调用对应一个 frame，内部包含 localVars 与 operandStack。
3. 实现线程模拟 (JvmThread)，持有一个 stack 用于 push/pop frame。
4. 创建 ClassLoader。负责从指定路径加载并缓存 (classMap) 已解析的 ClassFile 对象。

完成标志：  
拥有能够在内存中表示“方法执行时所需空间”的各组成成分 (Frame, LocalVars, OperandStack) 以及管理类加载的 ClassLoader。

---

## 阶段四：执行引擎 (Execution Engine / Interpreter)
1. 创建 Interpreter 或类似的循环逻辑。一个大循环，每次从当前 frame 的字节码中读取 opcode，匹配指令，执行操作数操作。
2. 编写 BytecodeReader 帮助管理字节码与 pc (program counter)。
3. 设计 Instruction 接口与 InstructionFactory，根据 opcode 返回对应指令实例 (如 BIPUSH, SIPUSH, IADD, ISTORE, ILOAD, RETURN 等)。
4. 在指令实现中，对 frame 的 localVars 与 operandStack 进行操作，如 bipush 把一个字节值压栈，istore 将栈顶值写入局部变量表等。

完成标志：  
可以成功解释并执行简单 .class 文件中的指令，将结果存储在操作数栈或局部变量表里，并在 RETURN 后退出循环。

---

## 阶段五：整合与执行 (Final)
1. 整合以上所有部分，形成一个可执行的 JVM.java （或 Main.java）入口:
   - 解析命令行。
   - 初始化 Classpath 与自定义 ClassLoader。
   - 加载目标类并查找 main 方法。
   - 创建 JvmThread 及初始栈帧并推入。
   - 通过 Interpreter（或内置 loop）执行字节码循环。
2. 使用 -cp test-classes a.b.c.SimpleTest 等参数正式运行，观察指令执行情况与最终结果。

完成标志：  
TinyJVM 可以加载并执行 SimpleTest 并在日志中正确显示每条指令对操作数栈与局部变量表的影响，成功完成 main 方法执行。

---

## 后续扩展
1. 支持更多指令 (条件跳转、方法调用、数组操作等)。
2. 支持对象与堆的分配 (`new`)。
3. 模拟 System.out.println，处理字面输出。
4. 处理多方法、多类的调用与返回、异常处理等。

---

## 总结

通过以上五大阶段的实施，我们搭建了一个“能运行简单 Java 程序”的 TinyJVM，涵盖了类加载、Class 文件解析、运行时数据区构建与字节码解释执行等核心环节。从中可以深入理解 Java 语言底层的工作原理以及 JVM 的运行机制，对后续扩展和学习深入的 JVM 功能奠定了扎实的基础。

动手实现 TinyJVM 的过程，是一次对 JVM 规范与字节码技术细节的全面探索，极具学习和实践价值。当你看到最终执行的日志时，证明你已经攻克了从类加载、字节码解析到最终执行的“Java 世界”的核心。祝贺你完成如此有挑战性的学习项目，愿你在下一步的拓展与深入研究中继续收获更多宝贵的经验与乐趣。
