package armorsetsearch.filter;

import armorsetsearch.skillactivation.ActivatedSkill;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.ArmorSkill;
import models.Equipment;

/**
 * This Filter will be applied internally.
 *
 * This will select the equipments with MAX skill point from a list of equipments bucketed into slots

 * This Filter must be the last filter that is going to be applied, because
 * for example if this filter is applied before Rarity(3)
 * then after this filter is done, some of rare 3 armors might be dropped.
 *
 * The torso up will bypass this check.
 * This will however select multiple Torso Up, but when the we are doing the search
 * we know what the skills users wants we can filter out the duplicated Torso up armors then.
 * see {@link ArmorSkillCacheTable::getEquipmentsWithDesiredSkills}
 *
 * So... why am I doing this?
 * Because without a way to cut down the number of armor set possibilities it will take
 * 'forever' to try and find all the combination of armor sets for a set of skills.
 */
public class MaxArmorSkillPointsFilter implements ArmorFilter{

    private String skillKind;
    private List<ActivatedSkill> desiredSkills;

    public MaxArmorSkillPointsFilter(String skillKind, List<ActivatedSkill> desiredSkills) {
        this.skillKind = skillKind;
        this.desiredSkills = desiredSkills;
    }

    @Override
    public List<Equipment> filter(List<Equipment> equipmentList) {
        if (equipmentList.isEmpty()) {
            return equipmentList;
        }

        // bucket the equipment by slots.
        Map<Integer, List<Equipment>> slotsMap = new HashMap<>();
        // max point bucketed by slots.
        Map<Integer, Integer> maxPointBySlot = new HashMap<>();

        // Return the armor with the max points in skillkind or has more slots than the maxed skill point equipment
        List<Equipment> maxPointBySlotsEquipments = new ArrayList<>();

        for (Equipment equipment : equipmentList) {
            List<Equipment> equipments = slotsMap.get(equipment.getSlots());
            if (equipments == null) {
                equipments = new ArrayList<>();
            }
            equipments.add(equipment);
            slotsMap.put(equipment.getSlots(), equipments);
        }

        // find max points by slots
        for (Map.Entry<Integer, List<Equipment>> entry : slotsMap.entrySet()) {
            int slotCount = entry.getKey();
            List<Equipment> equipments = entry.getValue();

            Equipment templateEquipment = equipments.get(0);
            int maxSkillPoints = findSkillPoint(templateEquipment);

            // find the armor with the max skill...
            for (Equipment equipment : equipments) {
                int currentMaxSkill = findSkillPoint(equipment);
                if (currentMaxSkill >= maxSkillPoints) {
                    maxSkillPoints = currentMaxSkill;
                }
            }

            maxPointBySlot.put(slotCount, maxSkillPoints);
        }

        // Select the max point equips by slots.
        for (Map.Entry<Integer, List<Equipment>> entry : slotsMap.entrySet()) {
            int slotCount = entry.getKey();
            List<Equipment> equipments = entry.getValue();
            int maxBySlot = maxPointBySlot.get(slotCount);
            equipments.forEach(equipment -> {
                int pointCount = findSkillPoint(equipment);
                if (pointCount >= maxBySlot || equipment.isTorsoUp()) {
                    maxPointBySlotsEquipments.add(equipment);
                }
            });
        }

        // do one more round of filtering
        // e.g if skills has +5 00-  and we have +7 000, we can skip the +5 00, armor
        List<Equipment> results = new ArrayList<>(maxPointBySlotsEquipments);
        for (Equipment equipment : maxPointBySlotsEquipments) {
            results = filterByMaxValue(equipment, results);
        }

        return results;
    }
    private List<Equipment> filterByMaxValue(Equipment templateEquipment, List<Equipment> equipmentList){
        List<Equipment> equipments = new ArrayList<>();
        int maxSkillPoints = findSkillPoint(templateEquipment);
        boolean alreadyHasTorsoUp = false;
        // Find all the armors that has more slots than the template or the same skill points.
        for (Equipment equipment : equipmentList) {
            int currentMaxSkill = findSkillPoint(equipment);
            if (equipment.getSlots() > templateEquipment.getSlots()) {
                equipments.add(equipment);
            } else if (!alreadyHasTorsoUp && equipment.isTorsoUp()) {
                alreadyHasTorsoUp = true;
                equipments.add(equipment);
            //}
            //else if (hasMoreThanOneDesireSkill(equipment)) {
            //    equipments.add(equipment);
            } else if (currentMaxSkill >= maxSkillPoints && maxSkillPoints > 0) {
                // select base on rarity
                if (equipment.getRarity() >= templateEquipment.getRarity()) {
                    equipments.add(equipment);
                }
            }
        }
        return equipments;
    }

    private int findSkillPoint(Equipment equipment) {
        for (ArmorSkill armorSkill : equipment.getArmorSkills()){
            if (armorSkill.isKind(skillKind)) {
                return armorSkill.points;
            }
        }
        return 0;
    }

    /**
     * Given a equipment check if it contains more than one desired skill.
     * @param equipment
     * @return
     */
    private boolean hasMoreThanOneDesireSkill(Equipment equipment) {
        int numberOfDesiredSkills = 0;
        for (ArmorSkill armorSkill : equipment.getArmorSkills()) {
            if (armorSkill.isPositive()) {

                for (ActivatedSkill activatedSkill : desiredSkills) {
                    if (armorSkill.isKind(activatedSkill.getKind())){
                        if (++numberOfDesiredSkills > 1){
                            return true;
                        }
                    }
                }

            }
        }
        return false;
    }
}
