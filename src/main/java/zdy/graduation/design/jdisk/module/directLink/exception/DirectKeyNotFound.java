package zdy.graduation.design.jdisk.module.directLink.exception;

public class DirectKeyNotFound extends Exception {
    private final String key;

    public DirectKeyNotFound(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
