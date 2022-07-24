package bms.building;

import bms.exceptions.*;
import bms.floor.Floor;
import bms.hazardevaluation.RuleBasedHazardEvaluator;
import bms.hazardevaluation.WeightingBasedHazardEvaluator;
import bms.room.Room;
import bms.room.RoomType;
import bms.sensors.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a building of floors, which in turn, contain rooms.
 * A building needs to manage and keep track of the floors that make up the building.
 * A building can be evacuated, which causes all rooms on all floors within the building to be evacuated.
 */
public class BuildingInitialiser {
    /**
     * Loads a list of buildings from a save file with the given filename.
     * Save files have the following structure.
     * Square brackets indicate that the data inside them is optional.
     * See the demo save file for an example (uqstlucia.txt).
     *
     *  buildingName
     *  numFloors
     *  floorNumber:floorWidth:floorLength:numRooms[:rooms,in,maintenance,schedule]
     *  roomNumber:ROOM_TYPE:roomArea:numSensors[:hazardEvalType]
     *  sensorType:list,of,sensor,readings[:sensorAttributes...][@weighting]
     *  ...       (more sensors)
     *  ...     (more rooms)
     *  ...   (more floors)
     *  ... (more buildings)
     *
     * A save file is invalid if any of the following conditions are true:
     * The number of floors specified for a building is not equal to the
     * actual number of floors read from the file for that building.
     * The number of rooms specified for a floor is not equal to the
     * actual number of rooms read from the file for that floor.
     * The number of sensors specified for a room is not equal to the
     * number of sensors read from the file for that room.
     * A floor's maintenance schedule contains a room number that does not correspond to a room
     * with the same number on that floor.
     * A floor's maintenance schedule is invalid according to Floor.createMaintenanceSchedule(List).
     * A building has two floors with the same floor number (a duplicate floor).
     * A floor's length or width is less than the minimum length or width, respectively, for a floor.
     * A floor has no floor below to support the floor.
     * A floor is too large to fit on top of the floor below.
     * A floor has two rooms with the same room number (a duplicate room).
     * A room cannot be added to its floor because there is insufficient unoccupied space on the floor.
     * A room's type is not one of the types listed in RoomType. Room types are case-sensitive.
     * A room's area is less than the minimum area for a room.
     * A room's hazard evaluator type is invalid.
     * A room's weighting-based hazard evaluator weightings
     * are invalid according to WeightingBasedHazardEvaluator(Map).
     * A room has two sensors of the same type (a duplicate sensor).
     * A sensor's type does not match one of the concrete sensor types (e.g. NoiseSensor, OccupancySensor, ...).
     * A sensor's update frequency does not meet the restrictions outlined in TimedSensor(int[], int).
     * A carbon dioxide sensor's variation limit is greater than its ideal CO2 value.
     * Any numeric value that should be non-negative is less than zero. This includes:
     * the number of floors in a building
     * the number of rooms on a floor
     * the number of sensors in room
     * sensor readings
     * occupancy sensor capacity
     * carbon dioxide sensor ideal CO2 level
     * carbon dioxide sensor variation limit
     * Any numeric value that should be positive is less than or equal to zero. This includes:
     * floor numbers
     * The colon-delimited format is violated, i.e. there are more/fewer colons than expected.
     * Any numeric value fails to be parsed.
     * An empty line occurs where a non-empty line is expected.
     *
     * @param filename path of the file from which to load a list of buildings
     * @return a list containing all the buildings loaded from the file
     * @throws IOException if an IOException is encountered when calling any IO methods
     * @throws FileFormatException if the file format of the given file is invalid according to the rules above
     */
    public static List<Building> loadBuildings(String filename)
            throws IOException,
            FileFormatException {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
        } catch (IOException ioException) {
            throw new IOException();
        }

        List<Building> buildingList = new ArrayList<>();
        try {
            String line = reader.readLine();

            if (line == null) {
                throw new FileFormatException();
            }

            //first line must be building name with no colons
            String buildingName;
            int numFloors;
            Building building = null;
            while (line != null) {
                if (!(line.contains(":"))) {
                    buildingName = line;
                    building = new Building(buildingName);
                }
                line = reader.readLine();
                try {
                    numFloors = Integer.parseInt(line);
                } catch (NumberFormatException numberFormatException) {
                    throw new FileFormatException();
                }

                List<Floor> floorList = new ArrayList<>();
                for (int i = 0; i < numFloors; i++) {
                    floorList.add(loadFloor(reader));
                }
                for (Floor floor:floorList) {
                    try {
                        building.addFloor(floor);
                    } catch (IllegalArgumentException e) {
                        throw new FileFormatException();
                    }
                }

                buildingList.add(building);
                line = reader.readLine();
            }

        } catch (IOException |
                FloorTooSmallException | DuplicateFloorException
                | NoFloorBelowException | InsufficientSpaceException
                | DuplicateRoomException ioException) {
            throw new FileFormatException();
        }
        return buildingList;
    }

    /**
     * help method used to load buildings list
     * return the floor object of current line is reading;
     * @param reader buffered reader object given by load buildings method
     * @return the floor object of current line;
     * @throws IOException if an IOException is encountered when calling any IO methods
     * @throws FileFormatException if the file format of the given file is invalid according to the rules above
     * @throws InsufficientSpaceException if there is insufficient space
     *          available on the floor to be able to add the room
     * @throws DuplicateRoomException if the room number on this floor is already taken
     */
    private static Floor loadFloor (BufferedReader reader)
            throws IOException, FileFormatException,
            InsufficientSpaceException, DuplicateRoomException {
        Floor floor = null;
        try {
            String line = reader.readLine();
            if (line.equals("")) {
                throw new FileFormatException();
            }
            String [] pair = line.split(":");
            int floorNumber;
            double width;
            double length;
            int numRooms = 0;
            String maintenance;

            if (pair.length != 4 && pair.length != 5) {
                throw new FileFormatException();
            }

            //floor with no maintenance schedule
            if (pair.length == 4) {
                try {
                    floorNumber = Integer.parseInt(pair[0]);
                    width = Double.parseDouble(pair[1]);
                    length = Double.parseDouble(pair[2]);
                    numRooms = Integer.parseInt(pair[3]);
                } catch (NumberFormatException numberFormatException) {
                    throw new FileFormatException();
                }
                floor = new Floor(floorNumber, width, length);

                List<Room> roomList = new ArrayList<>();
                for (int i = 0; i < numRooms; i++) {
                    roomList.add(loadRoom(reader));
                }
                for (Room room:roomList) {
                    floor.addRoom(room);
                }

            }

            //floor with maintenance schedule
            if (pair.length == 5) {
                List<Integer> maintenanceIntList = new ArrayList<>();
                try {
                    floorNumber = Integer.parseInt(pair[0]);
                    width = Double.parseDouble(pair[1]);
                    length = Double.parseDouble(pair[2]);
                    numRooms = Integer.parseInt(pair[3]);

                    //create a list to keep maintenance schedule
                    maintenance = pair[4];
                    String [] maintenanceList = maintenance.split(",");
                    for (int i = 0; i < maintenanceList.length; i++) {
                        int roomNumber;
                        try {
                            roomNumber = Integer.parseInt(maintenanceList[i]);
                        } catch (NumberFormatException numberFormatException) {
                            throw new FileFormatException();
                        }
                        maintenanceIntList.add(roomNumber);
                    }

                } catch (NumberFormatException numberFormatException) {
                    throw new FileFormatException();
                }

                floor = new Floor(floorNumber, width, length);

                List<Room> roomList = new ArrayList<>();
                for (int i = 0; i < numRooms; i++) {
                    roomList.add(loadRoom(reader));
                }
                for (Room room:roomList) {
                    floor.addRoom(room);
                }

                //set the maintenance schedule of the floor
                List<Room> maintenanceRoomList = new ArrayList<>();

                //check the invalidation of maintenance list
                ArrayList<Integer> roomIntList = new ArrayList<>();
                for (Room room:roomList) {
                    roomIntList.add(room.getRoomNumber());
                }

                for (int roomNumber:maintenanceIntList) {
                    if (!(roomIntList.contains(roomNumber))) {
                        throw new FileFormatException();
                    }
                }

                //creat List<Room> type maintenance Room List
                for (int roomNumber:maintenanceIntList) {
                    for (Room room:roomList) {
                        if (roomNumber == room.getRoomNumber()) {
                            maintenanceRoomList.add(room);
                        }
                    }
                }

                try {
                    floor.createMaintenanceSchedule(maintenanceRoomList);
                } catch (IllegalArgumentException illegalArgumentException) {
                    throw new FileFormatException();
                }

            }

        } catch (IOException | FileFormatException
                | DuplicateRoomException | InsufficientSpaceException
                | DuplicateSensorException ioException) {
            throw new FileFormatException();
        }
        return floor;
    }

    /**
     * help method used to load floors
     * @param reader buffered reader object given by load buildings method
     * @return the room object of the current line is reading
     * @throws IOException if an IOException is encountered when calling any IO methods
     * @throws DuplicateSensorException if the sensor to add is of the
     *                    same type as a sensor already in this room
     * @throws FileFormatException if the file format of the given file is invalid according to the rules above
     */
    private static Room loadRoom (BufferedReader reader)
            throws IOException, DuplicateSensorException, FileFormatException {
        Room room = null;
        try {
            String line = reader.readLine();
            if (line.equals("")) {
                throw new FileFormatException();
            }
            String [] pair = line.split(":");
            int roomNumber;
            String roomType;
            RoomType roomTypeEnum;
            double area;
            int numSensors = 0;
            String hazardEvaluator;
            try {
                roomNumber = Integer.parseInt(pair[0]);
                roomType = pair[1];
                roomTypeEnum = RoomType.valueOf(roomType);
                area = Double.parseDouble(pair[2]);
                if (area < Room.getMinArea()) {
                    throw new FileFormatException();
                }
                numSensors = Integer.parseInt(pair[3]);
            } catch (IllegalArgumentException illegalArgumentException) {
                throw new FileFormatException();
            }

            if (pair.length != 4 && pair.length != 5){
                throw new FileFormatException();
            }
            //room with no hazard evaluator
            if (pair.length == 4) {
                room = new Room(roomNumber, roomTypeEnum, area);

                if (numSensors == 0) {
                    return room;
                } else {
                    List<Sensor> sensorList = new ArrayList<>();
                    for (int i = 0; i < numSensors; i++) {
                        sensorList.add(loadSensor(reader));
                    }
                    for (Sensor sensor:sensorList) {
                        room.addSensor(sensor);
                    }
                    return room;
                }
            }

            ////room with hazard evaluator
            if (pair.length == 5) {
                hazardEvaluator = pair[4];
                room = new Room(roomNumber, roomTypeEnum, area);
                List<Sensor> sensorList = new ArrayList<>();

                if (!(hazardEvaluator.equals("RuleBased")) &&
                        !(hazardEvaluator.equals("WeightingBased"))) {
                    throw new FileFormatException();
                }

                if (hazardEvaluator.equals("RuleBased")) {
                    for (int i = 0; i < numSensors; i++) {
                        sensorList.add(loadSensor(reader));
                    }
                    for (Sensor sensor:sensorList) {
                        room.addSensor(sensor);
                    }

                    //cast sensor type list to hazard sensor list
                    List<HazardSensor> hazardSensorList = new ArrayList<>();
                    for (Sensor sensor:sensorList) {
                        hazardSensorList.add((HazardSensor) sensor);
                    }
                    RuleBasedHazardEvaluator ruleBasedHazardEvaluator
                            = new RuleBasedHazardEvaluator(hazardSensorList);
                    room.setHazardEvaluator(ruleBasedHazardEvaluator);
                }

                if (hazardEvaluator.equals("WeightingBased")) {

                    Map<HazardSensor, Integer> weightingBasedMap = new HashMap<>();
                    for (int i = 0; i < numSensors; i++) {
                        Map<Sensor, Integer> map = loadSensorWeightingBased(reader);
                        Map.Entry<Sensor, Integer> tempEntry = map.entrySet().iterator().next();
                        Sensor sensor = tempEntry.getKey();
                        sensorList.add(sensor);
                        //cast sensor type to hazard sensor and put it into a map with weighting
                        HazardSensor hazardSensor = (HazardSensor) sensor;
                        int weighting = tempEntry.getValue();
                        weightingBasedMap.put(hazardSensor, weighting);
                    }
                    for (Sensor sensor:sensorList) {
                        room.addSensor(sensor);
                    }

                    WeightingBasedHazardEvaluator weightingBasedHazardEvaluator;
                    try {
                        weightingBasedHazardEvaluator
                                = new WeightingBasedHazardEvaluator(weightingBasedMap);
                    } catch (IllegalArgumentException e) {
                        throw new FileFormatException();
                    }
                    room.setHazardEvaluator(weightingBasedHazardEvaluator);
                }


            }
        } catch (IOException | FileFormatException | DuplicateSensorException ioException) {
            throw new FileFormatException();
        }
        return room;
    }

    /**
     * help method used to load sensor if the hazard evaluator of the room is rule based
     * @param reader buffered reader object given by load buildings method
     * @return the sensor object of the current line is reading
     * @throws IOException if an IOException is encountered when calling any IO methods
     * @throws FileFormatException if the file format of the given file is invalid according to the rules above
     */
    private static Sensor loadSensor (BufferedReader reader)
            throws IOException, FileFormatException {
        Sensor sensor = null;

        try {
            String line = reader.readLine();
            if (line.equals("")) {
                throw new FileFormatException();
            }
            String [] pair = line.split(":");
            String sensorName = pair[0];
            int updateFrequency;
            int capacity;
            int idealValue;
            int varLimit;
            //convert the string type sensor readings list to int type list
            String[] sensorReadingsString = pair[1].split(",");
            int [] sensorReadings = new int[sensorReadingsString.length];
            for (int i = 0; i < sensorReadingsString.length; i++) {
                try {
                    int reading = Integer.parseInt(sensorReadingsString[i]);
                    sensorReadings[i] = reading;
                } catch (NumberFormatException numberFormatException) {
                    throw new FileFormatException();
                }
            }

            //check file format valid
            if (pair.length != 2 && pair.length != 3 && pair.length != 4 && pair.length != 5) {
                throw new FileFormatException();
            }
            //check sensor type valid
            if (!(sensorName.equals("TemperatureSensor")) &&
                    !(sensorName.equals("CarbonDioxideSensor")) &&
                    !(sensorName.equals("OccupancySensor")) &&
                    !(sensorName.equals("NoiseSensor"))) {
                throw new FileFormatException();
            }

            if (sensorName.equals("TemperatureSensor")) {
                TemperatureSensor temperatureSensor;
                try {
                    temperatureSensor = new TemperatureSensor(sensorReadings);
                } catch (IllegalArgumentException e) {
                    throw new FileFormatException();
                }

                sensor = temperatureSensor;
            }

            if (sensorName.equals("CarbonDioxideSensor")) {
                try {
                    updateFrequency = Integer.parseInt(pair[2]);
                    idealValue = Integer.parseInt(pair[3]);
                    varLimit = Integer.parseInt(pair[4]);
                } catch (NumberFormatException numberFormatException) {
                    throw new FileFormatException();
                }

                CarbonDioxideSensor carbonDioxideSensor;
                try {
                    carbonDioxideSensor =
                            new CarbonDioxideSensor(sensorReadings, updateFrequency, idealValue, varLimit);
                } catch (IllegalArgumentException e) {
                    throw new FileFormatException();
                }

                sensor = carbonDioxideSensor;
            }

            if (sensorName.equals("OccupancySensor")) {
                try {
                    updateFrequency = Integer.parseInt(pair[2]);
                    capacity = Integer.parseInt(pair[3]);
                } catch (NumberFormatException numberFormatException) {
                    throw new FileFormatException();
                }

                OccupancySensor occupancySensor;
                try {
                    occupancySensor = new OccupancySensor(sensorReadings, updateFrequency, capacity);
                } catch (IllegalArgumentException e) {
                    throw new FileFormatException();
                }
                sensor = occupancySensor;
            }

            if (sensorName.equals("NoiseSensor")) {
                try {
                    updateFrequency = Integer.parseInt(pair[2]);
                } catch (NumberFormatException numberFormatException) {
                    throw new FileFormatException();
                }

                NoiseSensor noiseSensor;
                try {
                    noiseSensor = new NoiseSensor(sensorReadings, updateFrequency);
                } catch (IllegalArgumentException e) {
                    throw new FileFormatException();
                }
                sensor = noiseSensor;
            }
        } catch (IOException | FileFormatException ioException) {
            throw new FileFormatException();
        }
        return sensor;
    }

    /**
     * help method used to load sensor if the hazard evaluator of the room is weighting based
     * @param reader buffered reader object given by load buildings method
     * @return map object with a single pair of key and value
     *         key is the sensor object of the current reading line
     *         value is the weighting of this sensor
     * @throws IOException if an IOException is encountered when calling any IO methods
     * @throws FileFormatException if the file format of the given file is invalid according to the rules above
     */
    private static Map<Sensor, Integer> loadSensorWeightingBased (BufferedReader reader)
            throws IOException, FileFormatException {
        Sensor sensor = null;
        int weighting;
        try {
            String line = reader.readLine();
            //split the sensor info part and weighting part
            String [] splitPair = line.split("@");
            String part1 = splitPair[0];
            String part2 = splitPair[1];

            try {
                weighting = Integer.parseInt(part2);
            } catch (NumberFormatException numberFormatException) {
                throw new FileFormatException();
            }

            String[] pair = part1.split(":");
            String sensorName = pair[0];
            int updateFrequency;
            int capacity;
            int idealValue;
            int varLimit;
            //convert the string type sensor readings list to int type list
            String[] sensorReadingsString = pair[1].split(",");
            int [] sensorReadings = new int[sensorReadingsString.length];
            for (int i = 0; i < sensorReadingsString.length; i++) {
                try {
                    int reading = Integer.parseInt(sensorReadingsString[i]);
                    sensorReadings[i] = reading;
                } catch (NumberFormatException numberFormatException) {
                    throw new FileFormatException();
                }
            }

            //check file format valid
            if (pair.length != 2 && pair.length != 3 && pair.length != 4 && pair.length != 5){
                throw new FileFormatException();
            }
            //check sensor type valid
            if (!(sensorName.equals("TemperatureSensor")) &&
                    !(sensorName.equals("CarbonDioxideSensor")) &&
                    !(sensorName.equals("OccupancySensor")) &&
                    !(sensorName.equals("NoiseSensor"))) {
                throw new FileFormatException();
            }

            if (sensorName.equals("TemperatureSensor")) {
                TemperatureSensor temperatureSensor;
                try {
                    temperatureSensor = new TemperatureSensor(sensorReadings);
                } catch (IllegalArgumentException e) {
                    throw new FileFormatException();
                }

                sensor = temperatureSensor;
            }

            if (sensorName.equals("CarbonDioxideSensor")) {
                try {
                    updateFrequency = Integer.parseInt(pair[2]);
                    idealValue = Integer.parseInt(pair[3]);
                    varLimit = Integer.parseInt(pair[4]);
                } catch (NumberFormatException numberFormatException) {
                    throw new FileFormatException();
                }

                CarbonDioxideSensor carbonDioxideSensor;
                try {
                    carbonDioxideSensor =
                            new CarbonDioxideSensor(sensorReadings, updateFrequency, idealValue, varLimit);
                } catch (IllegalArgumentException e) {
                    throw new FileFormatException();
                }

                sensor = carbonDioxideSensor;
            }

            if (sensorName.equals("OccupancySensor")) {
                try {
                    updateFrequency = Integer.parseInt(pair[2]);
                    capacity = Integer.parseInt(pair[3]);
                } catch (NumberFormatException numberFormatException) {
                    throw new FileFormatException();
                }

                OccupancySensor occupancySensor;
                try {
                    occupancySensor = new OccupancySensor(sensorReadings, updateFrequency, capacity);
                } catch (IllegalArgumentException e) {
                    throw new FileFormatException();
                }
                sensor = occupancySensor;
            }

            if (sensorName.equals("NoiseSensor")) {
                try {
                    updateFrequency = Integer.parseInt(pair[2]);
                } catch (NumberFormatException numberFormatException) {
                    throw new FileFormatException();
                }

                NoiseSensor noiseSensor;
                try {
                    noiseSensor = new NoiseSensor(sensorReadings, updateFrequency);
                } catch (IllegalArgumentException e) {
                    throw new FileFormatException();
                }
                sensor = noiseSensor;
            }
        } catch (IOException | FileFormatException ioException) {
            throw new FileFormatException();
        }
        Map<Sensor, Integer> map = new HashMap<>();
        map.put(sensor, weighting);
        return map;
    }
}
