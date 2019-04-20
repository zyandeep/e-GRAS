package project.mca.e_gras.model;

import com.google.gson.annotations.SerializedName;

public class OfficeModel {

    @SerializedName("NAME")
    private String name;

    @SerializedName("OFFICE_CODE")
    private String officeCode;

    @SerializedName("SRO_CODE")
    private int SroCode;

    public OfficeModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOfficeCode() {
        return officeCode;
    }

    public void setOfficeCode(String officeCode) {
        this.officeCode = officeCode;
    }

    public int getSroCode() {
        return SroCode;
    }

    public void setSroCode(int sroCode) {
        SroCode = sroCode;
    }

    @Override
    public String toString() {
        return "OfficeModel{" +
                "name='" + name + '\'' +
                ", officeCode='" + officeCode + '\'' +
                ", SroCode=" + SroCode +
                '}';
    }
}
