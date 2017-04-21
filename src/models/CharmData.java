package models;

import java.util.ArrayList;
import java.util.List;

/**
 * defines one type of charm
 *
 * This is a model that maps to the CSV.
 * The reason we have both {@link GeneratedCharm} and this class is because
 * Before brute-forcing a charm, we have no idea which of the charm position(charmPoints)
 * will be valid. Thus this class defines what kind of values/position the charm skill can be in.
 */
public class CharmData {
    private String skillkind;
    private String charmType;
    // Given a index, what is the range of points it can have.
    List<CharmPoint> charmPoints = new ArrayList<>();

    private CharmData() {
    }

    public static CharmData Builder(){
        return new CharmData();
    }

    public String getSkillkind() {
        return skillkind;
    }

    public CharmData setSkillkind(String skillkind) {
        this.skillkind = skillkind;
        return this;
    }

    public List<CharmPoint> getCharmPoints() {
        return charmPoints;
    }

    public CharmData setCharmPoints(List<CharmPoint> charmPoints) {
        this.charmPoints = charmPoints;
        return this;
    }

    public CharmData addCharmPoint(CharmPoint charmPoint) {
        if (charmPoint != null) {
            charmPoints.add(charmPoint);
        }
        return this;
    }

    public String getCharmType() {
        return charmType;
    }

    public CharmData setCharmType(String charmType) {
        this.charmType = charmType;
        return this;
    }

    public boolean isSameCharmType(CharmData charmData) {
        return charmType.equalsIgnoreCase(charmData.charmType);
    }

    @Override public String toString() {
        return "CharmData{" +
            "skillkind='" + skillkind + '\'' +
            ", charmType='" + charmType + '\'' +
            ", charmPoints=" + charmPoints +
            '}';
    }

    public static class CharmPoint {
        private int min;
        private int max;
        // is it first or second skill
        private int skillPosition;

        public CharmPoint(int min, int max, int skillPosition) {
            this.min = min;
            this.max = max;
            this.skillPosition = skillPosition;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        public int getSkillPosition() {
            return skillPosition;
        }

        @Override
        public String toString() {
            return "CharmPoint{" +
                "min=" + min +
                ", max=" + max +
                ", skillPosition=" + skillPosition +
                '}';
        }
    }
}
