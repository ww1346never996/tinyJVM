package tinyjvm;

import java.util.Arrays;

public class Cmd {

    private boolean helpFlag;
    private boolean versionFlag;
    private String classpath;
    private String mainClass;
    private String[] appArgs; // 传递给目标Java程序的参数

    private Cmd() {
    }

    public static Cmd parse(String[] args) {
        Cmd cmd = new Cmd();
        // 我们假设参数格式是固定的，不做复杂的动态解析
        // 例如：java [-options] class [args...]
        if (args == null || args.length == 0) {
            cmd.helpFlag = true;
            return cmd;
        }

        // 查找 -cp 或 -classpath
        int cpIndex = -1;
        for (int i = 0; i < args.length; i++) {
            if ("-cp".equals(args[i]) || "-classpath".equals(args[i])) {
                cpIndex = i;
                break;
            }
        }

        if (cpIndex != -1) {
            // -cp 后面必须跟一个路径
            if (cpIndex + 1 < args.length) {
                cmd.classpath = args[cpIndex + 1];
                // 移除 -cp 和它的值，剩下的就是主类和程序参数
                String[] remainingArgs = new String[args.length - 2];
                System.arraycopy(args, 0, remainingArgs, 0, cpIndex);
                System.arraycopy(args, cpIndex + 2, remainingArgs, cpIndex, args.length - (cpIndex + 2));
                args = remainingArgs;
            } else {
                System.err.println("Error: Missing classpath value after -cp/-classpath");
                cmd.helpFlag = true;
                return cmd;
            }
        }

        // 此时的 args 应该只剩下主类和程序参数了
        if (args.length > 0) {
            // 处理 -help 和 -version
            if ("-help".equals(args[0]) || "-?".equals(args[0])) {
                cmd.helpFlag = true;
                return cmd;
            }
            if ("-version".equals(args[0])) {
                cmd.versionFlag = true;
                return cmd;
            }

            cmd.mainClass = args[0];
            if (args.length > 1) {
                cmd.appArgs = Arrays.copyOfRange(args, 1, args.length);
            }
        } else {
            // 如果移除了 -cp 后没有参数了，也显示帮助
            cmd.helpFlag = true;
        }

        // 如果没有指定-cp，默认使用当前目录
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
}