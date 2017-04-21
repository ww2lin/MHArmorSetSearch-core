package interfaces;

import java.util.List;
import models.GeneratedArmorSet;

public interface OnSearchResultProgress {
    void onStart(int max);
    void onProgress(GeneratedArmorSet generatedArmorSet);
    void onProgress(int current);
    void onComplete(List<GeneratedArmorSet> generatedArmorSets);
}
