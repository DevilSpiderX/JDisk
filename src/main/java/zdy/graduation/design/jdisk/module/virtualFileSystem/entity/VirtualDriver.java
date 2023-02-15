package zdy.graduation.design.jdisk.module.virtualFileSystem.entity;

import java.io.Serial;
import java.io.Serializable;

public class VirtualDriver implements Serializable {
    @Serial
    private static final long serialVersionUID = 933071886294066922L;

    private Integer id;
    /**
     * 是否启用
     */
    private Boolean enable;
    /**
     * 虚拟驱动器名
     */
    private String name;
    /**
     * 虚拟驱动器代号，用于url
     */
    private String key;
    /**
     * 虚拟驱动器对应真实文件系统的路径
     */
    private String path;
    /**
     * 备注
     */
    private String remark;
    /**
     * 是否生成签名下载链接
     */
    private Boolean isPrivate;
    /**
     * 下载链接签名存活时间
     */
    private Integer tokenTime;
    /**
     * 是否允许文件操作
     */
    private Boolean enableFileOperator;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Integer getTokenTime() {
        return tokenTime;
    }

    public void setTokenTime(Integer tokenTime) {
        this.tokenTime = tokenTime;
    }

    public Boolean getEnableFileOperator() {
        return enableFileOperator;
    }

    public void setEnableFileOperator(Boolean enableFileOperator) {
        this.enableFileOperator = enableFileOperator;
    }

    @Override
    public String toString() {
        return "VirtualDriver{" +
                "id=" + id +
                ", enable=" + enable +
                ", name='" + name + '\'' +
                ", key='" + key + '\'' +
                ", path='" + path + '\'' +
                ", remark='" + remark + '\'' +
                ", isPrivate=" + isPrivate +
                ", tokenTime=" + tokenTime +
                ", enableFileOperator=" + enableFileOperator +
                '}';
    }
}
