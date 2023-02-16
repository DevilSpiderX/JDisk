package zdy.graduation.design.jdisk.module.directLink.entity;

import java.io.Serial;
import java.util.Date;
import java.io.Serializable;

public class DirectLink implements Serializable {
    @Serial
    private static final long serialVersionUID = -59866042078362790L;

    private Integer id;
    /**
     * 直链的key
     */
    private String key;
    /**
     * 直链对应文件的路径
     */
    private String path;
    /**
     * 对应驱动器的id
     */
    private Integer driverId;
    /**
     * 直链创建时间
     */
    private Date createDate;

    public DirectLink() {
    }

    public DirectLink(String key) {
        this.key = key;
    }

    public DirectLink(String path, Integer driverId) {
        this.path = path;
        this.driverId = driverId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

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

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "DirectLink{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", path='" + path + '\'' +
                ", driverId=" + driverId +
                ", createDate=" + createDate +
                '}';
    }
}

