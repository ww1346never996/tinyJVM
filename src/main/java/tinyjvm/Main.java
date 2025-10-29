//package tinyjvm;
//
//import tinyjvm.classfile.ClassFile;
//import tinyjvm.classpath.Classpath;
//
//import java.io.IOException;
//import java.util.Arrays;
//
//public class Main {
//    public static void main(String[] args) {
//        System.out.println("Starting TinyJVM...");
//        Cmd cmd = Cmd.parse(args);
//        if (cmd == null) {
//            System.out.println("Usage: java -cp <classpath> tinyjvm.Main [-cp classpath] main_class [args...]");
//            return;
//        }
//
//        System.out.printf("classpath: %s | mainClass: %s | args: %s\n",
//                cmd.getClasspath(), cmd.getMainClass(), Arrays.toString(cmd.getAppArgs()));
//
//        // --- 这是关键的修改部分 ---
//        try {
//            Classpath cp = new Classpath(cmd.getClasspath());
//            // 将类名中的 '.' 替换为 '/'
//            String className = cmd.getMainClass().replace('.', '/');
//            byte[] classData = cp.readClass(className);
//
//            if (classData != null) {
//                // 调用我们新的解析方法！
//                ClassFile.parse(classData);
//            } else {
//                System.out.println("Could not find or load main class " + cmd.getMainClass());
//            }
//        } catch (Exception e) {
//            System.err.println("Error occurred while loading class:");
//            e.printStackTrace();
//        }
//    }
//}