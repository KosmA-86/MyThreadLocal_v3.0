import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class MyThreadLocal<T> {

    private final int threadLocalHashCode = nextHashCode();
    private static final AtomicInteger nextHashCode = new AtomicInteger();
    private static final int HASH_INCREMENT = 0x61c88647;

    private static int nextHashCode() {
        return nextHashCode.getAndAdd(HASH_INCREMENT);
    }

    private MyThreadLocalSingleton<Object> myThreadLocalSingleton;

    public MyThreadLocal() {
        this.myThreadLocalSingleton = MyThreadLocalSingleton.getInstance();
    }

    public T get() {
        return (T) myThreadLocalSingleton.get(this);
    }

    public void set(T value) {
        myThreadLocalSingleton.set(value, this);
    }

    public void remove() {
        myThreadLocalSingleton.remove(this);
    }

    public static class MyThreadLocalSingleton<T> {

        private static final int INITIAL_CAPACITY = 16;
        private static volatile MyThreadLocalSingleton<Object> instance;
        private static final Map<Thread, Entry[]> map = new WeakHashMap<>();
        private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        private static final Lock r = rwl.readLock();
        private static final Lock w = rwl.writeLock();

        private MyThreadLocalSingleton() {
        }

        private static MyThreadLocalSingleton<Object> getInstance() {
            MyThreadLocalSingleton<Object> result = instance;
            if (result != null) {
                return result;
            }
            synchronized (MyThreadLocalSingleton.class) {
                if (instance == null) {
                    instance = new MyThreadLocalSingleton<>();
                }
                return instance;
            }
        }

        public T get(MyThreadLocal<?> myThreadLocal) {
            r.lock();
            try {
                T value = null;
                Thread t = Thread.currentThread();
                Entry[] result = map.get(t);
                if (result != null) {
                    int i = myThreadLocal.threadLocalHashCode & (result.length - 1);
                    Entry e = result[i];
                    if (e != null && e.get() == myThreadLocal) {
                        value = (T) result[i].value;
                    } else
                        return getEntryAfterMiss(myThreadLocal, i, e, result);
                }
                return value;
            } finally {
                r.unlock();
            }
        }

        private T getEntryAfterMiss(MyThreadLocal<?> key, int i, Entry e, Entry[] table) {
            int len = table.length;
            while (e != null) {
                MyThreadLocal<?> k = e.get();
                if (k == key)
                    return (T) e.value;
                else
                    i = nextIndex(i, len);
                e = table[i];
            }
            return null;
        }

        public void set(T value, MyThreadLocal<?> myThreadLocal) {
            w.lock();
            try {
                Thread t = Thread.currentThread();
                int len;
                int i;
                Entry[] tab = map.get(t);
                if (tab != null) {
                    len = tab.length;
                    i = myThreadLocal.threadLocalHashCode & (len - 1);
                    for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
                        MyThreadLocal<?> k = e.get();

                        if (k == myThreadLocal) {
                            e.value = value;
                            return;
                        }
                    }
                    int count = 0;
                    for (int j = 0; j < len; j++) {
                        if (tab[j] != null) {
                            count++;
                        }
                    }
                    if (count >= len * 2 / 3) {
                        tab = resize(tab);
                        i = myThreadLocal.threadLocalHashCode & (tab.length - 1);
                    }
                    tab[i] = new Entry(myThreadLocal, value);
                    map.put(t, tab);
                } else {
                    tab = new Entry[INITIAL_CAPACITY];
                    i = myThreadLocal.threadLocalHashCode & (INITIAL_CAPACITY - 1);
                    tab[i] = new Entry(myThreadLocal, value);
                    map.put(t, tab);
                }
            } finally {
                w.unlock();
            }
        }

        public void remove(MyThreadLocal<?> myThreadLocal) {
            w.lock();
            try {
                Thread t = Thread.currentThread();
                Entry[] tab = map.get(t);
                int len = tab.length;
                int i = myThreadLocal.threadLocalHashCode & (len - 1);
                for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
                    if (e.get() == myThreadLocal) {
                        e.clear();
                    }
                }
                map.remove(t);
            } finally {
                w.unlock();
            }
        }

        private static int nextIndex(int i, int len) {
            return ((i + 1 < len) ? i + 1 : 0);
        }

        private Entry[] resize(Entry[] table) {
            int oldLen = table.length;
            int newLen = oldLen * 2;
            Entry[] newTab = new Entry[newLen];
            for (Entry e : table) {
                if (e != null) {
                    MyThreadLocal<?> k = e.get();
                    if (k == null) {
                        e.value = null;
                    } else {
                        int h = k.threadLocalHashCode & (newLen - 1);
                        while (newTab[h] != null)
                            h = nextIndex(h, newLen);
                        newTab[h] = e;
                    }
                }
            }
            return newTab;
        }

        static class Entry extends WeakReference<MyThreadLocal<?>> {
            Object value;

            Entry(MyThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }
    }
}