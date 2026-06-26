package com.irctc.booking_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import java.util.List;
import java.io.File;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.irctc.booking_service.entities.Train;



public class TrainService {

	private List<Train> trainList;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String TRAIN_DB_PATH = "local_Db/train.json";

    public TrainService() throws IOException {
        /*File trains = new File(TRAIN_DB_PATH);
        trainList = objectMapper.readValue(trains, new TypeReference<List<Train>>() {});*/
        
    	File trains = new File(TRAIN_DB_PATH);

    	// create file if not exists
    	if (!trains.exists()) {
    	    File parentDir = trains.getParentFile();
    	    if (parentDir != null && !parentDir.exists()) {
    	        parentDir.mkdirs();
    	    }
    	    trains.createNewFile();
    	    trainList = new java.util.ArrayList<>();
    	    return;
    	}

    	// if file is empty
    	if (trains.length() == 0) {
    	    trainList = new java.util.ArrayList<>();
    	    return;
    	}

    	// read from file
    	trainList = objectMapper.readValue(trains, new TypeReference<List<Train>>() {});
    }

    public List<Train> searchTrains(String source, String destination) {
        return trainList.stream().filter(train -> validTrain(train, source, destination)).collect(Collectors.toList());
    }
    
	
    public void addTrain(Train newTrain) {
        // Check if a train with the same trainId already exists
        Optional<Train> existingTrain = trainList.stream()
                .filter(train -> train.getTrainId().equalsIgnoreCase(newTrain.getTrainId()))
                .findFirst();

        if (existingTrain.isPresent()) {
            // If a train with the same trainId exists, update it instead of adding a new one
            updateTrain(newTrain);
        } else {
            // Otherwise, add the new train to the list
            trainList.add(newTrain);
            saveTrainListToFile();
        }
    }

    public void updateTrain(Train updatedTrain) {
        // Find the index of the train with the same trainId
        OptionalInt index = IntStream.range(0, trainList.size())
                .filter(i -> trainList.get(i).getTrainId().equalsIgnoreCase(updatedTrain.getTrainId()))
                .findFirst();

        if (index.isPresent()) {
            // If found, replace the existing train with the updated one
            trainList.set(index.getAsInt(), updatedTrain);
            saveTrainListToFile();
        } else {
            // If not found, treat it as adding a new train
            addTrain(updatedTrain);
        }
    }

    private void saveTrainListToFile() {
        try {
            objectMapper.writeValue(new File(TRAIN_DB_PATH), trainList);
        } catch (IOException e) {
        	System.out.println("Error saving train data ❌");
        	
            e.printStackTrace(); // Handle the exception based on your application's requirements
        }
    }

    private boolean validTrain(Train train, String source, String destination) {
        
        List<String> stationOrder = train.getStations();

        if (stationOrder == null || stationOrder.isEmpty()) {
            return false;
        }
        
        int sourceIndex = -1;
        int destinationIndex = -1;

        for (int i = 0; i < stationOrder.size(); i++) {
            if (stationOrder.get(i).equalsIgnoreCase(source)) {
                sourceIndex = i;
            }
            if (stationOrder.get(i).equalsIgnoreCase(destination)) {
                destinationIndex = i;
            }
        }

        return sourceIndex != -1 && destinationIndex != -1 && sourceIndex < destinationIndex;
    }
}
