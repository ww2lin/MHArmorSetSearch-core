package armorsetsearch.filter;

import java.util.Map;
import models.Equipment;
import models.EquipmentType;

/**
 * filter for a generated armor set
 */
public interface ArmorSetFilter {
    boolean isArmorValid(Map<EquipmentType, Equipment> currentSet);
}
