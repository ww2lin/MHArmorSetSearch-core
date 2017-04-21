package armorsetsearch.armorsearch.thread;

import interfaces.OnSearchResultProgress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import models.EquipmentType;
import models.GeneratedArmorSet;
import armorsetsearch.skillactivation.ActivatedSkill;
import armorsetsearch.skillactivation.SkillUtil;

public class ArmorSearchWorkerThread extends Thread {

    private int id;
    private int currentProgress;
    private float maxPossiblePercentage;
    private EquipmentType equipmentType;
    private EquipmentList previousEquipmentList;
    private EquipmentList currentEquipmentList;
    private final EquipmentList updatedEquipmentSkillList;

    private List<ActivatedSkill> desiredSkills;
    private final List<GeneratedArmorSet> generatedArmorSets;
    private boolean stop = false;
    private AtomicInteger setsFound;
    private final int uniqueSetSearchLimit;
    private OnSearchResultProgress onSearchResultProgress;

    public ArmorSearchWorkerThread(int id,
                                   AtomicInteger setsFound,
                                   int currentProgress,
                                   float maxPossiblePercentage,
                                   OnSearchResultProgress onSearchResultProgress,
                                   int uniqueSetSearchLimit,
                                   EquipmentType equipmentType,
                                   EquipmentList previousEquipmentList,
                                   EquipmentList currentEquipmentList,
                                   List<ActivatedSkill> desiredSkills,
                                   EquipmentList updatedEquipmentSkillList,
                                   List<GeneratedArmorSet> generatedArmorSets) {
        this.id = id;
        this.setsFound = setsFound;
        this.currentProgress = currentProgress;
        this.maxPossiblePercentage = maxPossiblePercentage;
        this.onSearchResultProgress = onSearchResultProgress;
        this.uniqueSetSearchLimit = uniqueSetSearchLimit;
        this.equipmentType = equipmentType;
        this.previousEquipmentList = previousEquipmentList;
        this.currentEquipmentList = currentEquipmentList;
        this.desiredSkills = desiredSkills;
        this.updatedEquipmentSkillList = updatedEquipmentSkillList;
        this.generatedArmorSets = generatedArmorSets;
    }

    @Override
    public void run() {
        int setsTried = 0;
        EquipmentList equipmentList = new EquipmentList();
        List<GeneratedArmorSet> armorsFound = new ArrayList<>();

        if (currentEquipmentList == null) {
            // number of sets is smaller than the number of threads.
            return;
        }

        for (EquipmentNode curEquipmentNode : currentEquipmentList.getEquipmentNodes()) {
            for (EquipmentNode preEquipmentNode : previousEquipmentList.getEquipmentNodes()) {
                if (stop) {
                    synchronized (generatedArmorSets) {
                        generatedArmorSets.addAll(armorsFound);
                    }
                    return;
                }

                EquipmentNode sumNode = EquipmentNode.add(preEquipmentNode, curEquipmentNode, equipmentType);


                // Check if this table satisfy the desire skills.
                List<ActivatedSkill> activatedSkills = sumNode.getActivatedSkills();
                if (SkillUtil.containsDesiredSkills(desiredSkills, activatedSkills)) {
                    GeneratedArmorSet generatedArmorSet = new GeneratedArmorSet(sumNode);
                    armorsFound.add(generatedArmorSet);
                    if (onSearchResultProgress != null) {
                        onSearchResultProgress.onProgress(generatedArmorSet);
                    }
                } else {
                    // Only add the set to the search set if its in complete.
                    equipmentList.add(sumNode);
                }
                if (onSearchResultProgress != null) {
                    onSearchResultProgress.onProgress(getProgressNumber(++setsTried));
                }
                if (setsFound.get() > uniqueSetSearchLimit) {
                    returnData(equipmentList, armorsFound);
                    return;
                }
            }
        }
        returnData(equipmentList, armorsFound);
    }

    private void returnData(EquipmentList equipmentList, List<GeneratedArmorSet> armorsFound){
        synchronized (updatedEquipmentSkillList) {
            updatedEquipmentSkillList.add(equipmentList);
        }

        synchronized (generatedArmorSets) {
            generatedArmorSets.addAll(armorsFound);
        }
    }

    private int getProgressNumber(float i){
        return currentProgress + Math.round(i * maxPossiblePercentage);
    }

    public void exit(){
        this.stop = true;
    }
}
