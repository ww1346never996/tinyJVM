package tinyjvm.rtda;

public class LocalVars {
    private final Object[] slots;

    public LocalVars(int maxLocals) {
        if (maxLocals > 0) {
            slots = new Object[maxLocals];
        } else {
            slots = null; // 或者 new Object[0] 避免空指针
        }
    }

    // --- 核心方法 ---

    public void setInt(int index, int val) {
        slots[index] = val;
    }

    public int getInt(int index) {
        return (int) slots[index];
    }

    public void setRef(int index, Object ref) {
        slots[index] = ref;
    }

    public Object getRef(int index) {
        return slots[index];
    }

    // ---- 方便调试 ----
    @Override
    public String toString() {
        // 实现一个漂亮的打印方法，方便调试
        return java.util.Arrays.toString(slots);
    }

    // 未来可以添加 setLong, getLong, setDouble, getDouble 等方法
    // 注意：long/double 需要占用两个索引 (index 和 index+1)
}
