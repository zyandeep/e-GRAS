package project.mca.e_gras.model;

import com.google.gson.annotations.SerializedName;

public class TransactionModel {

    @SerializedName("ID")
    private int id;

    @SerializedName("NAME")
    private String name;

    @SerializedName("GRNNO")
    private String grn_no;

    @SerializedName("CHALLAN_DATE")
    private String challan_date;

    @SerializedName("AMOUNT")
    private int amount;

    @SerializedName("STATUS")
    private String status;

    @SerializedName("MOP")
    private String mop;


    public TransactionModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMop() {
        return mop;
    }

    public void setMop(String mop) {
        this.mop = mop;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGrn_no() {
        return grn_no;
    }

    public void setGrn_no(String grn_no) {
        this.grn_no = grn_no;
    }

    public String getChallan_date() {
        return challan_date;
    }

    public void setChallan_date(String challan_date) {
        this.challan_date = challan_date;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TransactionModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", grn_no='" + grn_no + '\'' +
                ", challan_date='" + challan_date + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", mop='" + mop + '\'' +
                '}';
    }
}
