import bitcask.Bitcask;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static class TestInt implements Externalizable {
        @Serial
        private static final long serialVersionUID = 1L;
        int value;

        public TestInt() {}

        public TestInt(int value) {
            this.value = value;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(value);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            this.value = in.readInt();
        }

        @Override
        public int hashCode() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestInt testInt)) return false;
            return value == testInt.value;
        }
    }

    public static void main(String[] args) throws Exception {
        Bitcask<TestInt, TestInt> bitcask = new Bitcask<>();
        Map<String, Integer> opts = new HashMap<>();
        opts.put("readWrite", 1);
        opts.put("syncOnPut", 1);
        opts.put("maxFileSize", 1);
        var bitcaskHandle = bitcask.open("", opts);
        /*for (int i = 0; i < 10000; ++i) {
            bitcask.put(bitcaskHandle, new TestInt(i), new TestInt(i));
        }*/
        /*for (int i = 0; i < 1000; ++i) {
            bitcask.put(bitcaskHandle, new TestInt(i), new TestInt(i+10000));
        }*/
        for (int i = 0; i < 100; ++i) {
            bitcask.put(bitcaskHandle, new TestInt(i), new TestInt(i+11000));
        }
        bitcask.merge(bitcaskHandle);
        int rand = bitcask.get(bitcaskHandle, new TestInt(199)).value;
        System.out.println(rand);
        bitcask.close(bitcaskHandle);
    }
}