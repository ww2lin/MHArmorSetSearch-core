package armorsetsearch;

import armorsetsearch.armorsearch.ArmorSearch;
import armorsetsearch.armorsearch.thread.EquipmentList;
import armorsetsearch.charmsearch.CharmSearch;
import armorsetsearch.decorationsearch.DecorationSearch;
import armorsetsearch.filter.ArmorFilter;
import armorsetsearch.filter.ArmorSetFilter;
import armorsetsearch.skillactivation.SkillUtil;
import interfaces.OnSearchResultProgress;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import models.CharmData;
import models.ClassType;
import models.Decoration;
import models.Gender;
import models.GeneratedArmorSet;
import armorsetsearch.skillactivation.ActivatedSkill;
import armorsetsearch.skillactivation.SkillActivationChart;
import armorsetsearch.skillactivation.SkillActivationRequirement;
import util.StopWatch;

public class ArmorSearchWrapper {

    private AllEquipments allEquipments;
    private Map<String, List<SkillActivationRequirement>> skillActivationChartMap;
    private Map<String, List<Decoration>> decorationLookupTable;
    private Map<String, List<CharmData>> charmLookupTable;
    private SkillActivationChart skillActivationChart;
    private ArmorSkillCacheTable armorSkillCacheTable;

    private Gender gender;
    private ClassType classType;
    private List<ArmorFilter> armorFilters = Collections.emptyList();
    private ArmorSearch armorSearch;
    private CharmSearch charmSearch;
    private DecorationSearch decorationSearch;

    private int weapSlot = 0;

    public ArmorSearchWrapper(AllEquipments allEquipments, Map<String, List<SkillActivationRequirement>> skillActivationChartMap, Map<String, List<Decoration>> decorationLookupTable, Map<String, List<CharmData>> charmLookupTable) throws IOException {
        this.allEquipments = allEquipments;
        this.skillActivationChartMap = skillActivationChartMap;
        this.decorationLookupTable = decorationLookupTable;
        this.charmLookupTable = charmLookupTable;
    }

    public List<SkillActivationRequirement> getPositiveSkillList(){
        List<SkillActivationRequirement> skillList = new ArrayList<>();
        skillActivationChartMap.values().forEach(skillList::addAll);
        return skillList.stream().filter(sar ->
            sar.getPointsNeededToActivate() > 0 && (sar.getClassType() == classType || sar.getClassType() ==ClassType.ALL)
        ).collect(Collectors.toList());
    }

    public List<GeneratedArmorSet> search(List<ArmorSetFilter> armorSetFilters,
                                          List<SkillActivationRequirement> desiredSkills,
                                          final int uniqueSetSearchLimit,
                                          final int decorationSearchLimit,
                                          OnSearchResultProgress onSearchResultProgress) {
        if (!SkillUtil.shouldDoSearch(desiredSkills)) {
            return Collections.emptyList();
        }

        List<ActivatedSkill> activatedSkills = new ArrayList<>(desiredSkills.size());

        desiredSkills.forEach(skillActivationRequirement -> {
            activatedSkills.add(new ActivatedSkill(skillActivationRequirement));
        });


        float progressChunk = 100 / 3;
        float progress = 0;
        List<GeneratedArmorSet> generatedArmorSets = new ArrayList<>();
        StopWatch stopWatch = new StopWatch();

        System.out.println("Filtering Equipments");
        armorSkillCacheTable = new ArmorSkillCacheTable(activatedSkills, skillActivationChart, allEquipments, armorFilters, classType, gender);
        stopWatch.printMsgAndResetTime("Finished filtering");

        System.out.println("Building Decoration data");
        decorationSearch = new DecorationSearch(generatedArmorSets, progress, progressChunk, uniqueSetSearchLimit, onSearchResultProgress, activatedSkills, decorationLookupTable);
        stopWatch.printMsgAndResetTime("Finished decoration setup");

        progress+=progressChunk;

        System.out.println("Building charm data");
        charmSearch = new CharmSearch(generatedArmorSets, progress, progressChunk, uniqueSetSearchLimit, onSearchResultProgress, charmLookupTable, decorationSearch);
        stopWatch.printMsgAndResetTime("Finished charm setup");

        progress+=progressChunk;

        System.out.println("Building equipment data");
        armorSearch = new ArmorSearch(generatedArmorSets, progress, progressChunk, weapSlot, armorSkillCacheTable, uniqueSetSearchLimit, onSearchResultProgress);
        stopWatch.printMsgAndResetTime("Finished armor setup");

        /**
         * Starting armor search
         */
        System.out.println("Starting Armor Search.");
        EquipmentList equipmentList = armorSearch.findArmorSetWith(activatedSkills);
        stopWatch.printMsgAndResetTime("Finished Armor Search");



        System.out.println("Starting Decoration Search.");
        equipmentList = decorationSearch.buildEquipmentWithDecorationSkillTable(equipmentList, activatedSkills);
        stopWatch.printMsgAndResetTime("Finished Decoration Search");



        System.out.println("Starting Charm Search.");
        charmSearch.findAValidCharmWithArmorSkill(equipmentList, activatedSkills, 50);
        stopWatch.printMsgAndResetTime("Finished Charm Search");

        return generatedArmorSets;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setClassType(ClassType classType) {
        this.classType = classType;
        skillActivationChart = new SkillActivationChart(skillActivationChartMap, classType);
    }

    public void setArmorFilters(List<ArmorFilter> armorFilters) {
        this.armorFilters = armorFilters;
    }

    public void setWeapSlot(int weapSlot) {
        this.weapSlot = weapSlot;
    }

    public void stopSearching(){

       if (armorSearch != null){
           armorSearch.stop();
       }

       if (decorationSearch != null) {
           decorationSearch.stop();
       }

       if (charmSearch != null) {
           charmSearch.stop();
       }
    }
}
