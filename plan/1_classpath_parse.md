好的，我们来详细分解并实现**阶段一：启动器和类路径 (Cmd & Classpath)**。

这个阶段的目标非常明确：让我们的 TinyJVM 能够像一个真正的 `java` 命令一样启动，解析参数，并根据给定的类路径（classpath）成功地在文件系统中找到并读取目标 `.class` 文件的二进制数据。

### 本阶段目标

1.  创建一个可执行的 `Main` 类作为我们 JVM 的入口。
2.  实现一个 `Cmd` 类，用于解析命令行参数，如 `-cp`、主类名等。
3.  实现一个 `Classpath` 类，它能够根据类名（例如 `a.b.c.SimpleTest`）在指定的类路径下搜索对应的 `a/b/c/SimpleTest.class` 文件，并将其内容读入一个字节数组 `byte[]`。

---

### 1. 项目结构

首先，我们来规划一下项目的目录结构。使用 Maven 或 Gradle 的标准目录结构是一个好习惯。

```
tiny-jvm/
├── pom.xml (或 build.gradle)  <-- 项目管理文件，可选但推荐
└── src/
    └── main/
        └── java/
            └── com/yourname/jvm/  <-- 你的包名
                ├── Main.java      <-- JVM 启动入口
                ├── Cmd.java       <-- 命令行解析器
                └── classpath/
                    └── Classpath.java <-- 类路径处理器
```

同时，我们需要一个测试用的类。在项目根目录下创建一个测试目录 `test-classes`：

```
tiny-jvm/
├── ... (上面的结构)
└── test-classes/
    └── a/
        └── b/
            └── c/
                └── SimpleTest.java <-- 我们的目标测试程序
```

**准备测试文件**：
将以下代码保存到 `test-classes/a/b/c/SimpleTest.java`：

```java
package a.b.c;

public class SimpleTest {
    public static void main(String[] args) {
        // 暂时是空的
    }
}
```

然后，在 `tiny-jvm` 根目录下，使用 `javac` 编译它：

```sh
# -d . 表示将编译后的 .class 文件输出到当前目录
# 但它会根据包名自动创建 a/b/c 目录
javac -d test-classes test-classes/a/b/c/SimpleTest.java
```

执行后，你的 `test-classes` 目录结构会变成：
```
test-classes/
└── a/
    └── b/
        └── c/
            ├── SimpleTest.java
            └── SimpleTest.class  <-- 这是我们 JVM 要加载的目标！
```

---

### 2. 实现 `Cmd.java` (命令行解析器)

这个类的职责是封装和解析从 `main` 方法接收到的 `String[] args`。

**`src/main/java/com/yourname/jvm/Cmd.java`**:

```java
package com.yourname.jvm;

import java.util.Arrays;

public class Cmd {

    private boolean helpFlag;
    private boolean versionFlag;
    private String classpath;
    private String mainClass;
    private String[] appArgs; // 传递给目标Java程序的参数

    private Cmd() {
    }

    public static Cmd parse(String[] args) {
        Cmd cmd = new Cmd();
        // 我们假设参数格式是固定的，不做复杂的动态解析
        // 例如：java [-options] class [args...]
        if (args == null || args.length == 0) {
            cmd.helpFlag = true;
            return cmd;
        }

        // 查找 -cp 或 -classpath
        int cpIndex = -1;
        for (int i = 0; i < args.length; i++) {
            if ("-cp".equals(args[i]) || "-classpath".equals(args[i])) {
                cpIndex = i;
                break;
            }
        }

        if (cpIndex != -1) {
            // -cp 后面必须跟一个路径
            if (cpIndex + 1 < args.length) {
                cmd.classpath = args[cpIndex + 1];
                // 移除 -cp 和它的值，剩下的就是主类和程序参数
                String[] remainingArgs = new String[args.length - 2];
                System.arraycopy(args, 0, remainingArgs, 0, cpIndex);
                System.arraycopy(args, cpIndex + 2, remainingArgs, cpIndex, args.length - (cpIndex + 2));
                args = remainingArgs;
            } else {
                System.err.println("Error: Missing classpath value after -cp/-classpath");
                cmd.helpFlag = true;
                return cmd;
            }
        }

        // 此时的 args 应该只剩下主类和程序参数了
        if (args.length > 0) {
            // 处理 -help 和 -version
            if ("-help".equals(args[0]) || "-?".equals(args[0])) {
                cmd.helpFlag = true;
                return cmd;
            }
            if ("-version".equals(args[0])) {
                cmd.versionFlag = true;
                return cmd;
            }
            
            cmd.mainClass = args[0];
            if (args.length > 1) {
                cmd.appArgs = Arrays.copyOfRange(args, 1, args.length);
            }
        } else {
            // 如果移除了 -cp 后没有参数了，也显示帮助
            cmd.helpFlag = true;
        }

        // 如果没有指定-cp，默认使用当前目录
        if (cmd.classpath == null || cmd.classpath.isEmpty()) {
            cmd.classpath = ".";
        }
        
        return cmd;
    }

    // --- Getters ---
    public boolean isHelpFlag() { return helpFlag; }
    public boolean isVersionFlag() { return versionFlag; }
    public String getClasspath() { return classpath; }
    public String getMainClass() { return mainClass; }
    public String[] getAppArgs() { return appArgs; }
}
```

**代码解释**：
*   我们用一个简化的循环来解析参数。实际的 `java` 命令解析比这复杂得多，但对我们的 TinyJVM 足够了。
*   它能识别 `-help`、`-version` 和 `-cp`/`-classpath`。
*   如果用户没有提供 `-cp`，我们默认类路径为 `.`（当前目录）。
*   解析后，将结果存储在私有字段中，并通过公共的 getter 方法暴露。

---

### 3. 实现 `Classpath.java` (类路径处理器)

这是本阶段的核心。它负责根据类名查找并读取 `.class` 文件。

**`src/main/java/com/yourname/jvm/classpath/Classpath.java`**:

```java
package com.yourname.jvm.classpath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Classpath {
    // 我们只处理目录形式的类路径，不处理 JAR 文件
    private String[] dirPaths;

    public Classpath(String classpathStr) {
        // 根据操作系统的路径分隔符来分割字符串
        // Windows是分号(;) , Linux/macOS是冒号(:)
        dirPaths = classpathStr.split(File.pathSeparator);
    }

    /**
     * 根据类名读取类的字节码
     * @param className 格式: java.lang.Object 或 a.b.c.SimpleTest
     * @return 类的字节码数据，如果找不到则返回 null
     * @throws IOException 读取文件时发生错误
     */
    public byte[] readClass(String className) throws IOException {
        // 1. 将类名转换为相对文件路径
        // a.b.c.SimpleTest -> a/b/c/SimpleTest.class
        String classFilePath = className.replace('.', File.separatorChar) + ".class";

        // 2. 遍历我们所有的类路径目录
        for (String path : dirPaths) {
            // 3. 组合成完整的绝对路径
            String absolutePath = Paths.get(path, classFilePath).toString();
            File classFile = new File(absolutePath);

            // 4. 检查文件是否存在
            if (classFile.exists() && classFile.isFile()) {
                System.out.println("[Classpath] Found class: " + className + " at " + absolutePath);
                // 5. 读取文件所有字节并返回
                return Files.readAllBytes(classFile.toPath());
            }
        }
        
        // 遍历完所有路径都没找到
        return null;
    }
}
```

**代码解释**：
*   构造函数接收一个字符串（例如 `"lib/dep.jar;."`），并用 `File.pathSeparator` 来分割它，得到一个路径数组。这使代码跨平台。
*   `readClass` 方法是核心。它首先将 `.` 分隔的类名转换为 `/` 或 `\` 分隔的文件路径，并添加 `.class` 后缀。
*   然后，它遍历所有类路径条目，将每个条目与相对路径拼接，形成一个可能的绝对路径。
*   它检查这个路径是否存在并且是一个文件。如果找到，就使用 `Files.readAllBytes`（一个非常方便的 API）读取整个文件内容到 `byte[]` 中，然后返回。
*   如果遍历完所有路径都找不到，就返回 `null`。

---

### 4. 实现 `Main.java` (启动入口)

现在，我们将 `Cmd` 和 `Classpath` 整合起来。

**`src/main/java/com/yourname/jvm/Main.java`**:

```java
package com.yourname.jvm;

import com.yourname.classpath.org.tinyjvm.tinyjvm.Classpath;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        // 1. 解析命令行
        Cmd cmd = Cmd.parse(args);

        // 2. 处理帮助和版本信息
        if (cmd.isHelpFlag()) {
            printUsage();
            return;
        }
        if (cmd.isVersionFlag()) {
            System.out.println("java version \"1.0.0-tinyjvm\"");
            System.out.println("A simple JVM implementation from scratch.");
            return;
        }

        // 3. 启动JVM
        startJvm(cmd);
    }

    private static void startJvm(Cmd cmd) {
        System.out.println("Starting TinyJVM...");
        System.out.printf("classpath: %s | mainClass: %s | args: %s\n",
                cmd.getClasspath(), cmd.getMainClass(), Arrays.toString(cmd.getAppArgs()));
        
        // 4. 创建 Classpath 实例
        Classpath classpath = new Classpath(cmd.getClasspath());

        try {
            // 5. 读取主类的字节码
            byte[] classData = classpath.readClass(cmd.getMainClass());
            
            if (classData == null) {
                System.err.println("Error: Could not find or load main class " + cmd.getMainClass());
                return;
            }
            
            System.out.println("Main class loaded successfully! Bytecode length: " + classData.length);
            
            // 打印前10个字节看看 (十六进制)
            System.out.print("Magic number: ");
            for (int i = 0; i < 10 && i < classData.length; i++) {
                System.out.printf("%02X ", classData[i]);
            }
            System.out.println();
            
            // 下一阶段的工作将从这里开始：解析 classData
            
        } catch (IOException e) {
            System.err.println("Error reading class file: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java [-options] class [args...]");
        System.out.println("   -cp <path>       Specify where to find user class files");
        System.out.println("   -classpath <path> Same as -cp");
        System.out.println("   -version         Print product version and exit");
        System.out.println("   -help or -?      Print this help message");
    }
}
```

### 5. 编译和运行

1.  **编译你的 TinyJVM**：
    在 `tiny-jvm` 根目录下，编译所有 `com.yourname.jvm` 包下的源文件。你需要将编译输出目录指向一个地方，比如 `out/production/tiny-jvm`。

    ```sh
    # 创建输出目录
    mkdir -p out/production/tiny-jvm
    
    # 编译
    # Make sure to include the new classfile package
     javac -d out/tiny-jvm src/main/java/tinyjvm/**/*.java     
    ```

2.  运行你的 TinyJVM：
    ```sh
    java -cp out/tiny-jvm tinyjvm.Main -cp test-classes a.b.c.SimpleTest
    ```

### 预期输出

如果一切顺利，你应该会看到类似下面的输出：

```
Starting TinyJVM...
classpath: test-classes | mainClass: a.b.c.SimpleTest | args: null
[Classpath] Found class: a.b.c.SimpleTest at test-classes/a/b/c/SimpleTest.class
Main class loaded successfully! Bytecode length: 300  <-- (这个数字可能会因编译器版本而异)
Magic number: CA FE BA BE 00 00 00 34 00 10       <-- (前4个字节总是 CAFEBABE)
```

### 阶段一完成！

恭喜！你已经完成了 TinyJVM 的第一步。现在，你拥有了一个可以：
*   接收和解析命令行参数的**启动器**。
*   根据类路径在文件系统中定位并加载 `.class` 文件的**类加载器**的雏形。
*   成功将 `SimpleTest.class` 文件的原始二进制数据读入了内存中的一个 `byte[]` 数组。

这个 `byte[]` 就是下一阶段——**“阶段二：解析 Class 文件”**——的输入。你已经为后续更激动人心的工作打下了坚实的基础。