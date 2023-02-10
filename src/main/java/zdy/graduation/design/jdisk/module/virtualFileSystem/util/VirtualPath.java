package zdy.graduation.design.jdisk.module.virtualFileSystem.util;

public class VirtualPath {
    public static String get(String first, String... more) {
        if (first == null) return "";
        if (more.length == 0) {
            return first;
        }
        StringBuilder sb = new StringBuilder(first);
        if (!first.equals("/")) {
            sb.append("/");
        }
        for (String path : more) {
            if (!path.isEmpty()) {
                sb.append(path).append("/");
            }
        }
        return sb.substring(0, sb.length() - 1);
    }
}
