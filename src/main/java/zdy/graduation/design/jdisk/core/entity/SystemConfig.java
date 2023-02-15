package zdy.graduation.design.jdisk.core.entity;

import java.io.Serial;
import java.io.Serializable;

public class SystemConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = -97110881458209771L;

    private Integer id;

    private String key;

    private String value;

    private String remark;

    public SystemConfig() {
    }

    public SystemConfig(String key) {
        this.key = key;
    }

    public SystemConfig(String key, String value, String remark) {
        this.key = key;
        this.value = value;
        this.remark = remark;
    }

    public SystemConfig(Integer id, String key, String value, String remark) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.remark = remark;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "SystemConfig{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}

