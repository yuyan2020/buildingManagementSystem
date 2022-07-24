package bms.building;

import bms.building.Building;
import bms.building.BuildingInitialiser;
import bms.exceptions.FileFormatException;
import static org.junit.Assert.*;

import bms.hazardevaluation.HazardEvaluator;
import bms.hazardevaluation.WeightingBasedHazardEvaluator;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BuildingInitialiserTest {

    @Test
    public void testBuildingInitializeCorrectly() throws IOException, FileFormatException {
        List<Building> buildingList = new ArrayList<>();
        buildingList = BuildingInitialiser.loadBuildings("saves/uqstlucia.txt");
        Building building1 = buildingList.get(0);
        Building building2 = buildingList.get(1);
        Building building3 = buildingList.get(2);

        //test load building name correctly
        assertEquals("General Purpose South", building1.getName());
        assertEquals("Forgan Smith Building", building2.getName());
        assertEquals("Andrew N. Liveris Building", building3.getName());
        //test load building number of floors correctly
        assertEquals(5, building1.getFloors().size());
        //test load number of rooms correctly
        assertEquals(4, building1.getFloorByNumber(1).getRooms().size());
        //test load number of sensors
        assertEquals(2, building1.getFloorByNumber(2).getRoomByNumber(201).getSensors().size());
        //test load sensor
        assertEquals("TimedSensor: freq=3, readings=55,62,69,63, type=NoiseSensor",
                building1.getFloorByNumber(2).getRoomByNumber(201).
                        getSensor("NoiseSensor").toString());
        //test load rule based hazard evaluator
        assertEquals("RuleBased",
                building1.getFloorByNumber(2).getRoomByNumber(201).getHazardEvaluator().toString());
        //test load weighting based hazard evaluator
        assertEquals("WeightingBased",
                building1.getFloorByNumber(5).getRoomByNumber(501).getHazardEvaluator().toString());
        //test load weighting of the sensors
        List<Integer> testWeighting = new ArrayList<>();
        testWeighting.add(25);
        testWeighting.add(75);
        WeightingBasedHazardEvaluator weightingBasedHazardEvaluator =
                (WeightingBasedHazardEvaluator) building1.getFloorByNumber(5).getRoomByNumber(501).getHazardEvaluator();
        assertEquals(testWeighting,
                weightingBasedHazardEvaluator.getWeightings());
        //test the maintenance schedule of the floor
        assertEquals("MaintenanceSchedule: currentRoom=#101, currentElapsed=0",
                building1.getFloorByNumber(1).getMaintenanceSchedule().toString());
        //test second building in the file load correctly
        assertEquals(1, building2.getFloors().size());
        assertEquals(1, building2.getFloorByNumber(1).
                getRoomByNumber(103).getSensors().size());
        //test second building in the file load sensors correctly
        assertEquals("TimedSensor: freq=3, readings=745,1320,2782,3216,5043,3528,1970, " +
                        "type=CarbonDioxideSensor, idealPPM=700, varLimit=300",
                building2.getFloorByNumber(1).getRoomByNumber(107).
                        getSensor("CarbonDioxideSensor").toString());
        //test number of rooms zero
        assertEquals(0, building3.getFloorByNumber(1).getRooms().size());
    }

    @Test(expected = FileFormatException.class)
    public void testNumFloorsLess() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/numFloorsNotEqual.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testNumFloorsMore() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/numFloorsMore.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testNumRoomsMore() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/numRoomsMore.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testNumRoomsLess() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/numRoomsLess.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testNumSensorsLess() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/numSensorsLess.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testNumSensorsMore() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/numSensorsMore.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testMaintenanceScheduleNotMatch() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/maintenanceScheduleNotMatch.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testMaintenanceScheduleInvalid() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/maintenanceScheduleInvalid.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testDuplicateFloor() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/duplicateFloor.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testEmptyLine() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/emptyLine.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testFloorLessThanMinLength() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/floorLessThanMinLength.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testFloorLessThanMinWidth() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/floorLessThanMinWidth.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testNoFloorBelow() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/noFloorBelow.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testFloorTooLarge() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/floorTooLarge.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testDuplicateRoom() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/duplicateRoom.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testInsufficientSpaceOnFloor() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/insufficientSpaceOnFloor.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testRoomTypeLowCase() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/roomTypeLowCase.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testRoomTypeInvalid() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/roomTypeInvalid.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testRoomAreaTooSmall() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/roomAreaTooSmall.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testRoomHazardEvaluatorInvalid() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/roomHazardEvaluatorInvalid.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testEvaluatorWeightingInvalid() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/evaluatorWeightingInvalid.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testDuplicateSensor() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/duplicateSensor.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testSensorTypeInvalid() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/sensorTypeInvalid.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testUpdateFrequencyInvalid() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/updateFrequencyInvalid.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testCarbondioxideSensorVarlimitInvalid() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/carbondioxideSensorVarlimitInvalid.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testNumFloorsNegative() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/numFloorsNegative.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testNumRoomsNegative() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/numRoomsNegative.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testNumSensorNegative() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/numSensorNegative.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testSensorReadingsNegative() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/sensorReadingsNegative.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testOccupancySensorCapacityNegative() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/occupancySensorCapacityNegative.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testIdealCO2LevelNegative() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/IdealCO2LevelNegative.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testVarlimitNegative() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/varlimitNegative.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testFloorNumberNegative() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/floorNumberNegative.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testLessColon() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/lessColon.txt");
    }

    @Test(expected = FileFormatException.class)
    public void testMoreColon() throws IOException, FileFormatException {
        BuildingInitialiser.loadBuildings("saves/moreColon.txt");
    }

    @Test
    public void testNumFloorsZero() throws IOException, FileFormatException {
        List<Building> buildingList = new ArrayList<>();
        buildingList = BuildingInitialiser.loadBuildings("saves/numFloorsZero.txt");
        Building building2 = buildingList.get(1);
        assertEquals(0, building2.getFloors().size());
    }
}
