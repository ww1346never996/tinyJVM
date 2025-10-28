package tinyjvm.classfile;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class ClassReader {

    private DataInputStream dis;

    public ClassReader(byte[] classData) {
        // Wrap the byte array in streams for easy reading
        this.dis = new DataInputStream(new ByteArrayInputStream(classData));
    }

    // Read an unsigned 8-bit integer (u1)
    public int readU1() {
        try {
            return dis.readUnsignedByte();
        } catch (IOException e) {
            throw new ClassFormatError("Failed to read U1");
        }
    }

    // Read an unsigned 16-bit integer (u2)
    public int readU2() {
        try {
            return dis.readUnsignedShort();
        } catch (IOException e) {
            throw new ClassFormatError("Failed to read U2");
        }
    }

    // Read a signed 32-bit integer (used for u4 as well)
    // Java doesn't have an unsigned int, but readInt() reads 4 bytes, which is what we need.
    // For values > 2^31-1, it will be negative, but for offsets and counts, this is fine.
    public int readU4() {
        try {
            return dis.readInt();
        } catch (IOException e) {
            throw new ClassFormatError("Failed to read U4");
        }
    }

    // Read a specified number of bytes
    public byte[] readBytes(int length) {
        try {
            byte[] bytes=  new byte[length];
            dis.readFully(bytes);
            return bytes;
        } catch (IOException e) {
            throw new ClassFormatError("Failed to read bytes");
        }
    }

    // A helper for reading arrays like interfaces, fields, methods
    public int[] readU2Array() {
        int count = readU2();
        int[] array = new int[count];
        for (int i = 0; i < count; i++) {
            array[i] = readU2();
        }
        return array;
    }
}

