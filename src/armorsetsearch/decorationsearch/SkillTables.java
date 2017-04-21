package armorsetsearch.decorationsearch;

import armorsetsearch.skillactivation.SkillActivationChart;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Contains all possible skill table for decoration for a list of equipment nodes.
 */
public class SkillTables {
    private List<SkillTable> skillTables = new ArrayList<>();

    public SkillTables(List<SkillTable> skillTables) {
        this.skillTables = skillTables;
    }

    public SkillTables(SkillTables other) {this.skillTables = other.skillTables;}

    public SkillTables() {
    }

    public void add(SkillTable skillTable) {
        skillTables.add(skillTable);
    }

    public void addAll(List<SkillTable> skillTables) {
        this.skillTables.addAll(skillTables);
    }

    public int size() {
        return skillTables.size();
    }

    public SkillTable get(int index) {
        return skillTables.get(index);
    }
    public void remove(int index){
        skillTables.remove(index);
    }

    public List<SkillTable> getSkillTables() {
        return skillTables;
    }

    public static SkillTables cartesianProduct(SkillTables skillTables1, SkillTables skillTables2) {
        SkillTables sumTable = new SkillTables();
        if (skillTables1.size() == 0) {
            sumTable.addAll(skillTables2.skillTables);
            return sumTable;
        } else if (skillTables2.size() == 0) {
            sumTable.addAll(skillTables1.skillTables);
            return sumTable;
        }

        for (int i = 0; i < skillTables1.size(); ++i) {
            SkillTable table1 = skillTables1.get(i);
            for (int j = i; j < skillTables2.size(); ++j) {
                SkillTable table2 = skillTables2.get(j);
                Map<String, Integer> sum = SkillActivationChart.add(table1.getSkillTable(), table2.getSkillTable());
                List<DecorationSearch.DecorationForOneEquipment> sumDecorations = new ArrayList<>();
                sumDecorations.addAll(table1.getDecorations());
                sumDecorations.addAll(table2.getDecorations());
                sumTable.add(new SkillTable(sum, sumDecorations));
            }
        }
        return sumTable;
    }
}
