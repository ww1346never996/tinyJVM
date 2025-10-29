package tinyjvm.classpath;

import java.io.File;

public interface Entry {
    /**
     * 读取类文件内容
     * @param className 相对路径的类文件名，例如 a/b/c/SimpleTest.class
     * @return 字节数组
     * @throws Exception 如果找不到或读取失败
     */
    byte[] readClass(String className) throws Exception;

    // 静态工厂方法，根据路径字符串创建不同类型的 Entry
    static Entry create(String path) {
        if (path.contains(File.pathSeparator)) { // e.g., "path/a:path/b"
            return new CompositeEntry(path);
        }
        if (path.endsWith("*")) { // e.g., "path/to/jars/*"
            return new WildcardEntry(path);
        }
        if (path.endsWith(".jar") || path.endsWith(".JAR") || path.endsWith(".zip") || path.endsWith(".ZIP")) {
            return new ZipEntry(path);
        }
        return new DirEntry(path); // 默认是目录
    }
}

