package org._2333.cyls;

/**
 * @author 2333
 */
public class Main {

    private static Cyls client;

    static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            // do nothing
        }
    }

    public static void main(String[] args) {
        client = new Cyls();
        client.start();
    }
}
