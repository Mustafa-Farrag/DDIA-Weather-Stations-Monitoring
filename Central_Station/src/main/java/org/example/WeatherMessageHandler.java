package org.example;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class WeatherMessageHandler {

    public ArrayList<ArrayList<WeatherMessage>> stationMessages;
    private String storageBaseDir;

    private final int BATCH_SIZE = 10;

    public WeatherMessageHandler(String storageBaseDir){

        this.storageBaseDir = storageBaseDir;

        stationMessages = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            stationMessages.add(new ArrayList<>());
            File stationDir = new File(storageBaseDir + "\\" + i);
            if (!stationDir.exists()) stationDir.mkdir();
        }
    }

    public void addMessage(String message){
        WeatherMessage weatherMessage = WeatherMessageParser.parse(message);

        stationMessages.get(weatherMessage.getStation_id()).add(weatherMessage);

        if (stationMessages.get(weatherMessage.getStation_id()).size() >= BATCH_SIZE ) {
            writeMessages(weatherMessage.getStation_id());
        }
    }

    private Calendar convertTimeStampToDate(long timestamp){
        Date date = new Date(timestamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    private void writeMessages(int stationID){
        WeatherMessage message = stationMessages.get(stationID).get(0);

        Calendar calendar = convertTimeStampToDate(message.getStatus_timestamp());
        String stationPath = storageBaseDir + "\\" + stationID + "\\"+
                calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH);
        File stationDir = new File(stationPath);
        if (!stationDir.exists()) stationDir.mkdir();

        WeatherMessage lastMessage = stationMessages.get(stationID).get(stationMessages.get(stationID).size()-1);

        int oldDate = convertTimeStampToDate(message.getStatus_timestamp()).get(Calendar.DAY_OF_MONTH);
        int newDate = convertTimeStampToDate(lastMessage.getStatus_timestamp()).get(Calendar.DAY_OF_MONTH);

        if (oldDate != newDate){
            ArrayList<WeatherMessage> oldMessages = (ArrayList<WeatherMessage>) stationMessages.get(stationID).stream()
                    .filter(m ->  convertTimeStampToDate(m.getStatus_timestamp()).get(Calendar.DAY_OF_MONTH) == oldDate).collect(Collectors.toList());
            ArrayList<WeatherMessage> newMessages = (ArrayList<WeatherMessage>) stationMessages.get(stationID).stream()
                    .filter(m ->  convertTimeStampToDate(m.getStatus_timestamp()).get(Calendar.DAY_OF_MONTH) == newDate).collect(Collectors.toList());
            MessagesParquetWriter.writeParquets(stationPath + "\\" + getParquetFileName(message.getStatus_timestamp())+ ".parquet", oldMessages);
            stationMessages.set(stationID, newMessages);

        } else {
            MessagesParquetWriter.writeParquets(stationPath + "\\" + getParquetFileName(message.getStatus_timestamp())+ ".parquet", stationMessages.get(stationID));
            stationMessages.get(stationID).clear();
        }
    }
    private String getParquetFileName (long timestamp){
        Date date = new java.util.Date(timestamp); // Convert seconds to milliseconds
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy_MM_dd_HH_mm_ss"); // Format pattern
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+3")); // Set the desired timezone (e.g., GMT-4)
        return sdf.format(date);
    }
}
