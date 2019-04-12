package project.mca.e_gras.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SchemeModel {
    private String acNo;
    private String name;
    private int amount;

    public SchemeModel(String acNo, String name, int amount) {
        this.acNo = acNo;
        this.name = name;
        this.amount = amount;
    }

    public String getAcNo() {
        return acNo;
    }

    public void setAcNo(String acNo) {
        this.acNo = acNo;
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

    // generate list of schemes
    public static List<SchemeModel> getSchemes() {
        List<SchemeModel> schemeList = new ArrayList<>();

        int noOfObjects = (new Random().nextInt(10)) + 1;

        for (int i = 1; i <= noOfObjects; i++) {
            schemeList.add(new SchemeModel("6003-00-105-0000-000" + String.valueOf(i),
                    "SchemeModel " + i,
                    0));
        }

        return schemeList;
    }

    @Override
    public String toString() {
        return "SchemeModel{" +
                "acNo='" + acNo + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                '}';
    }
}
