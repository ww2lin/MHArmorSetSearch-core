package models;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Equipment {
    private static final int NOT_AVAILABLE = 99;

    private int id;
    private String name;
    private Gender gender;
    private ClassType classType;
    private int rarity;
    private int slots;
    // 99 means you cant get it.
    private int onlineMonsterAvailableAtQuestLevel;
    private int villageMonsterAvailableAtQuestLevel;

    // Cant read japanese, guessing this means if you need to do both online/offline quest
    private boolean needBothOnlineAndOffLineQuest;

    private int baseDefense;
    private int maxDefense;

    private List<Resistance> resistances = Collections.emptyList();

    private Set<ArmorSkill> armorSkills = Collections.emptySet();
    private Set<ItemPart> itemParts = Collections.emptySet();

    // State variable, not from the CSV
    private int slotsUsed;
    // Maps: Decoration -> frequency/Count of this jewel
    private Map<Decoration, Integer> decorations = new HashMap<>();
    private boolean isTorsoUp;
    private EquipmentType equipmentType;
    private boolean canBeSubstitutedForAnyOtherThreeSlotEquipment;

    private Equipment(){}

    public Equipment(Equipment other) {
        this.id = other.id;
        this.name = other.name;
        this.gender = other.gender;
        this.classType = other.classType;
        this.rarity = other.rarity;
        this.slots = other.slots;
        this.onlineMonsterAvailableAtQuestLevel = other.onlineMonsterAvailableAtQuestLevel;
        this.villageMonsterAvailableAtQuestLevel = other.villageMonsterAvailableAtQuestLevel;
        this.needBothOnlineAndOffLineQuest = other.needBothOnlineAndOffLineQuest;
        this.baseDefense = other.baseDefense;
        this.maxDefense = other.maxDefense;
        this.resistances = other.resistances;
        this.armorSkills = other.armorSkills;
        this.itemParts = other.itemParts;
        this.slotsUsed = other.slotsUsed;
        this.decorations.putAll(other.decorations);
        this.isTorsoUp = other.isTorsoUp;
        this.equipmentType = other.equipmentType;
        this.canBeSubstitutedForAnyOtherThreeSlotEquipment = other.canBeSubstitutedForAnyOtherThreeSlotEquipment;
        this.slotsUsed = other.slotsUsed;
    }

    public static Equipment Builder(){
        return new Equipment();
    }

    @Override public String toString() {
        return "Equipment{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", gender=" + gender +
            ", classType=" + classType +
            ", rarity=" + rarity +
            ", slots=" + slots +
            ", onlineMonsterAvailableAtQuestLevel=" + onlineMonsterAvailableAtQuestLevel +
            ", villageMonsterAvailableAtQuestLevel=" + villageMonsterAvailableAtQuestLevel +
            ", needBothOnlineAndOffLineQuest=" + needBothOnlineAndOffLineQuest +
            ", baseDefense=" + baseDefense +
            ", maxDefense=" + maxDefense +
            ", resistances=" + resistances +
            ", armorSkills=" + armorSkills +
            ", itemParts=" + itemParts +
            ", slotsUsed=" + slotsUsed +
            ", decorations=" + decorations +
            ", isTorsoUp=" + isTorsoUp +
            ", equipmentType=" + equipmentType +
            ", canBeSubstitutedForAnyOtherThreeSlotEquipment=" + canBeSubstitutedForAnyOtherThreeSlotEquipment +
            '}';
    }

    public String getName() {
        return name;
    }

    public Equipment setName(String name) {
        this.name = name;
        return this;
    }

    public Gender getGender() {
        return gender;
    }

    public Equipment setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public ClassType getClassType() {
        return classType;
    }

    public Equipment setClassType(ClassType classType) {
        this.classType = classType;
        return this;
    }

    public int getRarity() {
        return rarity;
    }

    public Equipment setRarity(int rarity) {
        this.rarity = rarity;
        return this;
    }

    public int getSlots() {
        return slots;
    }

    public int getSlotsUsed() {
        return slotsUsed;
    }

    public Equipment setSlots(int slots) {
        this.slots = slots;
        return this;
    }

    public int getOnlineMonsterAvailableAtQuestLevel() {
        return onlineMonsterAvailableAtQuestLevel;
    }

    public Equipment setOnlineMonsterAvailableAtQuestLevel(int onlineMonsterAvailableAtQuestLevel) {
        this.onlineMonsterAvailableAtQuestLevel = onlineMonsterAvailableAtQuestLevel;
        return this;
    }

    public int getVillageMonsterAvailableAtQuestLevel() {
        return villageMonsterAvailableAtQuestLevel;
    }

    public Equipment setVillageMonsterAvailableAtQuestLevel(int villageMonsterAvailableAtQuestLevel) {
        this.villageMonsterAvailableAtQuestLevel = villageMonsterAvailableAtQuestLevel;
        return this;
    }

    public boolean isNeedBothOnlineAndOffLineQuest() {
        return needBothOnlineAndOffLineQuest;
    }

    public Equipment setNeedBothOnlineAndOffLineQuest(boolean needBothOnlineAndOffLineQuest) {
        this.needBothOnlineAndOffLineQuest = needBothOnlineAndOffLineQuest;
        return this;
    }

    public int getBaseDefense() {
        return baseDefense;
    }

    public Equipment setBaseDefense(int baseDefense) {
        this.baseDefense = baseDefense;
        return this;
    }

    public int getMaxDefense() {
        return maxDefense;
    }

    public Equipment setMaxDefense(int maxDefense) {
        this.maxDefense = maxDefense;
        return this;
    }

    public List<Resistance> getResistances() {
        return resistances;
    }

    public Equipment setResistances(List<Resistance> resistances) {
        this.resistances = resistances;
        return this;
    }

    public Equipment setArmorSkills(Set<ArmorSkill> armorSkills) {
        this.armorSkills = armorSkills;
        return this;
    }

    public Equipment setItemParts(Set<ItemPart> itemParts) {
        this.itemParts = itemParts;
        return this;
    }

    public Equipment setTorsoUp(boolean torsoUp) {
        isTorsoUp = torsoUp;
        return this;
    }

    public Set<ArmorSkill> getArmorSkills() {
        return armorSkills;
    }

    public Set<ItemPart> getItemParts() {
        return itemParts;
    }

    public boolean isAvailable(){
        return onlineMonsterAvailableAtQuestLevel != NOT_AVAILABLE || villageMonsterAvailableAtQuestLevel != NOT_AVAILABLE;
    }

    public Map<Decoration, Integer> getDecorations() {
        return decorations;
    }

    public boolean isTorsoUp() {
        return isTorsoUp;
    }

    public EquipmentType getEquipmentType() {
        return equipmentType;
    }

    public Equipment setEquipmentType(EquipmentType equipmentType) {
        this.equipmentType = equipmentType;
        return this;
    }

    public int getId() {
        return id;
    }

    public Equipment setId(int id) {
        this.id = id;
        return this;
    }

    public int getSlotsNeeded(){
        return slotsUsed;
    }

    public boolean isCanBeSubstitutedForAnyOtherThreeSlotEquipment() {
        return canBeSubstitutedForAnyOtherThreeSlotEquipment;
    }

    public Equipment setCanBeSubstitutedForAnyOtherThreeSlotEquipment(boolean canBeSubstitutedForAnyOtherThreeSlotEquipment) {
        this.canBeSubstitutedForAnyOtherThreeSlotEquipment = canBeSubstitutedForAnyOtherThreeSlotEquipment;
        return this;
    }

    public void removeAllDecorations(){
        decorations.clear();
        slotsUsed = 0;
    }

    public void removeDecoration(Decoration decoration){
        Integer frequency = decorations.get(decoration);
        if (frequency == null || frequency <= 0){
            return;
        }
        --frequency;
        if (frequency == 0){
            decorations.remove(decoration);
        } else {
            decorations.put(decoration, frequency);
        }
        slotsUsed-=decoration.getSlotsNeeded();
    }

    public void addDecoration(Decoration decoration){
        Integer frequency = decorations.get(decoration);
        if (frequency == null){
            frequency = 0;
        }
        ++frequency;
        slotsUsed+=decoration.getSlotsNeeded();
        decorations.put(decoration, frequency);
    }

    public void addAllDecorations(List<Decoration> decorations){
        for (Decoration decoration : decorations){
            addDecoration(decoration);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Equipment)) {
            return false;
        }

        Equipment equipment = (Equipment) o;

        if (equipmentType != equipment.equipmentType) {
            return false;
        }
        return id == equipment.id;
    }

    @Override
    public int hashCode() {
        int result = equipmentType.hashCode();
        result = 31 * result + id;
        return result;
    }



}
