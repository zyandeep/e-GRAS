package project.mca.e_gras.model;

import com.google.gson.annotations.SerializedName;


public class DistrictModel {

    @SerializedName("NAME")
    private String name;

    @SerializedName("DISTRICT_CODE")
    private int districtCode;

    public DistrictModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(int districtCode) {
        this.districtCode = districtCode;
    }

    @Override
    public String toString() {
        return "DistrictModel{" +
                "name='" + name + '\'' +
                ", districtCode=" + districtCode +
                '}';
    }
}
