package project.mca.e_gras.model;

import com.google.gson.annotations.SerializedName;

public class PaymentModel {

    @SerializedName("NAME")
    private String name;

    @SerializedName("PAYMENT_TYPE")
    private String type;

    public PaymentModel() {
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

    @Override
    public String toString() {
        return "PaymentModel{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}