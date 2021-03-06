
public class TestMyThreadLocal {

    public static void main(String[] args) throws InterruptedException {

        MyThreadLocal<String> myThL = new MyThreadLocal<>();
        MyThreadLocal<String> myThL1 = new MyThreadLocal<>();
        MyThreadLocal<String> myThL2 = new MyThreadLocal<>();
        myThL.set(Thread.currentThread().getName());
        class MyRunnable implements Runnable {
            @Override
            public void run() {
                myThL.set(Thread.currentThread().getName());
                myThL2.set(Thread.currentThread().getName() + " myThL2");
                myThL1.set(Thread.currentThread().getName() + " myThL1");

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + " myThL2 value: " + myThL2.get());
                System.out.println(Thread.currentThread().getName() + " value: " + myThL.get());
                System.out.println(Thread.currentThread().getName() + " myThL1 value: " + myThL1.get());

            }
        }

        /////////////////////////////////////////////////////////////////////////////////////////
        Thread t1 = new Thread(new MyRunnable());
        Thread t2 = new Thread(new MyRunnable());
        Thread t3 = new Thread(new MyRunnable());
        Thread t4 = new Thread(new MyRunnable());
        Thread t5 = new Thread(new MyRunnable());
        Thread t6 = new Thread(new MyRunnable());
        Thread t7 = new Thread(new MyRunnable());
        Thread t8 = new Thread(new MyRunnable());
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
        t7.start();
        t8.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();
        t6.join();
        t7.join();
        t8.join();
        System.out.println(myThL.get());
        myThL.remove();
        System.out.println(myThL.get());
    }
}
