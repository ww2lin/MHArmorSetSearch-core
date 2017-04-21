package armorsetsearch;

import java.util.List;
import models.Equipment;

public class AllEquipments {

    private final List<Equipment> headEquipments;
    private final List<Equipment> bodyEquipments;
    private final List<Equipment> armEquipments;
    private final List<Equipment> wstEquipments;
    private final List<Equipment> legEquipments;

    public AllEquipments(List<Equipment> headEquipments, List<Equipment> bodyEquipments, List<Equipment> armEquipments, List<Equipment> wstEquipments, List<Equipment> legEquipments) {
        this.headEquipments = headEquipments;
        this.bodyEquipments = bodyEquipments;
        this.armEquipments = armEquipments;
        this.wstEquipments = wstEquipments;
        this.legEquipments = legEquipments;
    }

    List<Equipment> getHeadEquipments() {
        return headEquipments;
    }

    List<Equipment> getBodyEquipments() {
        return bodyEquipments;
    }

    List<Equipment> getArmEquipments() {
        return armEquipments;
    }

    List<Equipment> getWstEquipments() {
        return wstEquipments;
    }

    List<Equipment> getLegEquipments() {
        return legEquipments;
    }
}
