package models;

import interfaces.Nullable;
import constants.StringConstants;

public class ArmorSkill implements Nullable {

    // Note this is NOT the name of the skill, rather its the 'kind' of the skill
    // E.g its NOT AuS, AuM, or negate stun,
    // it is Attack, Poison, Stun, Hearing
    public String kind;

    // Can be positive or negative
    public int points;

    private ArmorSkill(String kind, int points) {
        this.kind = kind;
        this.points = points;
    }

    public static ArmorSkill createArmorSkill(String kind, int points) {
        if (kind == null || kind.isEmpty()) {
            return null;
        }
        return new ArmorSkill(kind.trim(), points);
    }

    @Override
    public String toString() {
        return "ArmorSkill{" +
            "kind='" + kind + '\'' +
            ", points=" + points +
            '}';
    }

    public boolean isKind(String kind){
        return this.kind.equalsIgnoreCase(kind);
    }

    public boolean isPositive() {
        return points > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArmorSkill)) {
            return false;
        }

        ArmorSkill that = (ArmorSkill) o;

        if (points != that.points) {
            return false;
        }
        return kind.equals(that.kind);
    }

    @Override public int hashCode() {
        int result = kind.hashCode();
        result = 31 * result + points;
        return result;
    }

    @Override
    public boolean isNull() {
        return kind == null || kind.trim().length() == 0;
    }

    public boolean isTorsoUp(){
       return (points == 0 && !isNull()) || kind.equalsIgnoreCase(StringConstants.ARMOR_SKILL_TORSO_UP) ;
    }
}
