package bms.floor;

import bms.building.Building;
import bms.room.Room;
import bms.room.RoomType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class MaintenanceScheduleTest {
    Building building1;
    Floor floor1;
    Floor floor2;
    Room room1;
    Room room2;
    Room room3;
    Room room4;
    List<Room> maintenanceList;
    MaintenanceSchedule maintenanceScheduleTest;

    @Before
    public void setUp() throws Exception {
        building1 = new Building("yyf");
        floor1 = new Floor(1, 10, 10);
        floor2 = new Floor(2, 10, 10);
        room1 = new Room(101, RoomType.STUDY, 20);
        room2 = new Room(102, RoomType.OFFICE, 25.6);
        room3 = new Room(103, RoomType.STUDY, 10);
        room4 = new Room(104, RoomType.LABORATORY, 20);
        building1.addFloor(floor1);
        floor1.addRoom(room1);
        floor1.addRoom(room2);
        floor1.addRoom(room3);
        floor1.addRoom(room4);
        maintenanceList = new ArrayList<>();
        maintenanceList.add(room1);
        maintenanceList.add(room2);
        maintenanceScheduleTest = new MaintenanceSchedule(maintenanceList);
    }

    @Test
    public void getMaintenanceTime() {
        assertEquals(14, maintenanceScheduleTest.getMaintenanceTime(room2));
    }

    @Test
    public void getCurrentRoom() {
        floor1.createMaintenanceSchedule(maintenanceList);
        MaintenanceSchedule maintenanceSchedule = floor1.getMaintenanceSchedule();
        assertEquals(room1, maintenanceSchedule.getCurrentRoom());
    }

    @Test
    public void getTimeElapsedCurrentRoom() {
        floor1.createMaintenanceSchedule(maintenanceList);
        MaintenanceSchedule maintenanceSchedule = floor1.getMaintenanceSchedule();
        assertEquals(0, maintenanceSchedule.getTimeElapsedCurrentRoom());
    }

    @Test
    public void elapseOneMinute() {
        floor1.createMaintenanceSchedule(maintenanceList);
        MaintenanceSchedule maintenanceSchedule = floor1.getMaintenanceSchedule();
        for (int i = 0; i < 8; i++) {
            maintenanceSchedule.elapseOneMinute();
        }
        assertEquals(room2, maintenanceSchedule.getCurrentRoom());
        assertEquals(0, maintenanceSchedule.getTimeElapsedCurrentRoom());
        for (int i = 0; i < 10; i++) {
            maintenanceSchedule.elapseOneMinute();
        }
        assertEquals(room2, maintenanceSchedule.getCurrentRoom());
        assertEquals(10, maintenanceSchedule.getTimeElapsedCurrentRoom());
        for (int i = 0; i < 8; i++) {
            maintenanceSchedule.elapseOneMinute();
        }
        assertEquals(room1, maintenanceSchedule.getCurrentRoom());
        assertEquals(4, maintenanceSchedule.getTimeElapsedCurrentRoom());
    }

    @Test
    public void skipCurrentMaintenance() {
        floor1.createMaintenanceSchedule(maintenanceList);
        MaintenanceSchedule maintenanceSchedule = floor1.getMaintenanceSchedule();
        for (int i = 0; i < 3; i++) {
            maintenanceSchedule.elapseOneMinute();
        }
        maintenanceSchedule.skipCurrentMaintenance();
        assertEquals(room2, maintenanceSchedule.getCurrentRoom());
        assertEquals(0, maintenanceSchedule.getTimeElapsedCurrentRoom());
    }

    @Test
    public void testToString() {
        assertEquals("MaintenanceSchedule: currentRoom=#101, currentElapsed=0", maintenanceScheduleTest.toString());
    }

    @Test
    public void encode() {
        assertEquals("101,102", maintenanceScheduleTest.encode());

    }
}