package tinyjvm.classpath;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DirEntry implements Entry {
    private final Path absolutePath;

    public DirEntry(String path) {
        this.absolutePath = Paths.get(path).toAbsolutePath();
    }

    @Override
    public byte[] readClass(String className) throws Exception {
        Path classPath = absolutePath.resolve(className);
        if (Files.exists(classPath) && Files.isRegularFile(classPath)) {
            return Files.readAllBytes(classPath);
        }
        // 为了和 CompositeEntry 协同工作，这里找不到就抛出异常，让上层去尝试下一个 Entry
        throw new Exception("Class not found in directory: " + className);
    }

    @Override
    public String toString() {
        return absolutePath.toString();
    }
}