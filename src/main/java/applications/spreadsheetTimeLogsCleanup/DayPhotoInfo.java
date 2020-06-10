package applications.spreadsheetTimeLogsCleanup;

import java.util.HashMap;

public class DayPhotoInfo {
    private final int id;
    private final String nameOfTheDay;
    private final HashMap<Integer, String> dayPhoto;

    public DayPhotoInfo(int id, String nameOfTheDay) {
        this.id = id;
        this.nameOfTheDay = nameOfTheDay;
        this.dayPhoto = createDefaultDayPhoto();
    }

    private HashMap<Integer, String> createDefaultDayPhoto() {
        HashMap<Integer, String> photo = new HashMap<>();
        return photo;
    }

    void update(Object o) {
        String logStr = (String) o;
        if (logStr.isEmpty()) {
            return;
        }
        LogCellSplitter splitter = new LogCellSplitter(logStr);
        if (splitter.myRoundedEndTime >= 25) {
            splitter.myRoundedEndTime -= 24;
        }
        if (splitter.myRoundedEndTime == 0) {
            splitter.myRoundedEndTime = 24;
        }
        if (splitter.myRoundedEndTime < splitter.myRoundedBeginTime) {
            String descriptionForSlot = createDescriptionForSlot(splitter);
            fillDaysSlots(splitter.myRoundedBeginTime, 24, descriptionForSlot);
            fillDaysSlots(0, splitter.myRoundedEndTime, descriptionForSlot);
        } else {
            fillDaysSlots(splitter.myRoundedBeginTime, splitter.myRoundedEndTime, createDescriptionForSlot(splitter));
        }
    }

    String get(int time) {
        return dayPhoto.get(time);
    }

    private String createDescriptionForSlot(LogCellSplitter splitter) {
        return splitter.myDescription +
                " (" +
                splitter.myBeginTime +
                " - " +
                splitter.myEndTime +
                ")";
    }

    private void fillDaysSlots(int myRoundedBeginTime, int myRoundedEndTime, String description) {
        assert myRoundedBeginTime < myRoundedEndTime;
        for (int i = myRoundedBeginTime; i < myRoundedEndTime; i++) {
            dayPhoto.computeIfPresent(i, (key, prevDescription) -> prevDescription + '\n' + description);
            dayPhoto.putIfAbsent(i, description);
        }
    }
}
