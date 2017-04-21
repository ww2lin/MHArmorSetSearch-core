package armorsetsearch;

import armorsetsearch.filter.ArmorFilter;
import armorsetsearch.filter.MaxArmorSkillPointsFilter;
import constants.Constants;
import constants.StringConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import models.ArmorSkill;
import models.ClassType;
import models.Equipment;
import models.EquipmentType;
import models.Gender;
import armorsetsearch.skillactivation.ActivatedSkill;
import armorsetsearch.skillactivation.SkillActivationChart;

public class ArmorSkillCacheTable {
    // Build a table from kind -> All equipment has that kind of skill
    private Map<String, List<Equipment>> headEquipmentCache = new HashMap<>();
    private Map<String, List<Equipment>> bodyEquipmentCache = new HashMap<>();
    private Map<String, List<Equipment>> armEquipmentCache = new HashMap<>();
    private Map<String, List<Equipment>> wstEquipmentCache = new HashMap<>();
    private Map<String, List<Equipment>> legEquipmentCache = new HashMap<>();

    private Map<EquipmentType, Map<String, List<Equipment>>> allEquipments = new HashMap<>();

    private List<ArmorFilter> armorFilters;
    private ClassType classType;
    private Gender gender;
    private List<ActivatedSkill> desiredSkills;
    /**
     * Build a look up table by skill, for faster lookup time. E.g
     * headEquipmentCache: skillKind -> All head armor that has this skill.
     * This construction should be moved into the csv while generating the List of equipments
     */
    public ArmorSkillCacheTable(List<ActivatedSkill> desiredSkills, SkillActivationChart skillActivationChart, AllEquipments allEquipment, List<ArmorFilter> armorFilters, ClassType classType, Gender gender) {
        this.armorFilters = armorFilters;
        this.classType = classType;
        this.gender = gender;
        this.desiredSkills = desiredSkills;

        Set<String> skillKinds = skillActivationChart.getSkillKind();

        for (String skillkind : skillKinds) {
            updateCacheBySkillKind(allEquipment.getHeadEquipments(), headEquipmentCache, skillkind);
            updateCacheBySkillKind(allEquipment.getBodyEquipments(), bodyEquipmentCache, skillkind);
            updateCacheBySkillKind(allEquipment.getArmEquipments(), armEquipmentCache, skillkind);
            updateCacheBySkillKind(allEquipment.getWstEquipments(), wstEquipmentCache, skillkind);
            updateCacheBySkillKind(allEquipment.getLegEquipments(), legEquipmentCache, skillkind);
        }

        this.allEquipments.put(EquipmentType.HEAD, headEquipmentCache);
        this.allEquipments.put(EquipmentType.BODY, bodyEquipmentCache);
        this.allEquipments.put(EquipmentType.ARM, armEquipmentCache);
        this.allEquipments.put(EquipmentType.WST, wstEquipmentCache);
        this.allEquipments.put(EquipmentType.LEG, legEquipmentCache);

    }

    private void updateCacheBySkillKind(final List<Equipment> equipmentData, Map<String, List<Equipment>> currentCache, String skillKind){
        MaxArmorSkillPointsFilter maxArmorSkillPointsFilter = new MaxArmorSkillPointsFilter(skillKind, desiredSkills);

        List<Equipment> equipmentsByKind = currentCache.get(skillKind);
        if (equipmentsByKind == null){
            equipmentsByKind = new LinkedList<>();
        }

        List<Equipment> filterBySkillKind = getEquipmentBySkillKind(equipmentData, skillKind);

        // Apply the filter Function and add it to the list
        equipmentsByKind.addAll(maxArmorSkillPointsFilter.filter(doFilter(filterBySkillKind)));

        currentCache.put(skillKind, equipmentsByKind);
    }

    /**
     * @param equipments a list of equipment, head, body...
     * @param skillkind which skill we are trying to search for
     * @return all the equipment that matches the skill kind
     */
    private List<Equipment> getEquipmentBySkillKind(List<Equipment> equipments, String skillkind) {
        return equipments.stream().filter(
            (equipment) -> {
                boolean isArmorAvailable = equipment.isAvailable();
                boolean validGender = (equipment.getGender() == Gender.BOTH || equipment.getGender() == gender);
                boolean validClassType = (equipment.getClassType() == ClassType.ALL || equipment.getClassType() == classType);
                if (validGender && validClassType && isArmorAvailable) {
                    for (ArmorSkill armorSkill : equipment.getArmorSkills()) {
                        //TODO remove negative check?
                        if (armorSkill.isKind(skillkind) && armorSkill.points > 0) {
                            return true;
                        }
                        // keep TorsoUp Pieces and 3 slotted no armor skill equipment bypassed all checks
                        if (equipment.isTorsoUp()) {
                            return true;
                        }

                        if (equipment.getArmorSkills().size() == 0 && equipment.getSlots() == Constants.MAX_SLOTS){
                            return true;
                        }

                    }
                }
                return false;
            }).collect(Collectors.toList());
    }

    private List<Equipment> doFilter(List<Equipment> equipments){
        for (ArmorFilter armorFilter : armorFilters) {
            equipments = armorFilter.filter(equipments);
        }

        return equipments;
    }


    private List<Equipment> getEquipmentsWithDesiredSkills(EquipmentType equipmentType, List<ActivatedSkill> desiredSkills) {
        Map<String, List<Equipment>> cache = allEquipments.get(equipmentType);

        // Make sure no duplicated equipment are selected.
        Set<Integer> equipmentIds = new HashSet<>();
        // check to see if we need to sneak in charka armors.
        boolean containsThreeSlottedEquipment = false;
        List<Equipment> equipments = new ArrayList<>();
        for (ActivatedSkill activatedSkill : desiredSkills) {
            List<Equipment> equipmentsWithDesiredSkills = cache.get(activatedSkill.getKind());
            if (equipmentsWithDesiredSkills != null) {
                for (Equipment equipmentToAdd : equipmentsWithDesiredSkills) {
                    if (equipmentIds.contains(equipmentToAdd.getId())) {
                        continue;
                    }

                    equipmentIds.add(equipmentToAdd.getId());

                    if (equipmentToAdd.getSlots() == Constants.MAX_SLOTS) {
                        // Found atleast one armor that has 3 slots.
                        containsThreeSlottedEquipment = true;
                    }

                    // filter additional torso up equipments
                    if (!equipmentToAdd.isTorsoUp()) {
                        equipments.add(equipmentToAdd);
                    }
                }
            }
        }

        if (!containsThreeSlottedEquipment) {
            // Sneak in a 3 slotted no skill armor (e.g charkra armors)
            equipments.add(Equipment.Builder()
                               .setId(Constants.GENERATED_EQUIPMENT_ID)
                               .setName(StringConstants.GENERATED_ARMOR)
                               .setEquipmentType(equipmentType)
                               .setSlots(Constants.MAX_SLOTS)
                               .setRarity(0)
                               .setCanBeSubstitutedForAnyOtherThreeSlotEquipment(true));
        }
        return equipments;
    }



    public Map<EquipmentType, List<Equipment>> getEquipmentCache(List<ActivatedSkill> desiredSkills) {
        Map<EquipmentType, List<Equipment>> results = new HashMap<>();
        for (EquipmentType equipmentType : EquipmentType.values()) {
            if (equipmentType == EquipmentType.WEP){
                // no equipment for wep, skip.
                continue;
            }
            results.put(equipmentType, getEquipmentsWithDesiredSkills(equipmentType, desiredSkills));
        }
        return results;
    }
}
