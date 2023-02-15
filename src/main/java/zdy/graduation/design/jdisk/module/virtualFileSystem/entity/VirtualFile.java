package zdy.graduation.design.jdisk.module.virtualFileSystem.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

public class VirtualFile implements Serializable {
    @Serial
    private static final long serialVersionUID = -18015153136007934L;
    /**
     * 文件的路径
     */
    private String path;
    /**
     * 文件所在的驱动器ID
     */
    private Integer driverId;
    /**
     * 文件名
     */
    private String name;
    /**
     * F代表文件，D代表目录
     */
    private String type;
    /**
     * 文件所在的目录
     */
    private String parent;
    /**
     * 文件的最后修改日期
     */
    private Date modified;
    /**
     * 文件的大小，目录的大小为null
     */
    private Long size;


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public void setDriverId(Integer driverId) {
        this.driverId = driverId;
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

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
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

    @Override
    public String toString() {
        return "VirtualFile{" +
                "path='" + path + '\'' +
                ", driverId=" + driverId +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", directoryPath='" + parent + '\'' +
                ", modified=" + modified +
                ", size=" + size +
                '}';
    }
}
