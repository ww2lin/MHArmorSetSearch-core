package armorsetsearch.decorationsearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains one possible skill table for decoration for a list of equipment nodes.
 */
public class SkillTable {
    private Map<String, Integer> skillTable = new HashMap<>();
    private List<DecorationSearch.DecorationForOneEquipment> decorations = new ArrayList<>();

    public SkillTable(Map<String, Integer> skillTable, List<DecorationSearch.DecorationForOneEquipment> decorations) {
        this.skillTable = skillTable;
        this.decorations = decorations;
    }

    public SkillTable(Map<String, Integer> skillTable) {
        this.skillTable = skillTable;
    }

    public Map<String, Integer> getSkillTable() {
        return skillTable;
    }

    public void setSkillTable(Map<String, Integer> skillTable) {
        this.skillTable = skillTable;
    }

    public List<DecorationSearch.DecorationForOneEquipment> getDecorations() {
        return decorations;
    }

    public void setDecorations(List<DecorationSearch.DecorationForOneEquipment> decorations) {
        this.decorations = decorations;
    }
}
