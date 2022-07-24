package bms.hazardevaluation;

public interface HazardEvaluator {
    /**
     * Calculates a hazard level between 0 and 100.
     * Indicates the hazard level in a location based
     * on information available from multiple hazard sensors at the location.
     * @return the hazard level, between 0 and 100 (inclusive)
     */
    int evaluateHazardLevel();
}
