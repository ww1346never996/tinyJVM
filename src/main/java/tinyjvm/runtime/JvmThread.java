package tinyjvm.runtime;

import java.util.Stack;

public class JvmThread {
    private int pc;
    private final Stack<Frame> stack;

    public JvmThread() {
        this.stack = new Stack<>();
    }

    public int getPC() { return pc; }
    public void setPC(int pc) { this.pc = pc; }

    public void pushFrame(Frame frame) {
        stack.push(frame);
    }

    public Frame popFrame() {
        return stack.pop();
    }

    public Frame currentFrame() {
        return stack.peek();
    }

    public boolean isStackEmpty() {
        return stack.isEmpty();
    }

}
