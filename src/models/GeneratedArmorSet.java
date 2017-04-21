package models;

import armorsetsearch.armorsearch.thread.EquipmentNode;
import armorsetsearch.skillactivation.SkillActivationChart;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import armorsetsearch.skillactivation.ActivatedSkill;
import java.util.Map;

// contains a list of equipment sets, with decorations
public class GeneratedArmorSet {
    List<ActivatedSkill> activatedSkills;
    Map<String, Integer> skillTable;
    List<Resistance> totalResistance;
    List<Equipment> equipments;

    // Some set might not need this.
    GeneratedCharm generatedCharm;

    int totalBaseDefense;
    int totalMaxDefense;

    public GeneratedArmorSet(EquipmentNode equipmentNode) {
        this.skillTable = equipmentNode.getSkillTable();
        this.activatedSkills = equipmentNode.getActivatedSkills();
        this.equipments = equipmentNode.getEquipments();

        totalResistance = calculateTotalResistance();
        totalBaseDefense = calculateTotalBaseDefense();
        totalMaxDefense = calculateTotalMaxDefense();
    }

    public GeneratedArmorSet(EquipmentNode equipmentNode, GeneratedCharm generatedCharm, Map<String, Integer> skillTable) {
        this.skillTable = skillTable;
        this.generatedCharm = generatedCharm;
        this.activatedSkills = SkillActivationChart.getActivatedSkills(this.skillTable);
        this.equipments = equipmentNode.getEquipments();

        totalResistance = calculateTotalResistance();
        totalBaseDefense = calculateTotalBaseDefense();
        totalMaxDefense = calculateTotalMaxDefense();

    }

    public static class MostSkillComparator implements Comparator<GeneratedArmorSet> {
        @Override
        public int compare(GeneratedArmorSet o1, GeneratedArmorSet o2) {
            // dont need to worry about overflow, as number of unique skills is way too small.
            return o1.activatedSkills.size() - o2.activatedSkills.size();
        }
    }

    @Override
    public String toString() {
        return "GeneratedArmorSet{" +
            "activatedSkills=" + activatedSkills +
            ", skillTable=" + skillTable +
            ", totalResistance=" + totalResistance +
            ", equipments=" + equipments +
            ", generatedCharm=" + generatedCharm +
            ", totalBaseDefense=" + totalBaseDefense +
            ", totalMaxDefense=" + totalMaxDefense +
            '}';
    }

    public List<ActivatedSkill> getActivatedSkills() {
        return activatedSkills;
    }

    public List<Equipment> getEquipments() {
        return equipments;
    }

    public GeneratedCharm getGeneratedCharm() {
        return generatedCharm;
    }

    private List<Resistance> calculateTotalResistance(){
        Resistance fire = new Resistance(ResistanceType.FIRE, 0);
        Resistance water = new Resistance(ResistanceType.WATER, 0);
        Resistance thunder = new Resistance(ResistanceType.THUNDER, 0);
        Resistance ice = new Resistance(ResistanceType.ICE, 0);
        Resistance dragon = new Resistance(ResistanceType.DRAGON, 0);

        List<Resistance> resistances = new ArrayList<>();
        resistances.add(fire);
        resistances.add(water);
        resistances.add(thunder);
        resistances.add(ice);
        resistances.add(dragon);

        equipments.forEach(equipment -> {
            equipment.getResistances().forEach(resistance -> {
                switch (resistance.resistanceType){
                    case FIRE:
                        fire.add(resistance);
                        break;
                    case WATER:
                        water.add(resistance);
                        break;
                    case THUNDER:
                        thunder.add(resistance);
                        break;
                    case ICE:
                        ice.add(resistance);
                        break;
                    case DRAGON:
                        dragon.add(resistance);
                        break;
                    default:
                        break;
                }
            });
        });
        return resistances;
    }

    private int calculateTotalBaseDefense(){
        return equipments.stream().mapToInt(Equipment::getBaseDefense).sum();
    }

    private int calculateTotalMaxDefense(){
        return equipments.stream().mapToInt(Equipment::getMaxDefense).sum();
    }

    public List<Resistance> getTotalResistance(){
        return totalResistance;
    }

    public int getTotalBaseDefense() {
        return totalBaseDefense;
    }

    public int getTotalMaxDefense() {
        return totalMaxDefense;
    }
}
