package armorsetsearch.skillactivation;

import armorsetsearch.armorsearch.thread.EquipmentNode;
import armorsetsearch.decorationsearch.DecorationSearch;
import constants.Constants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Decoration;
import models.Equipment;

public class SkillUtil {

    public static EquipmentNode placeDecorations(EquipmentNode originalEquipmentNode, Map<String, Integer> skillTable, List<DecorationSearch.DecorationForOneEquipment> decorationForOneEquipments){
        EquipmentNode equipmentNode = new EquipmentNode(originalEquipmentNode.getEquipments(), skillTable);
        for (DecorationSearch.DecorationForOneEquipment decorationForOneEquipment : decorationForOneEquipments) {
            List<Decoration> decorations = decorationForOneEquipment.getDecorations();
            int slotsNeeded = decorations.stream().mapToInt(Decoration::getSlotsNeeded).sum();
            List<Equipment> equipments = equipmentNode.getEquipments();
            for (Equipment equipment : equipments) {
                if (equipment.getSlotsUsed() == 0 && equipment.getSlots() == slotsNeeded) {
                    equipment.addAllDecorations(decorations);
                }
            }
        }
        return equipmentNode;
    }

    /**
     * check if the current armor set is a super set of the desired set
     *
     * @param desiredSkills the skill that the user wants
     * @param activatedSkills current generated armor set
     * @return true if current generated armor set matches or has more of the desired skills
     */
    public static boolean containsDesiredSkills(List<ActivatedSkill> desiredSkills, List<ActivatedSkill> activatedSkills) {
        if (activatedSkills.size() < desiredSkills.size()) {
            return false;
        } else {
            // the skill is a match if
            // 1. the skill kind matches
            // 2. skill point is equal or greater to the desired skill
            for (ActivatedSkill desiredSkill : desiredSkills) {
                boolean hasSkill = false;
                for (ActivatedSkill activatedSkill : activatedSkills) {
                    boolean hasEnoughSkillPoints = activatedSkill.getAccumulatedPoints() >= desiredSkill.getPointsNeededToActivate();
                    if (hasEnoughSkillPoints && desiredSkill.kind.equalsIgnoreCase(activatedSkill.kind)) {
                        hasSkill = true;
                    }
                }
                if (!hasSkill) {
                    // dont have one of the desired skills, exit.
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Put some hardcoded limit to avoid user making a massive search query.
     *
     * @param desiredSkills
     * @return
     */
    public static boolean shouldDoSearch(List<SkillActivationRequirement> desiredSkills) {
        if (desiredSkills.isEmpty() || desiredSkills.size() >= Constants.MAX_SKILL) {
            return false;
        }

        int sumSkillPoint = desiredSkills.stream().mapToInt(SkillActivationRequirement::getPointsNeededToActivate).sum();
        return sumSkillPoint <= Constants.MAX_SKILL_POINT;
    }

    /**
     * Find which skill is missing from the desire skill and the point that is needed.
     * Note: the activatedSkills should not contain two skill with the same skill Kind
     * Such as AuL and AuS.  Hearing Small and Hearing Large.  Negative Stun and Double Stun.
     *
     * @param desiredSkills
     * @param activatedSkills
     * @return
     */
    public static Map<String, Integer> getMissingSkills(List<ActivatedSkill> desiredSkills, Map<String, Integer> skillTable) {
        Map<String, Integer> missingSkill = new HashMap<>();
        desiredSkills.forEach(desiredSkill -> {
            Integer points = skillTable.get(desiredSkill.getKind());
            if (points != null) {
                // Pointed needed in the skill - skill point accumulated.
                int pointDiff = desiredSkill.getPointsNeededToActivate() - points;
                // found a partial skill with points missing?
                if (pointDiff > 0) {
                    // there is still points needed in this skill.
                    missingSkill.put(desiredSkill.getKind(), pointDiff);
                }
            } else {
                // skill does not even exist in the current set.
                missingSkill.put(desiredSkill.getKind(), desiredSkill.getPointsNeededToActivate());
            }
        });
        return missingSkill;
    }
}
