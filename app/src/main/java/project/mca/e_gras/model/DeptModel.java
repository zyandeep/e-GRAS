package project.mca.e_gras.model;

import com.google.gson.annotations.SerializedName;

public class DeptModel {

    @SerializedName("NAME")
    private String name;

    @SerializedName("DEPT_CODE")
    private String deptCode;


    public DeptModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    @Override
    public String toString() {
        return "DeptModel{" +
                "name='" + name + '\'' +
                ", deptCode='" + deptCode + '\'' +
                '}';
    }
}