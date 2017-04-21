package armorsetsearch.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import models.Equipment;
import models.EquipmentType;

public class RarityFilter implements ArmorFilter, ArmorSetFilter {

    private int rarity;

    public RarityFilter(int rarity) {
        this.rarity = rarity;
    }

    @Override
    public List<Equipment> filter(List<Equipment> equipmentList) {
        return equipmentList.stream().filter(equipment -> equipment.getRarity() >= rarity).collect(Collectors.toList());
    }

    @Override
    public boolean isArmorValid(Map<EquipmentType, Equipment> currentSet) {
        return filter(new ArrayList<>(currentSet.values())).size() > 0;
    }
}
