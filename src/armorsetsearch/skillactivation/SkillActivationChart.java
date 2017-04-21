package armorsetsearch.skillactivation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import models.ArmorSkill;
import models.ClassType;
import models.Decoration;
import models.Equipment;
import models.EquipmentType;
import models.GeneratedCharm;

public class SkillActivationChart {

    private static ClassType classType;
    private static Map<String, List<SkillActivationRequirement>> skillActivationLookupTable;

    /**
     * Mapping from the skill kind -> actual skill
     * e.g Attack -> Attack Up Small, Attack Up Mid, Attack Up Large.
     */
    public SkillActivationChart(Map<String, List<SkillActivationRequirement>> skillChart, ClassType classType) {
        skillActivationLookupTable = skillChart;
        SkillActivationChart.classType = classType;
    }

    public Set<String> getSkillKind() {
        return skillActivationLookupTable.keySet();
    }

    public ActivatedSkill getMaxedActivatedSkill(String kind) {
        List<SkillActivationRequirement> skillActivationRequirements = skillActivationLookupTable.get(kind);

        // There has to be a skill with the type kind, or we will never obtain kind from the CSV sheets.
        SkillActivationRequirement maxSkillActivationRequirement = skillActivationRequirements.get(0);
        for (SkillActivationRequirement skillActivationRequirement : skillActivationRequirements) {
            if (skillActivationRequirement.getPointsNeededToActivate() > maxSkillActivationRequirement.getPointsNeededToActivate()) {
                maxSkillActivationRequirement = skillActivationRequirement;
            }
        }
        return new ActivatedSkill(maxSkillActivationRequirement, 0);
    }

    public static Map<String, Integer> add(Map<String, Integer> chart1, Map<String, Integer> chart2) {
        Map<String, Integer> newChart = new HashMap<>();
        newChart.putAll(chart1);
        for (Map.Entry<String, Integer> chartData : chart2.entrySet()) {
            Integer points = newChart.get(chartData.getKey());
            if (points == null) {
                points = 0;
            }
            points += chartData.getValue();
            newChart.put(chartData.getKey(), points);
        }
        return newChart;
    }

    public static Map<String, Integer> multiply(Map<String, Integer> chart, int skillMultiplier) {
        Map<String, Integer> currentEquipmentSkillChart = new HashMap<>();
        for (Map.Entry<String, Integer> entry : chart.entrySet()) {
            String skill = entry.getKey();
            int points = entry.getValue();
            currentEquipmentSkillChart.put(skill, points * skillMultiplier);
        }
        return currentEquipmentSkillChart;
    }

    public static Map<String, Integer> getActivatedSkillChart(Equipment equipment) {
        Map<String, Integer> currentEquipmentSkillChart = new HashMap<>();
        updateSkillChartByArmorSkill(currentEquipmentSkillChart, equipment.getArmorSkills(), 1);
        updateSkillChartByDecoration(currentEquipmentSkillChart, equipment.getDecorations(), 1);
        return currentEquipmentSkillChart;
    }

    public static Map<String, Integer> getActivatedSkillChart(List<Equipment> equipments, int maxIndex, int skillMultiplier) {
        Map<String, Integer> currentEquipmentSkillChart = new HashMap<>();
        for (int i = 0; i <= maxIndex ; ++i) {
            Equipment equipment = equipments.get(i);
            int multiplier = equipment.getEquipmentType() == EquipmentType.BODY ? skillMultiplier : 1;
            updateSkillChartByArmorSkill(currentEquipmentSkillChart, equipment.getArmorSkills(), multiplier);
            updateSkillChartByDecoration(currentEquipmentSkillChart, equipment.getDecorations(), multiplier);
        }
        return currentEquipmentSkillChart;
    }

    public static Map<String, Integer> getActivatedSkillChart(Map<String, Integer> skillTable, Map<Decoration, Integer> decorations) {
        Map<String, Integer> currentEquipmentSkillChart = new HashMap<>(skillTable);
        updateSkillChartByDecoration(currentEquipmentSkillChart, decorations, 1);
        return currentEquipmentSkillChart;
    }

    /**
     * refactor this later to remove duplicated code.
     *
     * @param decoration
     * @return
     */
    public static Map<String, Integer> getSkillChart(Decoration decoration) {
        Map<String, Integer> currentEquipmentSkillChart = new HashMap<>();
        for (ArmorSkill armorSkill : decoration.getArmorSkills()) {
            Integer sum = currentEquipmentSkillChart.get(armorSkill.kind);
            if (sum == null) {
                // if the current skill kind don't exist, assign it to 0
                sum = 0;
            }

            // Times the armor skill by the number of the same jewels
            sum += armorSkill.points;
            currentEquipmentSkillChart.put(armorSkill.kind, sum);
        }
        return currentEquipmentSkillChart;
    }

    public static Map<String, Integer> getSkillChart(GeneratedCharm generatedCharm, int charmSkillMultiplier) {
        Map<String, Integer> currentCharmSkillChart = new HashMap<>();
        updateSkillChartByCharmSkill(currentCharmSkillChart, generatedCharm, charmSkillMultiplier);
        updateSkillChartByDecoration(currentCharmSkillChart, generatedCharm.getDecorations(), charmSkillMultiplier);
        return currentCharmSkillChart;
    }

    public static void updateSkillChartByArmorSkill(Map<String, Integer> currentEquipmentSkillChart, Set<ArmorSkill> armorSkills, int skillMultiplier) {
        for (ArmorSkill armorSkill : armorSkills) {
            // accumulate the skill point by skill kind
            Integer sum = currentEquipmentSkillChart.get(armorSkill.kind);
            if (sum == null) {
                // if the current skill kind don't exist, assign it to 0
                sum = 0;
            }

            sum += (armorSkill.points * skillMultiplier);
            currentEquipmentSkillChart.put(armorSkill.kind, sum);
        }
    }

    public static void updateSkillChartByDecoration(Map<String, Integer> currentEquipmentSkillChart, List<Decoration> decorations, int skillMuliplier) {
        // loop over the decorations
        Map<Decoration, Integer> decorationIntegerMap = new HashMap<>();
        for (Decoration decoration : decorations) {
            Integer frequencyCount = decorationIntegerMap.get(decoration);
            if (frequencyCount == null) {
                frequencyCount = 0;
            }
            frequencyCount +=1;
            decorationIntegerMap.put(decoration, frequencyCount);
        }
        updateSkillChartByDecoration(currentEquipmentSkillChart, decorationIntegerMap, skillMuliplier);
    }

    public static void updateSkillChartByDecoration(Map<String, Integer> currentEquipmentSkillChart, Map<Decoration, Integer> decorations, int skillMuliplier) {
        // loop over the decorations
        for (Map.Entry<Decoration, Integer> decorationSet : decorations.entrySet()) {
            Decoration decoration = decorationSet.getKey();
            Integer frequencyCount = decorationSet.getValue();

            for (ArmorSkill armorSkill : decoration.getArmorSkills()) {
                Integer sum = currentEquipmentSkillChart.get(armorSkill.kind);
                if (sum == null) {
                    // if the current skill kind don't exist, assign it to 0
                    sum = 0;
                }

                // Times the armor skill by the number of the same jewels
                sum += (armorSkill.points * frequencyCount * skillMuliplier);
                currentEquipmentSkillChart.put(armorSkill.kind, sum);
            }
        }
    }

    public static void updateSkillChartByCharmSkill(Map<String, Integer> currentEquipmentSkillChart, GeneratedCharm generatedCharm, int charmMultiplier) {
        for (GeneratedCharm.CharmSkill charmSkill : generatedCharm.getCharmSkills()) {
            // accumulate the skill point by skill kind
            Integer sum = currentEquipmentSkillChart.get(charmSkill.getSkillKind());
            if (sum == null) {
                // if the current skill kind don't exist, assign it to 0
                sum = 0;
            }

            sum += charmSkill.getSkillPoints() * charmMultiplier;
            currentEquipmentSkillChart.put(charmSkill.getSkillKind(), sum);
        }
    }

    public static List<ActivatedSkill> getActivatedSkills(Equipment equipment) {
        return getActivatedSkills(getActivatedSkillChart(equipment));
    }
    /**
     * check to see which skill is activated.
     * @param currentEquipmentSkillChart
     * @return
     */
    public static List<ActivatedSkill> getActivatedSkills(Map<String, Integer> currentEquipmentSkillChart){
        List<ActivatedSkill> activatedSkills = new LinkedList<>();
        for (Map.Entry<String, Integer> skill : currentEquipmentSkillChart.entrySet()) {
            String kind = skill.getKey();
            Integer skillPoints = skill.getValue();

            List<SkillActivationRequirement> skillActivationRequirements = skillActivationLookupTable.get(kind);

            SkillActivationRequirement maxSkillActivation = null;
            // Find the biggest armor skill the current skill point can activate.
            // E.g 20 points in Attack -> will only return 'Attack Up Large'
            for (SkillActivationRequirement skillActivationRequirement : skillActivationRequirements){
                // TODO fix it for negative skill?
                boolean isNegativeSkill = skillActivationRequirement.isNegativeSkill();
                boolean hasEnoughSkillPoints = skillPoints >= skillActivationRequirement.getPointsNeededToActivate();
                boolean usableClass = skillActivationRequirement.getClassType() == ClassType.ALL || skillActivationRequirement.getClassType() == classType;

                if (!isNegativeSkill && hasEnoughSkillPoints && usableClass) {
                    maxSkillActivation = skillActivationRequirement;
                }
            }

            if (maxSkillActivation != null){
                // found an activated skill.
                activatedSkills.add(new ActivatedSkill(maxSkillActivation, skillPoints));
            }
        }
        return activatedSkills;
    }
}
