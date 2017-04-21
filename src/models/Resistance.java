package models;

public class Resistance {
    ResistanceType resistanceType;
    int value;

    public Resistance(ResistanceType resistanceType, int value) {
        this.resistanceType = resistanceType;
        this.value = value;
    }

    @Override
    public String toString() {
        return "RESISTANCE{" +
            "resistanceType=" + resistanceType +
            ", value=" + value +
            '}';
    }

    public void add(Resistance resistance){
        if (resistance.resistanceType == resistanceType){
            value+=resistance.value;
        }
    }

    public ResistanceType getResistanceType() {
        return resistanceType;
    }

    public int getValue() {
        return value;
    }
}
