package armorsetsearch.skillactivation;

import models.ClassType;

public class SkillActivationRequirement {
    private int id;
    private String name;
    private String kind;
    private int pointsNeededToActivate;
    private ClassType classType;
    private boolean isNegativeSkill;

    // only for displaying purposes.
    // This is what will be shown on the UI
    private String displayText;

    private SkillActivationRequirement(){}

    public static SkillActivationRequirement Builder() {
        return new SkillActivationRequirement();
    }

    public SkillActivationRequirement setId(int id) {
        this.id = id;
        return this;
    }

    public int getId() {
        return id;
    }

    public SkillActivationRequirement setName(String name) {
        this.name = name.trim();
        return this;
    }

    public SkillActivationRequirement setKind(String kind) {
        this.kind = kind;
        return this;
    }

    public SkillActivationRequirement setPointsNeededToActivate(int pointsNeededToActivate) {
        this.pointsNeededToActivate = pointsNeededToActivate;
        return this;
    }

    public SkillActivationRequirement setClassType(ClassType classType) {
        this.classType = classType;
        return this;
    }

    public SkillActivationRequirement setIsNegativeSkill(boolean isNegativeSkill) {
        this.isNegativeSkill = isNegativeSkill;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public int getPointsNeededToActivate() {
        return pointsNeededToActivate;
    }

    public ClassType getClassType() {
        return classType;
    }

    public boolean isNegativeSkill() {
        return isNegativeSkill;
    }

    public String getDisplayText() {
        return displayText;
    }

    public SkillActivationRequirement setDisplayText(String displayText) {
        this.displayText = displayText;
        return this;
    }

    @Override
    public String toString() {
        return "SkillActivationRequirement{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", kind='" + kind + '\'' +
            ", pointsNeededToActivate=" + pointsNeededToActivate +
            ", classType=" + classType +
            ", isNegativeSkill=" + isNegativeSkill +
            ", displayText='" + displayText + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SkillActivationRequirement)) {
            return false;
        }

        SkillActivationRequirement that = (SkillActivationRequirement) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}