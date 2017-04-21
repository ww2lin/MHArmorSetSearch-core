package armorsetsearch.skillactivation;

public class ActivatedSkill {
    String name;
    String kind;

    private int pointsNeededToActivate;
    private int accumulatedPoints;

    // for displaying on UI
    private String displayText;

    public ActivatedSkill(SkillActivationRequirement skillActivationRequirement, int accumulatedPoints) {
        this.name = skillActivationRequirement.getName();
        this.kind = skillActivationRequirement.getKind();
        this.pointsNeededToActivate = skillActivationRequirement.getPointsNeededToActivate();
        this.accumulatedPoints = accumulatedPoints;
        this.displayText = skillActivationRequirement.getDisplayText();
    }

    public ActivatedSkill(SkillActivationRequirement skillActivationRequirement) {
        this.name = skillActivationRequirement.getName();
        this.kind = skillActivationRequirement.getKind();
        this.pointsNeededToActivate = skillActivationRequirement.getPointsNeededToActivate();
        this.accumulatedPoints =  0;
        this.displayText = skillActivationRequirement.getDisplayText();
    }

    public String getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }

    public int getPointsNeededToActivate() {
        return pointsNeededToActivate;
    }

    public int getAccumulatedPoints() {
        return accumulatedPoints;
    }

    public String getDisplayText() {
        return displayText;
    }

    @Override public String toString() {
        return "ActivatedSkill{" +
            "name='" + name + '\'' +
            ", kind='" + kind + '\'' +
            ", pointsNeededToActivate=" + pointsNeededToActivate +
            ", accumulatedPoints=" + accumulatedPoints +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ActivatedSkill)) {
            return false;
        }

        ActivatedSkill that = (ActivatedSkill) o;

        return kind.equals(that.kind);
    }

    @Override
    public int hashCode() {
        return kind.hashCode();
    }
}
