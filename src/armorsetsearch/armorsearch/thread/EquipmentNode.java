package armorsetsearch.armorsearch.thread;

import armorsetsearch.decorationsearch.SkillTable;
import armorsetsearch.decorationsearch.SkillTables;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Decoration;
import models.Equipment;
import models.EquipmentType;
import armorsetsearch.skillactivation.ActivatedSkill;
import armorsetsearch.skillactivation.SkillActivationChart;

public class EquipmentNode {
    private List<Equipment> equipments = new ArrayList<>(5);
    private Map<String, Integer> skillTable;
    private SkillTables skillTables;
    private List<ActivatedSkill> activatedSkills;
    private int skillMultiplier = 0;

    private EquipmentNode(List<Equipment> equipments, Map<String, Integer> skillTable, int skillMultiplier) {
        this.equipments = equipments;
        this.skillTable = skillTable;
        activatedSkills = SkillActivationChart.getActivatedSkills(skillTable);
        this.skillMultiplier = skillMultiplier;
    }

    public EquipmentNode(Equipment equipment, Map<String, Integer> skillTable) {
        equipments.add(equipment);
        this.skillTable = skillTable;
        activatedSkills =  SkillActivationChart.getActivatedSkills(skillTable);
        skillMultiplier =  equipment.isTorsoUp() ? 1 : 0;
    }
    public EquipmentNode(List<Equipment> equipments, Map<String, Integer> skillTable) {
        for (Equipment equipment : equipments) {
            // Deep copy the equipment
            this.equipments.add(new Equipment(equipment));
        }
        this.skillTable = new HashMap<>(skillTable);
        activatedSkills =  SkillActivationChart.getActivatedSkills(skillTable);
        skillMultiplier = 0;
        for (Equipment equipment : equipments){
            if (equipment.isTorsoUp()) {
                ++skillMultiplier;
            }
        }
    }

    public List<ActivatedSkill> getActivatedSkills() {
        return activatedSkills;
    }

    public int getSkillMultiplier() {
        return skillMultiplier;
    }

    public Map<String, Integer> getSkillTable() {
        return skillTable;
    }

    public List<Equipment> getEquipments() {
        return equipments;
    }

    public void setActivatedSkills(List<ActivatedSkill> activatedSkills) {
        this.activatedSkills = activatedSkills;
    }

    public void setSkillTable(Map<String, Integer> skillTable) {
        this.skillTable = skillTable;
    }

    public List<SkillTable> getSkillTables() {
        return skillTables.getSkillTables();
    }

    public void setSkillTables(SkillTables skillTables) {
        this.skillTables = skillTables;
    }

    public static EquipmentNode add(EquipmentNode node1, EquipmentNode curEquipmentNode, EquipmentType equipmentType){
        Map<String, Integer> sumTable = SkillActivationChart.add(node1.skillTable,  curEquipmentNode.skillTable);
        List<Equipment> equipments = new ArrayList<>(node1.equipments);
        equipments.addAll(curEquipmentNode.equipments);
        return new EquipmentNode(equipments, sumTable, node1.skillMultiplier + curEquipmentNode.skillMultiplier);
    }

}
