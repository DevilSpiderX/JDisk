package zdy.graduation.design.jdisk.module.virtualFileSystem.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualDriver;
import zdy.graduation.design.jdisk.module.virtualFileSystem.entity.VirtualFile;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class FileTree implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(FileTree.class);
    @Serial
    private static final long serialVersionUID = 7881491649828258837L;
    private final VirtualFile file;
    private final VirtualDriver driver;
    private final boolean directory;
    private final List<FileTree> children;

    public FileTree(@NotNull VirtualFile file, @NotNull VirtualDriver driver) {
        this.file = file;
        this.driver = driver;
        this.directory = Objects.equals("D", file.getType());
        this.children = this.directory ? new ArrayList<>() : null;
    }

    public VirtualFile getFile() {
        return file;
    }

    public VirtualDriver getDriver() {
        return driver;
    }

    public boolean isDirectory() {
        return directory;
    }

    public List<FileTree> getChildren() {
        return children;
    }

    public void append(FileTree node) {
        if (directory) {
            children.add(node);
        }
    }

    @Override
    public String toString() {
        return "FileTree{" +
                "path=" + file.getPath() +
                ", driverID=" + driver.getId() +
                ", driverPath=" + driver.getPath() +
                ", directory=" + directory +
                (directory ? (", childrenCount=" + children.size()) : "") +
                '}';
    }

    public static FileTree scanDriver(@NotNull VirtualDriver driver) {
        //创建一个树根
        VirtualFile root = new VirtualFile();
        root.setPath("/");
        root.setType("D");
        FileTree result = new FileTree(root, driver);
        scanNode(result);
        return result;
    }

    public static void scanNode(@NotNull FileTree node) {
        VirtualFile currentVirtualFile = node.getFile();
        VirtualDriver driver = node.getDriver();
        Path currentPath = Paths.get(driver.getPath(), currentVirtualFile.getPath());
        File currentFile = currentPath.toFile();
        if (currentFile.isDirectory()) {
            List<FileTree> currentChildren = node.getChildren();
            File[] childFiles = currentFile.listFiles();
            if (childFiles != null) {
                for (File childFile : childFiles) {
                    String name = childFile.getName();
                    String childVirtualPath = VirtualPath.get(currentVirtualFile.getPath(), name);
                    boolean childIsDir = childFile.isDirectory();

                    VirtualFile childVirtualFile = new VirtualFile();
                    childVirtualFile.setPath(childVirtualPath);
                    childVirtualFile.setDriverId(driver.getId());
                    childVirtualFile.setName(name);
                    childVirtualFile.setType(childIsDir ? "D" : "F");
                    childVirtualFile.setParent(currentVirtualFile.getPath());
                    childVirtualFile.setModified(new Date(childFile.lastModified()));
                    childVirtualFile.setSize(childIsDir ? null : childFile.length());

                    FileTree childNode = new FileTree(childVirtualFile, driver);
                    currentChildren.add(childNode);

                    if (childIsDir) {
                        scanNode(childNode);
                    }
                }
            }
        }
    }
}
