package tinyjvm.classpath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class WildcardEntry extends CompositeEntry {

    public WildcardEntry(String path) {
        // 调用父类的空构造函数（如果 CompositeEntry 提供了的话）
        // 或者直接在这里初始化内部的 entries 列表
        // WildcardEntry 的逻辑是把 `path` 中的 '*' 替换为所有 jar 文件
        super(expandWildcardPath(path));
    }

    private static String expandWildcardPath(String path) {
        // 去掉末尾的 '*'
        String baseDirStr = path.substring(0, path.length() - 1);
        Path baseDir = Paths.get(baseDirStr);

        if (!Files.isDirectory(baseDir)) {
            return ""; // 如果基本目录不存在或不是目录，返回空路径
        }

        try {
            // 使用 Stream API 查找所有 .jar 文件，并将它们的绝对路径用路径分隔符连接起来
            return Files.list(baseDir)
                    .filter(p -> !Files.isDirectory(p))
                    .map(Path::toString)
                    .filter(s -> s.toLowerCase().endsWith(".jar"))
                    .collect(Collectors.joining(File.pathSeparator));
        } catch (IOException e) {
            // 发生 IO 异常，返回空路径
            e.printStackTrace();
            return "";
        }
    }
}