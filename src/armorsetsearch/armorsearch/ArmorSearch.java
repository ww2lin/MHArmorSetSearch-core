package armorsetsearch.armorsearch;

import armorsetsearch.ArmorSkillCacheTable;
import armorsetsearch.armorsearch.thread.ArmorSearchWorkerThread;
import armorsetsearch.armorsearch.thread.EquipmentList;
import armorsetsearch.armorsearch.thread.EquipmentNode;
import armorsetsearch.skillactivation.SkillActivationChart;
import interfaces.OnSearchResultProgress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import models.Equipment;
import models.EquipmentType;
import models.GeneratedArmorSet;
import armorsetsearch.skillactivation.ActivatedSkill;

import static constants.Constants.GENERATED_EQUIPMENT_ID;
import static constants.Constants.THREAD_COUNT;

public class ArmorSearch {

    private ArmorSkillCacheTable armorSkillCacheTable;
    private final int uniqueSetSearchLimit;
    private OnSearchResultProgress onSearchResultProgress;

    // use to stop the threads.
    private boolean stop = false;
    private ArmorSearchWorkerThread[] workerThreads;
    private int weapSlots;
    private final float initProgress;
    private final float maxProgress;
    private List<GeneratedArmorSet> results;

    public ArmorSearch(List<GeneratedArmorSet> results, float initProgress, float maxProgress, int weapSlots, ArmorSkillCacheTable armorSkillCacheTable, int uniqueSetSearchLimit, OnSearchResultProgress onSearchResultProgress) {
        this.results = results;
        this.weapSlots = weapSlots;
        this.armorSkillCacheTable = armorSkillCacheTable;
        this.uniqueSetSearchLimit = uniqueSetSearchLimit;
        this.onSearchResultProgress = onSearchResultProgress;
        this.initProgress = initProgress;
        this.maxProgress = maxProgress;
    }

    /**
     * run a dfs search for the skill search
     * @param desiredSkills that the user wants to generate
     * @return list of equipment that matches what the user wants
     */
    public EquipmentList findArmorSetWith(List<ActivatedSkill> desiredSkills) {
        Map<EquipmentType, List<Equipment>> equipments = armorSkillCacheTable.getEquipmentCache(desiredSkills);
        return searchArmor(desiredSkills, equipments);
    }

    /**
     * DP implementation of finding if a possible armor set exists.
     * @return
     */
    private EquipmentList searchArmor(List<ActivatedSkill> desiredSkills, Map<EquipmentType, List<Equipment>> equipmentsToSearch) {
        boolean hasWepSlot = weapSlots > 0;
        int size;

        // Check if we have slot for wep.
        if (hasWepSlot) {
            size = EquipmentType.values().length;
            // Smuggle in a weap with slots.
            Equipment wep = Equipment.Builder()
                .setId(GENERATED_EQUIPMENT_ID)
                .setName("")
                .setEquipmentType(EquipmentType.WEP)
                .setSlots(weapSlots);
            equipmentsToSearch.put(EquipmentType.WEP, Collections.singletonList(wep));
        } else {
            size = EquipmentType.values().length - 1;
        }

        // Offset one for searching for charms.
        int progressChuck = (int)maxProgress / size;
        int progressBar = (int)initProgress + progressChuck;

        EquipmentList[] table = new EquipmentList[size];

        // Base case.
        EquipmentType currentType = EquipmentType.values()[0];
        List<Equipment> equipments = equipmentsToSearch.get(currentType);
        EquipmentList currentEquipmentList = new EquipmentList();
        for (Equipment equipment : equipments) {
            currentEquipmentList.add(new EquipmentNode(equipment, SkillActivationChart.getActivatedSkillChart(equipment)));
        }
        table[0] = currentEquipmentList;
        System.out.println("0   "+table[0].size());

        // iterative case
        for (int i = 1; i < size; ++i){
            currentType = EquipmentType.values()[i];
            equipments = equipmentsToSearch.get(currentType);

            if (equipments.isEmpty()) {
                // should only happen for wep.
                continue;
            }

            currentEquipmentList = new EquipmentList();
            // construct all the table for the i element first.
            for (Equipment equipment : equipments) {
                currentEquipmentList.add(new EquipmentNode(equipment, SkillActivationChart.getActivatedSkillChart(equipment)));
            }

            // update the all the values for the current i from i-1
            // add it to sumEquipmentList - this is to avoid value getting updated after one iteration
            EquipmentList previousEquipmentList = table[i-1];

            final float maxPossiblePercentage = (float)progressChuck / (previousEquipmentList.size() * currentEquipmentList.size());

            // divide up the list into multiple parts and use multiple threads to do the calculation
            EquipmentList[] dataSet = new EquipmentList[THREAD_COUNT];
            workerThreads = new ArmorSearchWorkerThread[THREAD_COUNT];
            for (int j = 0; j < currentEquipmentList.size(); ++j){
                int index = j % THREAD_COUNT;
                if (dataSet[index] == null){
                    dataSet[index] = new EquipmentList();
                }
                dataSet[index].add(currentEquipmentList.getEquipmentNodes().get(j));
            }

            // to store the results.
            EquipmentList updatedEquipmentSkillList = new EquipmentList();
            AtomicInteger setsFound = new AtomicInteger(results.size());
            for (int j = 0; j < THREAD_COUNT; ++j){
                workerThreads[j] = new ArmorSearchWorkerThread(j,
                                                               setsFound,
                                                               progressBar,
                                                               maxPossiblePercentage,
                                                               onSearchResultProgress,
                                                               uniqueSetSearchLimit,
                                                               EquipmentType.values()[i],
                                                               previousEquipmentList,
                                                               dataSet[j],
                                                               desiredSkills,
                                                               updatedEquipmentSkillList,
                                                               results);
            }

            for (int  j = 0; j < THREAD_COUNT; ++j) {
                workerThreads[j].start();
            }

            for (int  j = 0; j < THREAD_COUNT; ++j) {
                try {
                    workerThreads[j].join();
                } catch (InterruptedException e) {
                }
            }

            progressBar+=progressChuck;

            // place the sumNode back in i-th index
            table[i] = updatedEquipmentSkillList;

            // free up the memory that we dont need anymore.
            table[i-1] = null;

            System.out.println(i+"  "+table[i].size());

            if (stop || results.size() >= uniqueSetSearchLimit) {
                return table[i];
            }
        }

        if (onSearchResultProgress != null) {
            onSearchResultProgress.onProgress(progressBar);
        }

        return table[size - 1];
    }

    public void stop() {
        stop = true;
        for (int i = 0; i < THREAD_COUNT; ++i){
            workerThreads[i].interrupt();
            workerThreads[i].exit();
        }
    }
}
