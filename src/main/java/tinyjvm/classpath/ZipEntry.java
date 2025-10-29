package tinyjvm.classpath;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipFile;

public class ZipEntry implements Entry {
    private final Path absolutePath;

    public ZipEntry(String path) {
        this.absolutePath = Paths.get(path).toAbsolutePath();
    }

    @Override
    public byte[] readClass(String className) throws Exception {
        if (!Files.exists(absolutePath)) {
            throw new Exception("JAR/ZIP file not found: " + absolutePath);
        }

        try (ZipFile zipFile = new ZipFile(absolutePath.toFile())) {
            java.util.zip.ZipEntry entry = zipFile.getEntry(className);
            if (entry != null) {
                try (InputStream is = zipFile.getInputStream(entry)) {
                    // 使用 Java 9+ 的 InputStream.readAllBytes()
                    return is.readAllBytes();
                }
            }
        }

        throw new Exception("Class not found in ZIP/JAR: " + className);
    }

    @Override
    public String toString() {
        return absolutePath.toString();
    }
}
