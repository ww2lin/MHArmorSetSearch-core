package armorsetsearch.charmsearch;

import armorsetsearch.armorsearch.thread.EquipmentList;
import armorsetsearch.armorsearch.thread.EquipmentNode;
import armorsetsearch.decorationsearch.DecorationSearch;
import armorsetsearch.decorationsearch.SkillChartWithDecoration;
import armorsetsearch.decorationsearch.SkillTable;
import armorsetsearch.skillactivation.ActivatedSkill;
import armorsetsearch.skillactivation.SkillActivationChart;
import armorsetsearch.skillactivation.SkillUtil;
import constants.Constants;
import constants.StringConstants;
import interfaces.OnSearchResultProgress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.ArmorSkill;
import models.CharmData;
import models.Decoration;
import models.GeneratedArmorSet;
import models.GeneratedCharm;

public class CharmSearch {
    private Map<String, List<CharmData>> charmLookupTable;
    private DecorationSearch decorationSearch;
    private OnSearchResultProgress onSearchResultProgress;
    private int searchLimit;
    private boolean stop = false;
    private final float maxProgress;
    private final float initProgress;
    private List<GeneratedArmorSet> results;

    public CharmSearch(List<GeneratedArmorSet> results, float initProgress, float maxProgress, int searchLimit, OnSearchResultProgress onSearchResultProgress, Map<String, List<CharmData>> charmLookupTable, DecorationSearch decorationSearch) {
        this.results = results;
        this.searchLimit = searchLimit;
        this.onSearchResultProgress = onSearchResultProgress;
        this.charmLookupTable = charmLookupTable;
        this.decorationSearch = decorationSearch;
        this.initProgress = initProgress;
        this.maxProgress = maxProgress;
    }

    public List<GeneratedArmorSet> findAValidCharmWithArmorSkill(EquipmentList equipmentList, List<ActivatedSkill> desiredSkills, int currentProgress) {
        List<MissingSkill> skillToMatchCharm = new ArrayList<>();

        float increment = maxProgress/(2 * equipmentList.size());
        int progress = 0;

        for (int i = 0; i < equipmentList.size(); ++i) {
            EquipmentNode equipmentNode = equipmentList.getEquipmentNodes().get(i);
            // find any skill that has not meet the desired skills yet.
            for (SkillTable skillTable : equipmentNode.getSkillTables()) {

                if (stop || results.size() >= searchLimit) {
                    return results;
                }

                Map<String, Integer> currentSkillTable = SkillActivationChart.add(skillTable.getSkillTable(), equipmentNode.getSkillTable());
                List<ActivatedSkill> currentActivatedSkill = SkillActivationChart.getActivatedSkills(currentSkillTable);

                if (SkillUtil.containsDesiredSkills(desiredSkills, currentActivatedSkill)) {
                    equipmentNode.setActivatedSkills(currentActivatedSkill);
                    equipmentNode.setSkillTable(currentSkillTable);
                    if (onSearchResultProgress != null) {
                        EquipmentNode newNode = SkillUtil.placeDecorations(equipmentNode, currentSkillTable, skillTable.getDecorations());
                        GeneratedArmorSet generatedArmorSet = new GeneratedArmorSet(newNode);
                        results.add(generatedArmorSet);
                        onSearchResultProgress.onProgress(generatedArmorSet);
                    }
                } else {
                    Map<String, Integer> missingSkill = SkillUtil.getMissingSkills(desiredSkills, currentSkillTable);
                    if (missingSkill.keySet().size() <= Constants.MAX_SLOTS + Constants.MAX_NUMBER_CHARM_SKILL) {
                        EquipmentNode newNode = SkillUtil.placeDecorations(equipmentNode, currentSkillTable, skillTable.getDecorations());
                        skillToMatchCharm.add(new MissingSkill(missingSkill, newNode));
                    }
                }
            }
            progress+=increment;
            if (onSearchResultProgress != null) {
                onSearchResultProgress.onProgress((int)initProgress+progress);
            }
        }

        /*
         * There is a lot of nested for loops, but that is alright.
         * Since most of he loops are bounded by a small constant.
         */
        for (int missingSkillSetCounter = 0; missingSkillSetCounter < skillToMatchCharm.size(); ++missingSkillSetCounter) {
            MissingSkill missingSkill = skillToMatchCharm.get(missingSkillSetCounter);
            if (stop || results.size() >= searchLimit) {
                return results;
            }
            progress+=increment;
            // go thru the slots
            SlotsLoop:
            for (int slotNumber = 1; slotNumber <= Constants.MAX_SLOTS; ++slotNumber) {
                if (onSearchResultProgress != null) {
                    onSearchResultProgress.onProgress((int) initProgress + progress);
                }

                // Check if solution exist for 1...n slots
                // If a set exist such that slotNumber < n, where  1<slotNumber<n. Then we can exit early for this set.
                List<SkillChartWithDecoration> skillChartWithDecorationsToTry = decorationSearch.getSkillListBySlot(missingSkill.missingSkillsMap.keySet(), slotNumber);

                for (SkillChartWithDecoration skillChartWithDecoration : skillChartWithDecorationsToTry) {
                    if (stop || results.size() >= searchLimit) {
                        return results;
                    }

                    // See if we can get by with just slots.
                    Map<String, Integer> onlyDecorationCharm = SkillActivationChart.add(skillChartWithDecoration.getSkillChart(), missingSkill.equipmentNode.getSkillTable());
                    List<ActivatedSkill> activatedSkills = SkillActivationChart.getActivatedSkills(onlyDecorationCharm);
                    if (SkillUtil.containsDesiredSkills(desiredSkills, activatedSkills)) {

                        GeneratedCharm generatedCharm = new GeneratedCharm(StringConstants.ANY_CHARM_WITH_SLOTS + slotNumber, Collections.emptyList(), skillChartWithDecoration.getDecorations(), slotNumber);
                        GeneratedArmorSet generatedArmorSet = new GeneratedArmorSet(missingSkill.equipmentNode, generatedCharm, onlyDecorationCharm);
                        results.add(generatedArmorSet);
                        if (onSearchResultProgress != null) {
                            onSearchResultProgress.onProgress(generatedArmorSet);
                        }
                        break SlotsLoop;
                    }

                    HashMap<String, Integer> leftOverSkills = new HashMap<>(missingSkill.missingSkillsMap);
                    for (Decoration decoration : skillChartWithDecoration.getDecorations()) {
                        for (ArmorSkill armorSkill : decoration.getArmorSkills()) {
                            Integer points = leftOverSkills.get(armorSkill.kind);
                            if (points != null) {
                                int leftOverPoints = points - armorSkill.points;
                                if (leftOverPoints > 0) {
                                    leftOverSkills.put(armorSkill.kind, leftOverPoints);
                                } else {
                                    leftOverSkills.remove(armorSkill.kind);
                                }
                            }
                        }
                    }

                    if (leftOverSkills.keySet().size() <= Constants.MAX_NUMBER_CHARM_SKILL) {
                        List<List<CharmData>> charmsBySkills = new ArrayList<>();
                        for (String skillKind : leftOverSkills.keySet()) {
                            if (!missingSkill.missingSkillsMap.keySet().contains(skillKind)) {
                                // skip negative skills or any other skill that is caused by decoration negative offsets
                                continue;
                            }
                            List<CharmData> charmsWithDesireSkill = charmLookupTable.get(skillKind);
                            if (charmsWithDesireSkill != null) {
                                // check that charm with such skill exist
                                charmsBySkills.add(charmsWithDesireSkill);
                            }
                        }

                        // Check if we only need one skill
                        if (charmsBySkills.size() == 1 && leftOverSkills.keySet().size() == 1) {
                            for (CharmData charmData : charmsBySkills.get(0)) {
                                int pointsNeeded = leftOverSkills.get(charmData.getSkillkind());
                                for (CharmData.CharmPoint charmPoint : charmData.getCharmPoints()) {
                                    if (pointsNeeded < charmPoint.getMax()) {
                                        GeneratedCharm.CharmSkill charmSkill = new GeneratedCharm.CharmSkill(charmData.getSkillkind(), pointsNeeded, charmPoint.getSkillPosition());
                                        GeneratedCharm generatedCharm = buildGeneratedCharm(charmData.getCharmType(), skillChartWithDecoration.getDecorations(), slotNumber, charmSkill);
                                        GeneratedArmorSet generatedArmorSet = buildGeneratedArmorSet(generatedCharm, missingSkill);
                                        results.add(generatedArmorSet);
                                        if (onSearchResultProgress != null) {
                                            onSearchResultProgress.onProgress(generatedArmorSet);
                                        }
                                    }
                                }
                            }
                        }
                        // NOTE: If the game ever gave charm more than 2 skills, this will have to be changed.
                        for (int j = 0; j < charmsBySkills.size(); ++j) {
                            for (int k = j + 1; j != k && k < charmsBySkills.size(); ++k) {
                                List<CharmData> charmDataList1 = charmsBySkills.get(j);
                                List<CharmData> charmDataList2 = charmsBySkills.get(k);

                                for (CharmData charmData1 : charmDataList1) {
                                    for (CharmData charmData2 : charmDataList2) {
                                        if (charmData1.isSameCharmType(charmData2)) {
                                            // If we found a working set with charm, then break. and try the next set,
                                            // as there is no need to brute force all the charms with decorations
                                            for (CharmData.CharmPoint charmPoint1 : charmData1.getCharmPoints()) {
                                                for (CharmData.CharmPoint charmPoint2 : charmData2.getCharmPoints()) {
                                                    if (charmPoint1.getSkillPosition() != charmPoint2.getSkillPosition()) {
                                                        int skillPointsNeeded1 = leftOverSkills.get(charmData1.getSkillkind());
                                                        int skillPointsNeeded2 = leftOverSkills.get(charmData2.getSkillkind());

                                                        if (skillPointsNeeded1 <= charmPoint1.getMax() &&
                                                            skillPointsNeeded2 <= charmPoint2.getMax()) {

                                                            GeneratedCharm.CharmSkill charmSkill1 = new GeneratedCharm.CharmSkill(charmData1.getSkillkind(), skillPointsNeeded1, charmPoint1.getSkillPosition());
                                                            GeneratedCharm.CharmSkill charmSkill2 = new GeneratedCharm.CharmSkill(charmData2.getSkillkind(), skillPointsNeeded2, charmPoint2.getSkillPosition());

                                                            GeneratedCharm generatedCharm = buildGeneratedCharm(charmData1.getCharmType(), skillChartWithDecoration.getDecorations(), slotNumber, charmSkill1, charmSkill2);

                                                            GeneratedArmorSet generatedArmorSet = buildGeneratedArmorSet(generatedCharm, missingSkill);
                                                            results.add(generatedArmorSet);

                                                            if (onSearchResultProgress != null) {
                                                                onSearchResultProgress.onProgress(generatedArmorSet);
                                                            }

                                                            break SlotsLoop;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    private GeneratedArmorSet buildGeneratedArmorSet(GeneratedCharm generatedCharm, MissingSkill missingSkill) {
        // TODO, change the multiplier into based on mantis set.
        Map<String, Integer> charmSkillTable = SkillActivationChart.getSkillChart(generatedCharm, 1);
        Map<String, Integer> totalSkillTable = SkillActivationChart.add(missingSkill.equipmentNode.getSkillTable(), charmSkillTable);

        return new GeneratedArmorSet(missingSkill.equipmentNode, generatedCharm, totalSkillTable);
    }

    private GeneratedCharm buildGeneratedCharm(String charmType, List<Decoration> decorations, int slotNumber, GeneratedCharm.CharmSkill... charmSkills) {
        List<GeneratedCharm.CharmSkill> charm = new ArrayList<>(2);
        charm.addAll(Arrays.asList(charmSkills));
        return new GeneratedCharm(charmType, charm, decorations, slotNumber);
    }

    public void stop() {
        this.stop = true;
    }

    private class MissingSkill {
        // desiredSkill -> how many points is missing.
        Map<String, Integer> missingSkillsMap;
        EquipmentNode equipmentNode;

        public MissingSkill(Map<String, Integer> missingSkillsMap, EquipmentNode equipmentNode) {
            this.missingSkillsMap = missingSkillsMap;
            this.equipmentNode = equipmentNode;
        }
    }
}
