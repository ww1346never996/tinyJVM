package tinyjvm;

import java.util.Arrays;

public class Cmd {

    private boolean helpFlag;
    private boolean versionFlag;
    private String classpath;
    private String mainClass;
    private String[] appArgs; // 传递给目标Java程序的参数
    private String xjreOption;

    private Cmd() {
    }

    public static Cmd parse(String[] args) {
        Cmd cmd = new Cmd();
        if (args == null || args.length == 0) {
            cmd.helpFlag = true;
            return cmd;
        }

        int i = 0;
        while (i < args.length) {
            String arg = args[i];
            switch (arg) {
                case "-help":
                case "-?":
                    cmd.helpFlag = true;
                    return cmd;
                case "-version":
                    cmd.versionFlag = true;
                    return cmd;
                case "-Xjre":
                    if (i + 1 < args.length) {
                        cmd.xjreOption = args[i + 1];
                        i += 2;
                    } else {
                        System.err.println("Error: Missing Xjre value after -Xjre");
                        cmd.helpFlag = true;
                        return cmd;
                    }
                    break;
                case "-cp":
                case "-classpath":
                    if (i + 1 < args.length) {
                        cmd.classpath = args[i + 1];
                        i += 2;
                    } else {
                        System.err.println("Error: Missing classpath value after -cp/-classpath");
                        cmd.helpFlag = true;
                        return cmd;
                    }
                    break;
                default:
                    // 第一个不是JVM选项的参数就是主类名
                    cmd.mainClass = arg;
                    if (i + 1 < args.length) {
                        cmd.appArgs = Arrays.copyOfRange(args, i + 1, args.length);
                    }
                    i = args.length; // 跳出循环
                    break;
            }
        }

        if (cmd.mainClass == null) {
            cmd.helpFlag = true;
        }
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
    public String getXjreOption() { return xjreOption; }
    public String getCpOption() { return classpath; }

    public static void printUsage() {
        System.out.println("Usage: java [-options] class [args...]");
        System.out.println("    -cp <dir> or -classpath <dir>   Specify where to find user class files");
        System.out.println("    -Xjre <dir>                     Specify where to find jre");
        System.out.println("    -help or -?                     Print this help message");
        System.out.println("    -version                        Print version and exit");
    }

}