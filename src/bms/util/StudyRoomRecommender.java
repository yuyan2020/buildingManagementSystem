package bms.util;

import bms.building.Building;
import bms.floor.Floor;
import bms.room.Room;
import bms.room.RoomState;
import bms.room.RoomType;
import bms.sensors.ComfortSensor;
import bms.sensors.Sensor;

import java.util.*;

public class StudyRoomRecommender {
    /**
     * Returns a room in the given building that is most suitable for study purposes.
     * Any given room's suitability for study is based on several criteria, including:
     *
     * the room's type - it must be a study room (see RoomType)
     * the room's status - it must be open, not being evacuated or in maintenance (see Room.evaluateRoomState())
     * the room's comfort level based on its available sensors (see ComfortSensor.getComfortLevel())
     * which floor the room is on - rooms on lower floors are better
     * Since travelling up the floors of a building often requires walking up stairs,
     * the process for choosing a study room begins by looking for rooms on the first floor,
     * and only considers higher floors if doing so would improve the comfort level of the room chosen.
     * Similarly, once on a floor, walking back down more than
     * one floor to a previously considered study room is also not optimal.
     * If there are no rooms on the first floor of a building that meet the basic criteria,
     * then the algorithm should recommend that the building be avoided entirely,
     * even if there are suitable rooms on higher floors.
     *
     * Based on these requirements, the algorithm for determining the most suitable study room is as follows:
     *
     * 1.If there are no rooms in the building, return null.
     * 2.Consider only rooms on the first floor.
     * 3.Eliminate any rooms that are not study rooms or are not open.
     * If there are no remaining candidate rooms,
     * return the room with the highest comfort level on the previous floor,
     * or null if there is no previous floor.
     * 4.Calculate the comfort level of all remaining rooms on this floor,
     * using the average of the comfort levels of each room's available comfort sensors.
     * If a room has no comfort sensors, its comfort level should be treated as 0.
     * 5.Keep a reference to the room with the highest comfort level on this floor
     * based on the calculation in the previous step.
     * If there is a tie between two or more rooms, any of these may be chosen.
     * 6.If the highest comfort level of any room on this floor is less than
     * or equal to the highest comfort level of any room on the previous floor,
     * return the room on the previous floor with the highest comfort level.
     * 7.If this is the top floor of the building, return the room found in step 5.
     * Otherwise, repeat steps 2-7 for the next floor up.
     *
     * @param building - building in which to search for a study room
     * @return Room - the most suitable study room in the building; null if there are none
     */
    public static Room recommendStudyRoom(Building building){
        if (building.getFloors().size() == 0) {
            return null;
        }
        //Test if there are no rooms in the building
        List<Floor> floors;
        floors = building.getFloors();
        int numberOfRooms = 0;
        for (Floor floor:floors) {
            numberOfRooms += floor.getRooms().size();
        }
        if (numberOfRooms == 0) {
            return null;
        }

        List<Room> candidateRooms = new ArrayList<>();
        Room candidateRoom = null;
        HashMap<Room, Double> comfortLevelMap = new HashMap<>();
        HashMap<Room, Double> candidateRoomRefMap = new HashMap<>();
        for (Floor floor:floors) {
            //Eliminate any rooms that are not study rooms or are not open.
            for (Room room:floor.getRooms()) {
                if (room.getType() == RoomType.STUDY && room.evaluateRoomState() == RoomState.OPEN) {
                    candidateRooms.add(room);
                }
            }
            //If there are no remaining candidate rooms,
            // return the room with the highest comfort level on the previous floor,
            //or null if there is no previous floor.
            if (candidateRooms.size() == 0) {
                if (floor.getFloorNumber() == 1) {
                    return null;
                } else {
                    return candidateRoom;
                }
            }
            //Calculate the comfort level of all remaining rooms on this floor
            else {
                comfortLevelMap.clear();
                for (Room r:candidateRooms) {
                    int numberOfSensors = r.getSensors().size();
                    int sumComfortLevel = 0;
                    double avgComfortLevel = 0.0;
                    //If a room has no comfort sensors, its comfort level should be treated as 0
                    if (numberOfSensors == 0) {
                        comfortLevelMap.put(r, 0.0);
                    }
                    //Calculate the comfort level of all remaining rooms on this floor,
                    //using the average of the comfort levels of each room's available comfort sensors.
                    else {
                        for (Sensor sensor:r.getSensors()) {
                            sumComfortLevel += ((ComfortSensor) sensor).getComfortLevel();
                        }
                        avgComfortLevel = ((float) sumComfortLevel) / numberOfSensors;
                        comfortLevelMap.put(r, avgComfortLevel);
                    }
                }
                //find the room with the highest comfort level on this floor
                double maxComfortLevel = Collections.max(comfortLevelMap.values());
                List<Room> candidateRoomList = new ArrayList<>();
                for (Map.Entry<Room, Double> entry : comfortLevelMap.entrySet()) {
                    if (entry.getValue() == maxComfortLevel) {
                        candidateRoomList.add(entry.getKey());
                    }
                }
                // if there is no previous candidate room exists, set the candidate room
                if (candidateRoom == null) {
                    candidateRoom = candidateRoomList.get(0);
                    candidateRoomRefMap.put(candidateRoomList.get(0), maxComfortLevel);
                }
                //if there exists a candidate room, compare it's comfort level with the candidate room on this floor
                else {
                    if (maxComfortLevel <= candidateRoomRefMap.get(candidateRoom)) {
                        return candidateRoom;
                    } else {
                        candidateRoom = candidateRoomList.get(0);
                        candidateRoomRefMap.clear();
                        candidateRoomRefMap.put(candidateRoom, maxComfortLevel);
                    }
                }
            }
        }
        return candidateRoom;
    }
}
