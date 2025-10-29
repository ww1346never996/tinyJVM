package tinyjvm.classpath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Classpath {
    private final Entry bootClasspath; // 启动类路径 (jre/lib/*)
    private final Entry userClasspath; // 用户类路径 (-cp)

    public Classpath(String jreOption, String cpOption) {
        // 1. 解析启动类路径
        this.bootClasspath = parseBootClasspath(jreOption);
        // 2. 解析用户类路径
        this.userClasspath = parseUserClasspath(cpOption);
    }

    /**
     * 按顺序搜索 class 文件：
     * 1. 首先在启动类路径 (boot) 中查找。
     * 2. 如果找不到，再在用户类路径 (user) 中查找。
     * @param className 类的全限定名，格式为 "java/lang/Object"
     * @return 类的字节码数据
     * @throws Exception 如果找不到类
     */
    public byte[] readClass(String className) throws Exception {
        className = className + ".class";

        try {
            // 先尝试从 boot classpath 读取
            return bootClasspath.readClass(className);
        } catch (Exception e) {
            // boot 中找不到，再尝试从 user classpath 读取
            if (userClasspath != null) {
                return userClasspath.readClass(className);
            }
        }

        throw new ClassNotFoundException("Class not found: " + className);
    }

    private Entry parseBootClasspath(String jreOption) {
        String jreDir = getJreDir(jreOption);
        // jre/lib/*
        String jreLibPath = Paths.get(jreDir, "lib", "*").toString();
        return new WildcardEntry(jreLibPath);
    }

    private Entry parseUserClasspath(String cpOption) {
        if (cpOption == null || cpOption.isEmpty()) {
            // 如果用户没有提供 -cp，默认使用当前目录 "."
            cpOption = ".";
        }
        return Entry.create(cpOption);
    }

    private String getJreDir(String jreOption) {
        if (jreOption != null && Files.exists(Paths.get(jreOption))) {
            return jreOption;
        }
        // 如果用户没提供，尝试在当前目录下找 jre
        if (Files.exists(Paths.get("./jre"))) {
            return "./jre";
        }
        // 最后尝试从 JAVA_HOME 环境变量找
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome != null) {
            return Paths.get(javaHome, "jre").toString();
        }
        throw new RuntimeException("Can not find JRE folder!");
    }
}