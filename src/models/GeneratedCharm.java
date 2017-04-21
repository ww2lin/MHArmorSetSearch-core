package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneratedCharm {
    private String charmType;
    private List<CharmSkill> charmSkills = new ArrayList<>();
    private Map<Decoration, Integer> decorations = new HashMap<>();
    private int slotUsed = 0;

    public GeneratedCharm(String charmType, List<CharmSkill> charmSkills, List<Decoration> decorations, int slotUsed) {
        this.charmType = charmType;
        this.charmSkills = charmSkills;
        this.slotUsed = slotUsed;
        for (Decoration decoration : decorations) {
            Integer counts = this.decorations.get(decoration);
            if (counts == null) {
                counts = 0;
            }
            ++counts;
            this.decorations.put(decoration, counts);
        }
    }

    public String getCharmType() {
        return charmType;
    }

    public List<CharmSkill> getCharmSkills() {
        return charmSkills;
    }

    public Map<Decoration, Integer> getDecorations() {
        return decorations;
    }

    public int getSlotUsed() {
        return slotUsed;
    }

    public static class CharmSkill {
        String skillKind;
        int skillPoints;
        int position;

        public String getSkillKind() {
            return skillKind;
        }

        public int getSkillPoints() {
            return skillPoints;
        }

        public CharmSkill(String skillKind, int skillPoints, int position) {
            this.skillKind = skillKind;
            this.skillPoints = skillPoints;
            this.position = position;
        }
    }
}
