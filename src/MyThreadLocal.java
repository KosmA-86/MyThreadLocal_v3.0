import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class MyThreadLocal<T> {

    private MyThreadLocalSingleton<Object> myThreadLocalSingleton;

    public MyThreadLocal() {
        this.myThreadLocalSingleton = MyThreadLocalSingleton.getInstance();
    }

    public T get() {
        return (T) myThreadLocalSingleton.get();
    }

    public void set(T value) {
        myThreadLocalSingleton.set(value);
    }

    public static class MyThreadLocalSingleton<T> {

        private static volatile MyThreadLocalSingleton<Object> instance;
        private static final Map<Thread, Object> map = new WeakHashMap<>();;
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

        public T get() {
            r.lock();
            try {
                T value = null;
                Thread t = Thread.currentThread();
                //int i = t.getName().hashCode();
                Object result = map.get(t);
                if (result != null) {
                    value = (T) result;
                }
                return value;
            } finally {
                r.unlock();
            }
        }

        public void set(T value) {
            w.lock();
            try {
                Thread t = Thread.currentThread();
                //int i = t.getName().hashCode();
                map.put(t, value);
            } finally {
                w.unlock();
            }
        }

        public void remove() {
            w.lock();
            try {
                Thread t = Thread.currentThread();
                //int i = t.getName().hashCode();
                map.remove(t);
            } finally {
                w.unlock();
            }
        }
    }
}