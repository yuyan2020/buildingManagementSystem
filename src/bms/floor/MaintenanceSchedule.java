package bms.floor;

import bms.room.Room;
import bms.room.RoomType;
import bms.util.Encodable;
import bms.util.TimedItem;
import bms.util.TimedItemManager;

import java.util.List;

/**
 * Carries out maintenance on a list of rooms in a given floor.
 * The maintenance time for each room depends on the type of the room and its area.
 * Maintenance cannot progress whilst an evacuation is in progress.
 */
public class MaintenanceSchedule implements TimedItem, Encodable {

    /**
     * room list for create maintenance schedule
     */
    private List<Room> roomOrder;

    /**
     * current room on maintain
     */
    private Room currentRoom;

    /**
     * time elapsed in current room
     */
    private int timeElapsed;

    /**
     * the index of the current room of the room list
     */
    private int currentRoomIndex = 0;

    /**
     * Creates a new maintenance schedule for a floor's list of rooms.
     * In this constructor, the new maintenance schedule should be registered as a timed item with the timed item manager.
     *
     * The first room in the given order should be set to "in maintenance", see Room.setMaintenance(boolean).
     * Requires:
     * roomOrder != null && roomOrder.size() > 0
     * @param roomOrder list of rooms on which to perform maintenance, in order
     */
    public MaintenanceSchedule(List<Room> roomOrder) {
        this.roomOrder = roomOrder;
        this.timeElapsed = 0;
        TimedItemManager.getInstance().registerTimedItem(this);
        this.currentRoom = roomOrder.get(0);
        this.currentRoom.setMaintenance(true);
    }

    /**
     * Returns the time taken to perform maintenance on the given room, in minutes.
     * The maintenance time for a given room depends on its size (larger rooms take longer to maintain)
     * and its room type (rooms with more furniture and equipment take take longer to maintain).
     *
     * The formula for maintenance time is calculated as the room's base maintenance time multiplied by its room type multiplier,
     * and finally rounded to the nearest integer. Floating point operations should be used during all steps of the calculation, until the final rounding to integer.
     * Rooms with an area of Room.getMinArea() have a base maintenance time of 5.0 minutes.
     * Rooms with areas greater than Room.getMinArea() have a base maintenance time of 5.0 minutes,
     * plus 0.2 minutes for every square metre the room's area is over Room.getMinArea().
     * @param room
     * @return
     */
    public int getMaintenanceTime(Room room) {
        int minArea = Room.getMinArea();
        double roomArea = room.getArea();
        RoomType roomType = room.getType();
        double rawTime = 0.0;
        if (roomType == RoomType.STUDY) {
            rawTime = 5.0 + (roomArea - minArea) * 0.2;
        }
        if (roomType == RoomType.LABORATORY) {
            rawTime = (5.0 + (roomArea - minArea) * 0.2) * 2;
        }
        if (roomType == RoomType.OFFICE) {
            rawTime = (5.0 + (roomArea - minArea) * 0.2) * 1.5;
        }
        int finalTime = (int) Math.round(rawTime);
        return finalTime;
    }

    /**
     * Returns the room which is currently in the process of being maintained.
     * @return room currently in maintenance
     */
    public Room getCurrentRoom(){
        return this.currentRoom;
    }

    /**
     * Returns the number of minutes that have elapsed while maintaining the current room (getCurrentRoom()).
     * @return time elapsed maintaining current room
     */
    public int getTimeElapsedCurrentRoom(){
        return this.timeElapsed;
    }


    /**
     *Progresses the maintenance schedule by one minute.
     * If the room currently being maintained has a room state of EVACUATE, then no action should occur.
     *
     * If enough time has elapsed such that the room currently being maintained has completed its maintenance
     * (according to getMaintenanceTime(Room)), then:
     *
     * the current room should have its maintenance status set to false ( see Room.setMaintenance(boolean))
     * the next room in the list passed to the constructor should be set as the new current room.
     * If the end of the list has been reached, the new current room should "wrap around" to the first room in the list.
     * the new current room should have its maintenance status set to true
     * Specified by:
     * elapseOneMinute in interface TimedItem
     */
    public void elapseOneMinute() {
        this.timeElapsed++;
        int numberOfRooms = this.roomOrder.size();

        if (this.timeElapsed >= getMaintenanceTime(this.currentRoom)) {
            this.currentRoom.setMaintenance(false);
            this.currentRoomIndex++;
            if (this.currentRoomIndex == numberOfRooms) {
                this.currentRoomIndex = 0;
            }
            this.currentRoom = this.roomOrder.get(this.currentRoomIndex);
            this.currentRoom.setMaintenance(true);
            this.timeElapsed = 0;
        }
    }

    /**
     * Stops the in-progress maintenance of the current room and progresses to the next room.
     * The same steps should be undertaken as described in the dot point list in elapseOneMinute().
     */
    public void skipCurrentMaintenance() {
        this.timeElapsed = 0;
        this.currentRoom.setMaintenance(false);
        this.currentRoomIndex++;
        if (this.currentRoomIndex == this.roomOrder.size()) {
            this.currentRoomIndex = 0;
        }
        this.currentRoom = this.roomOrder.get(this.currentRoomIndex);
        this.currentRoom.setMaintenance(true);
    }


    /**
     * Returns the human-readable string representation of this maintenance schedule.
     * The format of the string to return is
     *
     * MaintenanceSchedule: currentRoom=#currentRoomNumber, currentElapsed=elapsed
     * where 'currentRoomNumber' is the room number of the room currently being maintained, and 'elapsed' is the number of minutes that have elapsed while maintaining the current room.
     * For example:
     * MaintenanceSchedule: currentRoom=#108, currentElapsed=3
     * Overrides:
     * toString in class Object
     * @return string representation of this maintenance schedule
     */
    @Override
    public String toString() {
        return "MaintenanceSchedule: " +
                "currentRoom=#" + this.currentRoom.getRoomNumber() +
                ", currentElapsed=" + this.timeElapsed;
    }

    /**
     * Returns the machine-readable string representation of this maintenance schedule.
     * The format of the string to return is:
     *  roomNumber1,roomNumber2,...,roomNumberN
     * where 'roomNumberX' is the room number of the Xth room in this maintenance schedule's room order,
     * from 1 to N where N is the number of rooms in the maintenance order.
     * There should be no newline at the end of the string.
     *
     * See the demo save file for an example (uqstlucia.txt).
     *
     * Specified by:
     * encode in interface Encodable
     * @return encoded string representation of this maintenance schedule
     */
    @Override
    public String encode() {
        String encodeRoomOrder = "";
        for (Room room:this.roomOrder) {
            encodeRoomOrder += (Integer.toString(room.getRoomNumber()) + ",");
        }
        return encodeRoomOrder.substring(0, encodeRoomOrder.length() - 1);
    }
}
