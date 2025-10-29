package tinyjvm.classpath;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CompositeEntry implements Entry {
    private final List<Entry> entries = new ArrayList<>();

    public CompositeEntry(String pathList) {
        String[] paths = pathList.split(File.pathSeparator);
        for (String path : paths) {
            if (!path.isEmpty()) {
                entries.add(Entry.create(path));
            }
        }
    }

    @Override
    public byte[] readClass(String className) throws Exception {
        for (Entry entry : entries) {
            try {
                // 依次尝试从每个子条目中读取
                return entry.readClass(className);
            } catch (Exception e) {
                // 忽略异常，继续尝试下一个条目
            }
        }
        // 所有子条目都找不到
        throw new Exception("Class not found in composite path: " + className);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            sb.append(entries.get(i).toString());
            if (i < entries.size() - 1) {
                sb.append(File.pathSeparator);
            }
        }
        return sb.toString();
    }
}
