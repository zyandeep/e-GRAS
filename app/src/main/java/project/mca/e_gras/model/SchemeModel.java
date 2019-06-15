package project.mca.e_gras.model;

import com.google.gson.annotations.SerializedName;

public class SchemeModel {

    @SerializedName("SCHEME_CODE")
    private String hoa;

    @SerializedName("SCHEME_NAME")
    private String name;

    private int amount = 0;

    public SchemeModel() {
    }

    public String getHoa() {
        return hoa;
    }

    public void setHoa(String hoa) {
        this.hoa = hoa;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }


    @Override
    public String toString() {
        return "SchemeModel{" +
                "hoa='" + hoa + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                '}';
    }
}
