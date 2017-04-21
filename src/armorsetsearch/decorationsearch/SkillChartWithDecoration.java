package armorsetsearch.decorationsearch;

import armorsetsearch.skillactivation.SkillActivationChart;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Decoration;

/**
 * Each skill chart corresponds to a list of decorations.
 */
public class SkillChartWithDecoration {
    // TODO change this into decoration -> frequency
    List<Decoration> decorations = new ArrayList<>();
    Map<String, Integer> skillChart = new HashMap<>();

    public SkillChartWithDecoration(List<Decoration> decorations, Map<String, Integer> skillChart) {
        this.decorations = decorations;
        this.skillChart = skillChart;
    }

    public SkillChartWithDecoration() {
    }

    public List<Decoration> getDecorations() {
        return decorations;
    }

    public static SkillChartWithDecoration add(SkillChartWithDecoration skillChart1, SkillChartWithDecoration skillChart2){
        SkillChartWithDecoration newSkillChart = new SkillChartWithDecoration();
        newSkillChart.decorations.addAll(skillChart1.decorations);
        newSkillChart.decorations.addAll(skillChart2.decorations);
        newSkillChart.skillChart = SkillActivationChart.add(skillChart1.skillChart, skillChart2.skillChart);
        return newSkillChart;
    }

    public Map<String, Integer> getSkillChart() {
        return skillChart;
    }
}
