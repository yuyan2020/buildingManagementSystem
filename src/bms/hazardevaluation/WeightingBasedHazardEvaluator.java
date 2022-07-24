package bms.hazardevaluation;

import bms.sensors.*;

import java.util.*;

/**
 * Evaluates the hazard level of a location using weightings for the sensor values.
 * The sum of the weightings of all sensors must equal 100.
 */
public class WeightingBasedHazardEvaluator implements HazardEvaluator{

    /**
     * hashmap used to construct weighting based hazrd evaluator
     * key is hazard sensor
     * value is integer which is the sensor's weighting
     */
    private Map<HazardSensor, Integer> weightingBasedHazardEvaluator;
    /**
     * Creates a new weighting-based hazard evaluator with the given sensors and weightings.
     * Each weighting must be between 0 and 100 inclusive, and the total sum of all weightings must equal 100.
     *
     * Parameters:
     * sensors - mapping of sensors to their respective weighting
     * Throws:
     * IllegalArgumentException - if any weighting is below 0 or above 100;
     * or if the sum of all weightings is not equal to 100
     * @throws IllegalArgumentException
     */
    public WeightingBasedHazardEvaluator(Map<HazardSensor,Integer> sensors)
            throws IllegalArgumentException {
        int sum = 0;
        for (int value:sensors.values()) {
            if (value < 0 || value > 100) {
                throw new IllegalArgumentException();
            }
            sum += value;
        }
        if (sum != 100) {
            throw new IllegalArgumentException();
        }
        this.weightingBasedHazardEvaluator = sensors;
    }

    /**
     * Returns the weighted average of the current hazard levels
     * of all sensors in the map passed to the constructor.
     * The weightings given in the constructor should be used.
     * The final evaluated hazard level should be rounded to the nearest integer between 0 and 100.
     *
     * For example, given the following sensors and weightings, this method should return a value of 28.
     * Specified by:
     * evaluateHazardLevel in interface HazardEvaluator
     * @return weighted average of current sensor hazard levels
     */
    @Override
    public int evaluateHazardLevel() {
        double weightingResult = 0.0;
        Iterator<Map.Entry<HazardSensor, Integer>> iterator
                = this.weightingBasedHazardEvaluator.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<HazardSensor, Integer> entry = iterator.next();
            weightingResult += entry.getKey().getHazardLevel() * ((double) entry.getValue() / 100.0);
        }
        return (int) Math.round(weightingResult);
    }

    /**
     * Returns a list containing the weightings associated with all of the sensors monitored by this hazard evaluator.
     * @return weightings
     */
    public List<Integer> getWeightings() {
        List<Integer> weightings = new ArrayList<>();

        Map<String, Integer> treeMap = new TreeMap<>();
        Iterator<Map.Entry<HazardSensor, Integer>> iterator =
                this.weightingBasedHazardEvaluator.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<HazardSensor, Integer> entry = iterator.next();
            if (entry.getKey() instanceof CarbonDioxideSensor) {
                treeMap.put("CarbonDioxideSensor", entry.getValue());
            }
            if (entry.getKey() instanceof NoiseSensor) {
                treeMap.put("NoiseSensor", entry.getValue());
            }
            if (entry.getKey() instanceof OccupancySensor) {
                treeMap.put("OccupancySensor", entry.getValue());
            }
            if (entry.getKey() instanceof TemperatureSensor) {
                treeMap.put("TemperatureSensor", entry.getValue());
            }
        }
        for (int weighting:treeMap.values()) {
            weightings.add(weighting);
        }

        return weightings;
    }

    /**
     * Returns the string representation of this hazard evaluator.
     * The format of the string to return is simply "WeightingBased" without the double quotes.
     *
     * See the demo save file for an example (uqstlucia.txt).
     * Overrides:
     * toString in class Object
     * @return string representation of this hazard evaluator
     */
    @Override
    public String toString() {
        return "WeightingBased";
    }
}
