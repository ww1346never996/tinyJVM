package tinyjvm.runtime;

import tinyjvm.classfile.ClassFile;
import tinyjvm.classpath.Classpath;

import java.util.HashMap;
import java.util.Map;

public class ClassLoader {

    private final Classpath classpath;
    private final Map<String, ClassFile> classMap;

    public ClassLoader(Classpath classpath) {
        this.classpath = classpath;
        this.classMap = new HashMap<>();
    }

    public ClassFile loadClass(String className) {
        // 1. 检查类是否已经加载
        if (classMap.containsKey(className)) {
            return classMap.get(className); // 直接从缓存（方法区）返回
        }

        return loadNonArrayClass(className);
    }

    private ClassFile loadNonArrayClass(String className) {
        try {
            // 2. 委托 Classpath 读取字节码
            byte[] data = classpath.readClass(className);
            if (data == null) {
                throw new ClassNotFoundException(className);
            }

            // 3. 解析字节码，生成 ClassFile 对象
            ClassFile classFile = ClassFile.parse(data);

            // 4. 放入缓存（方法区）
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
