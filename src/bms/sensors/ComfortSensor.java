package bms.sensors;

/**
 * Represents a sensor that can be used to evaluate the level
 * of comfort present in a location monitored by the sensor.
 */
public interface ComfortSensor extends Sensor{
    /**
     * Returns the comfort level in a location as detected by this sensor (as a percentage).
     * A value of 0 indicates demonstrates very low comfort,
     * and a value of 100 indicates very high comfort.
     * @return level of comfort at sensor location, 0 to 100
     */
    int getComfortLevel();
}
