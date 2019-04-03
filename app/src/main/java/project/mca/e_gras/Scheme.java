package project.mca.e_gras;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Scheme {
    private String acNo;
    private String name;
    private int amount;

    public Scheme(String acNo, String name, int amount) {
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

    @Override
    public String toString() {
        return "Scheme{" +
                "acNo='" + acNo + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                '}';
    }


    // generate list of schemes
    public static List<Scheme> getSchemes() {
        List<Scheme> schemeList = new ArrayList<>();

        int noOfObjects =  (new Random().nextInt(10)) + 1;

        for (int i = 1; i <= noOfObjects; i++) {
            schemeList.add(new Scheme("6003-00-105-0000-000" + String.valueOf(i),
                    "Scheme " + i,
                    100 * noOfObjects));
        }

        return schemeList;
    }
}
