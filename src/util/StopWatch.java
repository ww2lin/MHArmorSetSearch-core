package util;

public class StopWatch {

    /* Private Instance Variables */
    /** Stores the start time when an object of the util.StopWatch class is initialized. */
    private long startTime;

    /**
     * Custom constructor which initializes the {@link #startTime} parameter.
     */
    public StopWatch() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Gets the elapsed time (in seconds) since the time the object of util.StopWatch was initialized.
     *
     * @return Elapsed time in seconds.
     */
    public double getElapsedTime() {
        long endTime = System.currentTimeMillis();
        return (double) (endTime - startTime) / (1000);
    }

    public double getElapsedTimeAndReset(){
        double elapsedTime = getElapsedTime();
        startTime = System.currentTimeMillis();
        return elapsedTime;
    }

    public void printMsgAndResetTime(String msg) {
        System.out.println(msg+" Time Elapsed(ms): "+getElapsedTimeAndReset());
    }
}
