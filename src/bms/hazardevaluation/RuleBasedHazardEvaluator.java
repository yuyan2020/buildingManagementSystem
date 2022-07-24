package bms.hazardevaluation;

import bms.sensors.HazardSensor;
import bms.sensors.OccupancySensor;

import java.util.List;

public class RuleBasedHazardEvaluator implements HazardEvaluator{

    /**
     * list of hazard sensors used to construct rule based hazard evaluator
     */
    private List<HazardSensor> hazardSensors;

    /**
     * Creates a new rule-based hazard evaluator with the given list of sensors.
     * @param sensors  sensors to be used in the hazard level calculation
     */
    public RuleBasedHazardEvaluator(List<HazardSensor> sensors){
        this.hazardSensors = sensors;
    }

    /**
     * Returns a calculated hazard level based on applying a set of rules to the
     * list of sensors passed to the constructor.
     * The rules to be applied are as follows.
     * Note that square brackets [] have been used to indicate mathematical grouping.
     *
     * If there are no sensors, return 0.
     * If there is only one sensor, return that sensor's current hazard level as per HazardSensor.getHazardLevel().
     * If there is more than one sensor:
     * If any sensor that is not an OccupancySensor has a hazard level of 100, return 100.
     * Calculate the average hazard level of all sensors that are not an OccupancySensor.
     * Floating point division should be used when finding the average.
     * If there is an OccupancySensor in the list,
     * multiply the average calculated in the previous step by [the occupancy sensor's
     * current hazard level divided by 100, using floating point division].
     * Return the final average rounded to the nearest integer between 0 and 100.
     * You can assume that there is no more than one OccupancySensor in the list passed to the constructor.
     * Specified by:
     * evaluateHazardLevel in interface HazardEvaluator
     * @return calculated hazard level according to a set of rules
     */
    @Override
    public int evaluateHazardLevel() {
        if (this.hazardSensors.size() == 0) {
            return 0;
        }
        if (this.hazardSensors.size() == 1) {
            return this.hazardSensors.get(0).getHazardLevel();
        }

        int count = 0;
        double totalHazard = 0.0;
        boolean occupancySensorInList = false;
        HazardSensor occupancySensorTemp = null;
        for (HazardSensor sensor:this.hazardSensors) {
            //Calculate the average hazard level of all sensors that are not an OccupancySensor.
            if (!(sensor instanceof OccupancySensor)) {
                if (sensor.getHazardLevel() == 100) {
                    return 100;
                }
                count++;
                totalHazard += sensor.getHazardLevel();
            }
            //test if there is a occupancy sensor in list
            if (sensor instanceof OccupancySensor) {
                occupancySensorInList = true;
                occupancySensorTemp = sensor;
            }
        }
        if (occupancySensorInList) {
            return (int) Math.round((totalHazard/count) * (float) (occupancySensorTemp.getHazardLevel()/100));
        } else {
            return (int) Math.round(totalHazard/count);
        }
    }

    /**
     * Returns the string representation of this hazard evaluator.
     * The format of the string to return is simply "RuleBased" without double quotes.
     * See the demo save file for an example (uqstlucia.txt).
     * Overrides:
     * toString in class Object
     * @return string representation of this room
     */
    @Override
    public String toString() {
        return "RuleBased";
    }
}
