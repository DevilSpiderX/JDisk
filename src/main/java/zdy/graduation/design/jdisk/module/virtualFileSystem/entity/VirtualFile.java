package zdy.graduation.design.jdisk.module.virtualFileSystem.entity;

import java.io.Serial;
import java.util.Date;
import java.io.Serializable;

public class VirtualFile implements Serializable {
    @Serial
    private static final long serialVersionUID = -32414638187832287L;

    private Integer id;
    /**
     * 文件名
     */
    private String name;
    /**
     * F代表文件，D代表目录
     */
    private String type;
    /**
     * 文件的路径
     */
    private String path;
    /**
     * 文件所在的目录
     */
    private String directoryPath;
    /**
     * 文件的最后修改日期
     */
    private Date modified;
    /**
     * 文件的大小，目录的大小为null
     */
    private Long size;
    /**
     * 文件所在的驱动器ID
     */
    private Integer driverId;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public void setDriverId(Integer driverId) {
        this.driverId = driverId;
    }

    @Override
    public String toString() {
        return "VirtualFile{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", path='" + path + '\'' +
                ", directoryPath='" + directoryPath + '\'' +
                ", modified=" + modified +
                ", size=" + size +
                ", driverId=" + driverId +
                '}';
    }
}
