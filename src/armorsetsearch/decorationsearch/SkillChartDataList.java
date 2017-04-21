package armorsetsearch.decorationsearch;

import java.util.ArrayList;
import java.util.List;
import models.Decoration;

class SkillChartDataList {
    private List<SkillChartWithDecoration> skillChartWithDecorations = new ArrayList<>();

    public SkillChartDataList(List<SkillChartWithDecoration> skillChartWithDecorations) {
        this.skillChartWithDecorations = skillChartWithDecorations;
    }

    public SkillChartDataList() {
    }

    public void add(SkillChartWithDecoration skillChartWithDecoration) {
        skillChartWithDecorations.add(skillChartWithDecoration);
    }

    public void addAll(SkillChartDataList skillChartDataList) {
        skillChartWithDecorations.addAll(skillChartDataList.skillChartWithDecorations);
    }

    public SkillChartWithDecoration get(int index) {
        return skillChartWithDecorations.get(index);
    }

    public List<SkillChartWithDecoration> getSkillChartWithDecorations() {
        return skillChartWithDecorations;
    }

    public void setSkillChartWithDecorations(List<SkillChartWithDecoration> skillChartWithDecorations) {
        this.skillChartWithDecorations = skillChartWithDecorations;
    }

    public int size(){
        return skillChartWithDecorations.size();
    }

    public static SkillChartDataList cartesianProduct(SkillChartDataList list1, SkillChartDataList list2) {
        SkillChartDataList skillChartDataList = new SkillChartDataList();
        for (int i = 0; i < list1.skillChartWithDecorations.size(); ++i) {
            SkillChartWithDecoration decoration1 = list1.get(i);
            for (int j = i; j < list2.size(); ++j) {
                SkillChartWithDecoration decoration2 = list2.get(j);
                SkillChartWithDecoration newSkillChart = SkillChartWithDecoration.add(decoration1, decoration2);
                skillChartDataList.add(newSkillChart);
            }
        }
        return skillChartDataList;
    }
}
