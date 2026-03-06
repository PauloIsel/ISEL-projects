// Dummy.java
package pt.isel.mpd;

abstract class Dummy {
    private static String data;

    public static String getData() {
        return data;
    }

    public void setData(String data) {
        Dummy.data = data;
    }

    public abstract void methods();
}