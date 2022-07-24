package bms.sensors;

/**
 * A sensor that measures the number of people in a room.
 * @ass1
 */
public class OccupancySensor extends TimedSensor implements HazardSensor, ComfortSensor {
    /**
     * Maximum capacity of the space the sensor is monitoring.
     */
    private int capacity;

    /**
     * Creates a new occupancy sensor with the given sensor readings, update
     * frequency and capacity.
     * <p>
     * The given capacity must be greater than or equal to zero.
     *
     * @param sensorReadings a non-empty array of sensor readings
     * @param updateFrequency indicates how often the sensor readings update,
     *                        in minutes
     * @param capacity maximum allowable number of people in the room
     * @throws IllegalArgumentException if capacity is less than zero
     * @ass1
     */
    public OccupancySensor(int[] sensorReadings, int updateFrequency,
                           int capacity) {
        super(sensorReadings, updateFrequency);

        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be >= 0");
        }

        this.capacity = capacity;
    }

    /**
     * Returns the capacity of this occupancy sensor.
     *
     * @return capacity
     * @ass1
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns the hazard level based on the ratio of the current sensor reading
     * to the maximum capacity.
     * <p>
     * When the current reading is equal to or more than the capacity, the
     * hazard level is equal to 100 percent.
     * <p>
     * For example, a room with a maximum capacity of 21 people and current
     * occupancy of 8 people would have a hazard level of 38.
     * A room with a maximum capacity of 30 people and a current occupancy of
     * 34 people would have a hazard level of 100.
     * <p>
     * Floating point division should be used when performing the calculation,
     * however the resulting floating point number should be <i>rounded to the
     * nearest integer</i> before being returned.
     *
     * @return the current hazard level as an integer between 0 and 100
     * @ass1
     */
    @Override
    public int getHazardLevel() {
        final int currentReading = this.getCurrentReading();

        if (currentReading >= this.capacity) {
            return 100;
        }
        double occupancyRatio = ((double) currentReading) / this.capacity;
        double occupancyPct = 100 * occupancyRatio;
        return (int) Math.round(occupancyPct);
    }

    /**
     * Returns the current comfort level as observed by the sensor.
     * The comfort level is calculated as the complement of the percentage given
     * by the current sensor reading divided by the maximum capacity.
     * If the current reading is above the capacity, the comfort level is 0.
     * For example, a sensor with a capacity of 20 people and a current reading
     * of 14 people would have a comfort level of 30.
     * A sensor with a capacity of 15 people and a current reading of 17 people would have a comfort level of 0.
     *
     * Floating point division should be used when performing the calculation,
     * however the resulting floating point number should be rounded to the nearest integer before being returned.
     *
     * Specified by:
     * getComfortLevel in interface ComfortSensor
     * @return the current comfort level as an integer between 0 and 100
     */
    @Override
    public int getComfortLevel() {
        if (getCurrentReading() >= this.capacity) {
            return 0;
        }
        return Math.round((((float)
                (this.capacity - getCurrentReading())) / ((float) this.capacity)) * 100);
    }

    /**
     * Returns true if and only if this occupancy sensor is equal to the other given sensor.
     * For two occupancy sensors to be equal, they must have the same:
     * update frequency
     * sensor readings array (in the same order)
     * maximum capacity
     * Overrides:
     * equals in class TimedSensor
     * @param obj other object to compare equality
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OccupancySensor)) {
            return false;
        }
        OccupancySensor otherOccupancySensor = (OccupancySensor) obj;
        return super.equals(otherOccupancySensor)
                && this.capacity == otherOccupancySensor.getCapacity();
    }

    /**
     * Returns the hash code of this occupancy sensor.
     * Two occupancy sensors that are equal according to equals(Object) should have the same hash code.
     * Overrides:
     * hashCode in class TimedSensor
     * @return hash code of this sensor
     */
    @Override
    public int hashCode() {
        return this.capacity + this.getUpdateFrequency();
    }

    /**
     * Returns the human-readable string representation of this occupancy
     * sensor.
     * <p>
     * The format of the string to return is
     * "TimedSensor: freq='updateFrequency', readings='sensorReadings',
     * type=OccupancySensor, capacity='sensorCapacity'"
     * without the single quotes, where 'updateFrequency' is this sensor's
     * update frequency (in minutes), 'sensorReadings' is a comma-separated
     * list of this sensor's readings, and 'sensorCapacity' is this sensor's
     * maximum capacity.
     * <p>
     * For example: "TimedSensor: freq=5, readings=27,28,28,25,3,1,
     * type=OccupancySensor, capacity=30"
     *
     * @return string representation of this sensor
     * @ass1
     */
    @Override
    public String toString() {
        return String.format("%s, type=OccupancySensor, capacity=%d",
                super.toString(),
                this.capacity);
    }

    /**
     * Returns the machine-readable string representation of this occupancy sensor.
     * The format of the string to return is:
     *
     *  OccupancySensor:sensorReading1,sensorReading2,...,sensorReadingN:frequency:capacity
     *
     * where 'sensorReadingX' is the Xth sensor reading in this sensor's list of readings,
     * from 1 to N where N is the number of readings,
     * 'frequency' is this sensor's update frequency (in minutes),
     * and 'capacity' is the maximum capacity of the room monitored by this sensor.
     * There should be no newline at the end of the string.
     *
     * See the demo save file for an example (uqstlucia.txt).
     *
     * Specified by:
     * encode in interface Encodable
     * Overrides:
     * encode in class TimedSensor
     * @return encoded string representation of this occupancy sensor
     */
    @Override
    public String encode() {
        String occupancySensorEncode = "OccupancySensor:";
        return occupancySensorEncode + super.encode() + ":" + this.getUpdateFrequency() + ":"
                + this.capacity;
    }
}
