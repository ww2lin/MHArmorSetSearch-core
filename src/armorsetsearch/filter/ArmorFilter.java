package armorsetsearch.filter;

import java.util.List;
import models.Equipment;

/**
 * Filter for individual pieces.
 *
 * TODO change it into chain of responsibility pattern
 */
public interface ArmorFilter {
     List<Equipment> filter(List<Equipment> equipmentList);
}
