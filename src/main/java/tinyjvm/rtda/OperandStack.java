package tinyjvm.rtda;

public class OperandStack {
    private final Object[] slots;
    private int top = 0; // 栈顶指针

    public OperandStack(int maxStack) {
        if (maxStack > 0) {
            slots = new Object[maxStack];
        } else {
            slots = null;
        }
    }

    // --- 核心压栈 (Push) 方法 ---
    public void pushInt(int val) {
        slots[top++] = val;
    }

    public void pushRef(Object ref) {
        slots[top++] = ref;
    }

    // --- 核心弹栈 (Pop) 方法 ---
    public int popInt() {
        top--;
        return (int) slots[top];
    }

    public Object popRef() {
        top--;
        return slots[top];
    }

    // ---- 方便调试 ----
    @Override
    public String toString() {
        // 实现一个漂亮的打印方法，显示栈中内容
        StringBuilder sb = new StringBuilder();
        sb.append("Stack (top=").append(top).append("): [");
        for (int i = 0; i < top; i++) {
            sb.append(slots[i]);
            if (i < top - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    // 未来可以添加 pushLong, popLong 等

}
