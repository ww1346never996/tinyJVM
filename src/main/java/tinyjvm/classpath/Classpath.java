package tinyjvm.classpath;

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