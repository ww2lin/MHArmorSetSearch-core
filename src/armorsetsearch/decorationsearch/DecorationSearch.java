package armorsetsearch.decorationsearch;

import armorsetsearch.armorsearch.thread.EquipmentList;
import armorsetsearch.armorsearch.thread.EquipmentNode;
import armorsetsearch.skillactivation.SkillUtil;
import constants.Constants;
import interfaces.OnSearchResultProgress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import models.ArmorSkill;
import models.Decoration;
import models.Equipment;
import armorsetsearch.skillactivation.ActivatedSkill;
import armorsetsearch.skillactivation.SkillActivationChart;
import models.EquipmentType;
import models.GeneratedArmorSet;
import util.StopWatch;

public class DecorationSearch {

    private Map<Integer, List<Decoration>> slotToDecorationMap = new HashMap<>();
    private SkillChartDataList[] decorationSkillTable;
    private Map<CombinationDecorationHashKey, SkillTables> decorationCombinationCache = new HashMap<>();

    private Map<Integer, Map<Integer, AllDecorationPossibleForGivenSlot>> slotsToAllCombinationSlot;
    private OnSearchResultProgress onSearchResultProgress;
    private boolean stop = false;
    private final float initProgress;
    private final float maxProgress;
    private final int uniqueSetSearchLimit;
    private List<GeneratedArmorSet> results;

    public DecorationSearch(List<GeneratedArmorSet> results, float initProgress, float maxProgress, int uniqueSetSearchLimit, OnSearchResultProgress onSearchResultProgress, List<ActivatedSkill> desiredSkills, Map<String, List<Decoration>> decorationLookupTable) {
        this.results = results;
        this.onSearchResultProgress = onSearchResultProgress;
        this.initProgress = initProgress;
        this.maxProgress = maxProgress;
        this.uniqueSetSearchLimit = uniqueSetSearchLimit;

        for (ActivatedSkill activatedSkill : desiredSkills) {
            List<Decoration> decorations = decorationLookupTable.get(activatedSkill.getKind());
            if (decorations != null) {
                for (Decoration decoration : decorations) {
                    if (!decoration.isAvailable() || !decoration.isPositive(activatedSkill.getKind())) {
                        // skip negative or not available jewels.
                        continue;
                    }
                    List<Decoration> sameSlotDecorations = slotToDecorationMap.get(decoration.getSlotsNeeded());
                    if (sameSlotDecorations == null) {
                        sameSlotDecorations = new ArrayList<>();
                    }

                    // Filter out the jewels that is inferior or duplicated.
                    sameSlotDecorations.add(decoration);
                    slotToDecorationMap.put(decoration.getSlotsNeeded(), sameSlotDecorations);
                }
            }
        }
        StopWatch stopWatch = new StopWatch();
        System.out.println("Building Decoration data");

        decorationSkillTable = initDecorationSkillChart();
        stopWatch.printMsgAndResetTime("Finish building decoration table");
        slotsToAllCombinationSlot = buildDecorationCombination();
        stopWatch.printMsgAndResetTime("Decoration slot Combination finished");
    }

    /**
     * Find all the decoration that has the skillkind
     *
     * @param skillKinds
     * @param slots
     * @return
     */
    public List<SkillChartWithDecoration> getSkillListBySlot(Set<String> skillKinds, int slots) {
        SkillChartDataList skillChartDataList = decorationSkillTable[slots];
        if (skillChartDataList != null) {
            // Only return the result with a decoration that has the wanted skill.
            return skillChartDataList.getSkillChartWithDecorations().stream().filter(skillChartWithDecoration -> {
                int decorationWithDesireSkill = 0;
                for (Decoration decoration : skillChartWithDecoration.decorations) {
                    for (ArmorSkill armorSkill : decoration.getArmorSkills()) {
                        if (skillKinds.contains(armorSkill.kind) && armorSkill.isPositive()) {
                            ++decorationWithDesireSkill;
                        }
                    }
                }
                return decorationWithDesireSkill == skillChartWithDecoration.decorations.size();
            }).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    /**
     * Use DP to build a decoration skill chart based on desired skill (decorations)
     *
     * @return DP table use to look up skill chart by slot number.
     */
    private SkillChartDataList[] initDecorationSkillChart() {
        final int slots = Constants.MAX_SLOTS;
        SkillChartDataList[] table = new SkillChartDataList[slots + 1];

        // base case:
        // no slots, then no skill activated from slots.
        table[0] = new SkillChartDataList(Collections.emptyList());

        // iterative case:
        // use all the i-th slots
        for (int i = 1; i <= slots; ++i) {
            SkillChartDataList skillChartDataList = new SkillChartDataList();
            List<Decoration> decorations = slotToDecorationMap.get(i);
            if (decorations != null) {
                decorations.forEach(decoration -> {
                    Map<String, Integer> skillChart = SkillActivationChart.getSkillChart(decoration);
                    List<Decoration> useDecoration = new ArrayList<>();
                    useDecoration.add(decoration);
                    skillChartDataList.add(new SkillChartWithDecoration(useDecoration, skillChart));
                });
            }

            // calculate sub-problems, e.g all the sub slots that makes slot i
            // divide the i by two to get rid of mirror decorations.  e.g Attack [2] Stun[1] is the same as Stun[1] Attack[2]
            for (int j = 1; i > j && j <= Math.ceil((float) i / 2); ++j) {

                SkillChartDataList subProblem1 = table[i - j];
                SkillChartDataList subProblem2 = table[j];


                // The beginning of the sub problem depends on the current decoration
                // For example if the current decoration is only available in slots 1 and n, then for
                // the n sub problem we can skip all the scenario where we fill n slots with decoration that uses 1 slot.
                // Assuming the decoration uses n slots gives more skill points than "n x decoration that use 1 slot".
                subProblem1 = new SkillChartDataList(subProblem1.getSkillChartWithDecorations().stream().filter(skillChartWithDecoration -> shouldKeep(decorations, skillChartWithDecoration)).collect(Collectors.toList()));
                subProblem2 = new SkillChartDataList(subProblem2.getSkillChartWithDecorations().stream().filter(skillChartWithDecoration -> shouldKeep(decorations, skillChartWithDecoration)).collect(Collectors.toList()));

                SkillChartDataList result = SkillChartDataList.cartesianProduct(subProblem1, subProblem2);

                skillChartDataList.addAll(result);
            }

            table[i] = skillChartDataList;
        }
        return table;
    }

    private Map<Integer, Map<Integer, AllDecorationPossibleForGivenSlot>> buildDecorationCombination(){
        // 3 layer of list/map
        // third layer map: consider 2 slots with 1 slot needed decorations. Map<Decoration, count>
        //              (Atk, Atk) -> (Atk, 2)
        // second layer: we have three armors which needs 2 slots
        //              (Atk, Def), (Evade, Health), (Guard, SpeedEating)
        // first layer: we have all the combinations of of the jewel that uses 2 slots
        //               (Atk, Def), (Evade, Health), (Guard, SpeedEating)
        //              (Atk, Sharpness), (Evade, Health), (Guard, SpeedEating)

        // Two layer map
        // Slots number -> Number Of equipment with Slot i -> the decorations from above.
        Map<Integer, Map<Integer, AllDecorationPossibleForGivenSlot>> slotsToAllCombinationSlot = new HashMap<>();
        for (int i = 1; i <= Constants.MAX_SLOTS; ++i) {
            SkillChartDataList skillChartDataList = decorationSkillTable[i];

            if (skillChartDataList != null) {
                Map<Integer, AllDecorationPossibleForGivenSlot> slotToDecoration = new HashMap<>();
                for (int j = 1; j <= EquipmentType.values().length; ++j) {
                    AllDecorationPossibleForGivenSlot results = new AllDecorationPossibleForGivenSlot();
                    getDecorationBySlotsNumber(j, skillChartDataList, results, 0, new ArrayList<>());
                    slotToDecoration.put(j, results);
                }
                slotsToAllCombinationSlot.put(i, slotToDecoration);
            }
        }
        return slotsToAllCombinationSlot;
    }

    private void getDecorationBySlotsNumber(int n, SkillChartDataList skillChartDataList, AllDecorationPossibleForGivenSlot results, int index, List<List<Decoration>> accumulatedDecorations){
        if (n > 0) {
            for (int i = index; i < skillChartDataList.size(); ++i) {
                SkillChartWithDecoration skillChartWithDecoration = skillChartDataList.get(i);
                accumulatedDecorations.add(skillChartWithDecoration.decorations);
                getDecorationBySlotsNumber(n - 1, skillChartDataList, results, index++, accumulatedDecorations);
                accumulatedDecorations.remove(skillChartWithDecoration.decorations);
            }
        } else {
            // Deep copy the accumulated decorations to prevent backtracking from modifying it.
            results.decorationForListOfEquipments.add(new DecorationForListOfEquipment(accumulatedDecorations));

        }
    }

    /**
     * Ugly mess, clean this up later.
     * @param decorationsWithSameSlot
     * @param skillChartWithDecoration
     * @return
     */
    private boolean shouldKeep(List<Decoration> decorationsWithSameSlot, SkillChartWithDecoration skillChartWithDecoration) {
        if (decorationsWithSameSlot == null) {
            return true;
        }
        int size = skillChartWithDecoration.decorations.size();
        for (int i = 0; i < size; ++i) {
            Decoration decoration1 = skillChartWithDecoration.decorations.get(i);
            for (int j = 0; j < size; ++j) {
                Decoration decoration2 = skillChartWithDecoration.decorations.get(j);
                if (i != j && !decoration1.equals(decoration2)) {
                    return true;
                }
            }
        }
        Decoration sameDecoration = skillChartWithDecoration.decorations.get(0);
        for (Decoration decoration : decorationsWithSameSlot) {
            for (ArmorSkill armorSkill : decoration.getArmorSkills()) {
                for (ArmorSkill armorSkill1 : sameDecoration.getArmorSkills()) {
                    if (armorSkill.isKind(armorSkill1.kind)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public EquipmentList buildEquipmentWithDecorationSkillTable(EquipmentList equipmentList, List<ActivatedSkill> desiredSkills) {
        float increment = maxProgress / equipmentList.size();
        float progress = initProgress;

        for (int i = 0; i < equipmentList.size(); ++i) {
            EquipmentNode equipmentNode = equipmentList.getEquipmentNodes().get(i);
            List<Equipment> equipments = equipmentNode.getEquipments();

            int[] slotCountByEquipment = new int[Constants.MAX_SLOTS+1];
            int differentSlotCount = 0;
            for (Equipment equipment : equipments) {
                int slots = equipment.getSlots();
                if (slots > 0) {
                    slotCountByEquipment[slots]++;
                }
            }
            CombinationDecorationHashKey combinationDecorationHashKey = new CombinationDecorationHashKey(slotCountByEquipment);
            SkillTables skillTables = decorationCombinationCache.get(combinationDecorationHashKey);
            if (skillTables == null) {
                // Use memorization to remember the problems that we already solved.
                List<AllDecorationPossibleForGivenSlot> combinationDecorations = new ArrayList<>();
                for (int j = 1; j < slotCountByEquipment.length; ++j) {
                    int slot = slotCountByEquipment[j];
                    if (slot > 0 && slotsToAllCombinationSlot.get(j) != null) {
                        AllDecorationPossibleForGivenSlot combinationDecoration = slotsToAllCombinationSlot.get(j).get(slot);
                        if (combinationDecoration != null) {
                            ++differentSlotCount;
                            combinationDecorations.add(combinationDecoration);
                        }
                    }
                }
                skillTables = buildDecorationTable(differentSlotCount, combinationDecorations);
                decorationCombinationCache.put(combinationDecorationHashKey, skillTables);
            }


            SkillTables filteredTable = new SkillTables(skillTables);

            SkillTableLoop:for (int j = 0; j < filteredTable.size(); ++j) {
                SkillTable skillTable = filteredTable.get(j);

                Map<String, Integer> currentTable = SkillActivationChart.add(equipmentNode.getSkillTable(), skillTable.getSkillTable());

                Map<String, Integer> missingSkillTable = SkillUtil.getMissingSkills(desiredSkills, currentTable);
                int sum = 0;
                for (Integer integer : missingSkillTable.values()) {
                    sum+=integer;
                    if (sum >= Constants.MAX_CHARM_SKILL_POINT) {
                        filteredTable.remove(j);
                        --j;
                        continue SkillTableLoop;
                    }
                }

                // See if we have found a set
                List<ActivatedSkill> activatedSkills = SkillActivationChart.getActivatedSkills(currentTable);
                if (SkillUtil.containsDesiredSkills(desiredSkills, activatedSkills)) {
                    EquipmentNode newNode = SkillUtil.placeDecorations(equipmentNode, currentTable, skillTable.getDecorations());
                    GeneratedArmorSet generatedArmorSet = new GeneratedArmorSet(newNode);
                    results.add(generatedArmorSet);
                    onSearchResultProgress.onProgress(generatedArmorSet);
                    break;
                }
            }

            equipmentNode.setSkillTables(skillTables);
            progress+=increment;
            if (onSearchResultProgress != null) {
                onSearchResultProgress.onProgress((int)progress);
            }

            if (stop || results.size() > uniqueSetSearchLimit) {
                return equipmentList;
            }

        }
        return  equipmentList;
    }


    private SkillTables buildDecorationTable(int differentSlotCount, List<AllDecorationPossibleForGivenSlot> combinationDecorations){
        SkillTables[] table = new SkillTables[differentSlotCount+1];

        //Base case:
        table[0] = new SkillTables(new ArrayList<>());

        for (int i = 1; i < table.length; ++i) {
            AllDecorationPossibleForGivenSlot allDecorationPossibleForGivenSlot = combinationDecorations.get(i-1);
            SkillTables currentTable = new SkillTables();
            SkillTables previousTable = table[i-1];

            // calculate the skilltable.
            for (DecorationForListOfEquipment decorationForListOfEquipments : allDecorationPossibleForGivenSlot.decorationForListOfEquipments) {
                List<DecorationForOneEquipment> decorationForOneEquipments = decorationForListOfEquipments.decorationForOneEquipments;
                Map<String, Integer> skilltable = new HashMap<>();

                for (DecorationForOneEquipment decorationForOneEquipment : decorationForOneEquipments) {
                    SkillActivationChart.updateSkillChartByDecoration(skilltable, decorationForOneEquipment.decorations, 1);
                }
                currentTable.add(new SkillTable(skilltable, decorationForOneEquipments));
            }

            // add the current skilltable to the previous one.
            SkillTables newTable = SkillTables.cartesianProduct(previousTable, currentTable);
            table[i-1] = null;
            table[i] = newTable;
        }
        return table[differentSlotCount];
    }

    public void stop() {
        this.stop = true;
    }

    private class CombinationDecorationHashKey {
        int[] slotCountByEquipment;

        public CombinationDecorationHashKey(int[] slotCountByEquipment) {
            this.slotCountByEquipment = slotCountByEquipment;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CombinationDecorationHashKey)) {
                return false;
            }

            CombinationDecorationHashKey that = (CombinationDecorationHashKey) o;

            return Arrays.equals(slotCountByEquipment, that.slotCountByEquipment);
        }

        @Override public int hashCode() {
            return Arrays.hashCode(slotCountByEquipment);
        }
    }

    private class AllDecorationPossibleForGivenSlot {
        List<DecorationForListOfEquipment> decorationForListOfEquipments = new ArrayList<>();

        @Override public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AllDecorationPossibleForGivenSlot)) {
                return false;
            }

            AllDecorationPossibleForGivenSlot that = (AllDecorationPossibleForGivenSlot) o;

            return decorationForListOfEquipments.equals(that.decorationForListOfEquipments);
        }

        @Override
        public int hashCode() {
            return decorationForListOfEquipments.hashCode();
        }
    }

    private class DecorationForListOfEquipment{
        List<DecorationForOneEquipment> decorationForOneEquipments = new ArrayList<>();

        public DecorationForListOfEquipment(List<List<Decoration>> decorations) {
            for (List<Decoration> decorationList : decorations) {
                decorationForOneEquipments.add(new DecorationForOneEquipment(decorationList));
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DecorationForListOfEquipment)) {
                return false;
            }

            DecorationForListOfEquipment that = (DecorationForListOfEquipment) o;

            return decorationForOneEquipments.equals(that.decorationForOneEquipments);
        }

        @Override public int hashCode() {
            return decorationForOneEquipments.hashCode();
        }
    }

    public static class DecorationForOneEquipment {
        List<Decoration> decorations;

        public DecorationForOneEquipment(List<Decoration> decorations) {
            this.decorations = decorations;
        }

        public List<Decoration> getDecorations() {
            return decorations;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DecorationForOneEquipment)) {
                return false;
            }

            DecorationForOneEquipment that = (DecorationForOneEquipment) o;

            return decorations.equals(that.decorations);
        }

        @Override public int hashCode() {
            return decorations.hashCode();
        }
    }
}

