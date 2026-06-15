import java.util.Objects;

public class MachineState {
    private static final int EXPECTED_BATCH_BALLS = 10;

    private int leftGateCounter;
    private int rightGateCounter;
    private int leftDepoCounter;
    private int rightDepoCounter;
    private int completedBatchCounter;
    private int totalProcessedCounter;
    private int totalGoodCounter;
    private int totalDiscardedCounter;
    private double averageGoodPerBatch;
    private double averageDiscardedPerBatch;
    private long sortingToGateTotalTimeMs;
    private int sortingToGateSamples;
    private boolean leftGateOpen = false;
    private boolean rightGateOpen = false;
    private boolean sortingOpen = false;
    private boolean leftBatchPresent = false;
    private boolean rightBatchPresent = false;
    private boolean checkBoxesError = false;
    private boolean monitoringActive = false;
    private boolean batchSeenDuringMonitoring = false;
    private int currentBatchBalls;
    private boolean missingBallWarning = false;
    private boolean extraBallWarning = false;
    private long monitoringStartTimeMs;
    private long batchSeenTimeMs;
    private long lastAppleTimeMs;
    private long firstAppleTimeMs;
    private long batchCompletedTimeMs;
    private long batchDurationMs;
    private long timeSinceLastAppleMs;
    private long firstAppleDelayMs;
    private boolean lineBlockedWarning = false;
    private boolean leftGateStuckOpenWarning = false;
    private boolean rightGateStuckOpenWarning = false;
    private boolean sortingGateStuckOpenWarning = false;
    private long leftGateOpenSinceMs;
    private long rightGateOpenSinceMs;
    private long sortingGateOpenSinceMs;

    public MachineState() {
    }

    public MachineState(MachineState other) {
        this.leftGateCounter = other.leftGateCounter;
        this.rightGateCounter = other.rightGateCounter;
        this.leftDepoCounter = other.leftDepoCounter;
        this.rightDepoCounter = other.rightDepoCounter;
        this.completedBatchCounter = other.completedBatchCounter;
        this.totalProcessedCounter = other.totalProcessedCounter;
        this.totalGoodCounter = other.totalGoodCounter;
        this.totalDiscardedCounter = other.totalDiscardedCounter;
        this.averageGoodPerBatch = other.averageGoodPerBatch;
        this.averageDiscardedPerBatch = other.averageDiscardedPerBatch;
        this.sortingToGateTotalTimeMs = other.sortingToGateTotalTimeMs;
        this.sortingToGateSamples = other.sortingToGateSamples;
        this.leftGateOpen = other.leftGateOpen;
        this.rightGateOpen = other.rightGateOpen;
        this.sortingOpen = other.sortingOpen;
        this.leftBatchPresent = other.leftBatchPresent;
        this.rightBatchPresent = other.rightBatchPresent;
        this.checkBoxesError = other.checkBoxesError;
        this.monitoringActive = other.monitoringActive;
        this.batchSeenDuringMonitoring = other.batchSeenDuringMonitoring;
        this.currentBatchBalls = other.currentBatchBalls;
        this.missingBallWarning = other.missingBallWarning;
        this.extraBallWarning = other.extraBallWarning;
        this.monitoringStartTimeMs = other.monitoringStartTimeMs;
        this.batchSeenTimeMs = other.batchSeenTimeMs;
        this.lastAppleTimeMs = other.lastAppleTimeMs;
        this.firstAppleTimeMs = other.firstAppleTimeMs;
        this.batchCompletedTimeMs = other.batchCompletedTimeMs;
        this.batchDurationMs = other.batchDurationMs;
        this.timeSinceLastAppleMs = other.timeSinceLastAppleMs;
        this.firstAppleDelayMs = other.firstAppleDelayMs;
        this.lineBlockedWarning = other.lineBlockedWarning;
        this.leftGateStuckOpenWarning = other.leftGateStuckOpenWarning;
        this.rightGateStuckOpenWarning = other.rightGateStuckOpenWarning;
        this.sortingGateStuckOpenWarning = other.sortingGateStuckOpenWarning;
        this.leftGateOpenSinceMs = other.leftGateOpenSinceMs;
        this.rightGateOpenSinceMs = other.rightGateOpenSinceMs;
        this.sortingGateOpenSinceMs = other.sortingGateOpenSinceMs;
    }

    public int getLeftGateCounter() {
        return leftGateCounter;
    }

    public void setLeftGateCounter(int leftGateCounter) {
        this.leftGateCounter = leftGateCounter;
    }

    public void incrementLeftGateCounter(){
        leftGateCounter++;
    }

    public int getRightGateCounter() {
        return rightGateCounter;
    }

    public void setRightGateCounter(int rightGateCounter) {
        this.rightGateCounter = rightGateCounter;
    }

    public void incrementRightGateCounter(){
        rightGateCounter++;
    }

    public int getLeftDepoCounter() {
        return leftDepoCounter;
    }

    public void incrementLeftDepoCounter(int incr){
        this.leftDepoCounter += incr;
        if (this.monitoringActive && incr > 0) {
            this.currentBatchBalls += incr;
            this.totalProcessedCounter += incr;
            this.totalGoodCounter += incr;
            updateBatchWarnings(System.currentTimeMillis());
        }
    }

    public int getRightDepoCounter() {
        return rightDepoCounter;
    }

    public void incrementRightDepoCounter(int incr){
        this.rightDepoCounter += incr;
        if (this.monitoringActive && incr > 0) {
            this.currentBatchBalls += incr;
            this.totalProcessedCounter += incr;
            this.totalDiscardedCounter += incr;
            updateBatchWarnings(System.currentTimeMillis());
        }
    }

    public boolean isLeftGateOpen() {
        return leftGateOpen;
    }

    public void setLeftGateOpen(boolean leftGateOpen) {
        this.leftGateOpen = leftGateOpen;
    }

    public boolean isRightGateOpen() {
        return rightGateOpen;
    }

    public void setRightGateOpen(boolean rightGateOpen) {
        this.rightGateOpen = rightGateOpen;
    }

    public boolean isSortingOpen() {
        return sortingOpen;
    }

    public void setSortingOpen(boolean sortingOpen) {
        this.sortingOpen = sortingOpen;
    }

    public boolean isLeftBatchPresent() {
        return leftBatchPresent;
    }

    public void setLeftBatchPresent(boolean leftBatchPresent) {
        System.out.println("LeftBatchPresent: " + leftBatchPresent);
        this.leftBatchPresent = leftBatchPresent;
        if (this.monitoringActive && leftBatchPresent) {
            this.batchSeenDuringMonitoring = true;
            if (this.batchSeenTimeMs == 0) {
                this.batchSeenTimeMs = System.currentTimeMillis();
            }
        }
    }

    public boolean isRightBatchPresent() {
        return rightBatchPresent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MachineState)) return false;
        MachineState that = (MachineState) o;
        return getLeftGateCounter() == that.getLeftGateCounter()
                && getRightGateCounter() == that.getRightGateCounter()
                && getLeftDepoCounter() == that.getLeftDepoCounter()
                && getRightDepoCounter() == that.getRightDepoCounter()
                && getCompletedBatchCounter() == that.getCompletedBatchCounter()
                && getTotalProcessedCounter() == that.getTotalProcessedCounter()
                && getTotalGoodCounter() == that.getTotalGoodCounter()
                && getTotalDiscardedCounter() == that.getTotalDiscardedCounter()
                && Double.compare(getAverageGoodPerBatch(), that.getAverageGoodPerBatch()) == 0
                && Double.compare(getAverageDiscardedPerBatch(), that.getAverageDiscardedPerBatch()) == 0
                && getAverageSortingToGateTimeMs() == that.getAverageSortingToGateTimeMs()
                && isLeftGateOpen() == that.isLeftGateOpen()
                && isRightGateOpen() == that.isRightGateOpen()
                && isSortingOpen() == that.isSortingOpen()
                && isLeftBatchPresent() == that.isLeftBatchPresent()
                && isRightBatchPresent() == that.isRightBatchPresent()
                && isCheckBoxesError() == that.isCheckBoxesError()
                && isMonitoringActive() == that.isMonitoringActive()
                && isBatchSeenDuringMonitoring() == that.isBatchSeenDuringMonitoring()
                && getCurrentBatchBalls() == that.getCurrentBatchBalls()
                && isMissingBallWarning() == that.isMissingBallWarning()
                && isExtraBallWarning() == that.isExtraBallWarning()
                && getBatchDurationMs() == that.getBatchDurationMs()
                && getTimeSinceLastAppleMs() == that.getTimeSinceLastAppleMs()
                && getFirstAppleDelayMs() == that.getFirstAppleDelayMs()
                && isLineBlockedWarning() == that.isLineBlockedWarning()
                && isLeftGateStuckOpenWarning() == that.isLeftGateStuckOpenWarning()
                && isRightGateStuckOpenWarning() == that.isRightGateStuckOpenWarning()
                && isSortingGateStuckOpenWarning() == that.isSortingGateStuckOpenWarning();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeftGateCounter(), getRightGateCounter(), getLeftDepoCounter(), getRightDepoCounter(), getCompletedBatchCounter(), getTotalProcessedCounter(), getTotalGoodCounter(), getTotalDiscardedCounter(), getAverageGoodPerBatch(), getAverageDiscardedPerBatch(), getAverageSortingToGateTimeMs(), isLeftGateOpen(), isRightGateOpen(), isSortingOpen(), isLeftBatchPresent(), isRightBatchPresent(), isCheckBoxesError(), isMonitoringActive(), isBatchSeenDuringMonitoring(), getCurrentBatchBalls(), isMissingBallWarning(), isExtraBallWarning(), getBatchDurationMs(), getTimeSinceLastAppleMs(), getFirstAppleDelayMs(), isLineBlockedWarning(), isLeftGateStuckOpenWarning(), isRightGateStuckOpenWarning(), isSortingGateStuckOpenWarning());
    }

    public void setRightBatchPresent(boolean rightBatchPresent) {
        System.out.println("RightBatchPresent: " + rightBatchPresent);
        this.rightBatchPresent = rightBatchPresent;
        if (this.monitoringActive && rightBatchPresent) {
            this.batchSeenDuringMonitoring = true;
            if (this.batchSeenTimeMs == 0) {
                this.batchSeenTimeMs = System.currentTimeMillis();
            }
        }
    }

    public boolean isMonitoringActive() {
        return monitoringActive;
    }

    public boolean isCheckBoxesError() {
        return checkBoxesError;
    }

    public void setCheckBoxesError(boolean checkBoxesError) {
        this.checkBoxesError = checkBoxesError;
    }

    public void startMonitoring() {
        startMonitoring(System.currentTimeMillis());
    }

    public void startMonitoring(long nowMs) {
        this.monitoringActive = true;
        this.checkBoxesError = false;
        this.batchSeenDuringMonitoring = false;
        this.currentBatchBalls = 0;
        this.missingBallWarning = false;
        this.extraBallWarning = false;
        this.monitoringStartTimeMs = nowMs;
        this.batchSeenTimeMs = 0;
        this.lastAppleTimeMs = 0;
        this.firstAppleTimeMs = 0;
        this.batchCompletedTimeMs = 0;
        this.timeSinceLastAppleMs = 0;
        this.firstAppleDelayMs = 0;
        this.lineBlockedWarning = false;
        this.leftGateStuckOpenWarning = false;
        this.rightGateStuckOpenWarning = false;
        this.sortingGateStuckOpenWarning = false;
        this.leftGateOpenSinceMs = 0;
        this.rightGateOpenSinceMs = 0;
        this.sortingGateOpenSinceMs = 0;
        this.leftDepoCounter = 0;
        this.rightDepoCounter = 0;
    }

    public void finishMonitoring() {
        finishMonitoring(System.currentTimeMillis());
    }

    public void finishMonitoring(long nowMs) {
        if (this.monitoringActive && this.currentBatchBalls < EXPECTED_BATCH_BALLS) {
            this.missingBallWarning = true;
        }
        if (this.monitoringActive && this.monitoringStartTimeMs > 0) {
            this.batchDurationMs = nowMs - this.monitoringStartTimeMs;
        }
        if (this.monitoringActive && this.currentBatchBalls > 0) {
            this.completedBatchCounter++;
            this.averageGoodPerBatch = (double) this.totalGoodCounter / this.completedBatchCounter;
            this.averageDiscardedPerBatch = (double) this.totalDiscardedCounter / this.completedBatchCounter;
        }
        this.monitoringActive = false;
        this.leftGateOpen = false;
        this.rightGateOpen = false;
        this.sortingOpen = false;
        this.lineBlockedWarning = false;
        this.leftGateStuckOpenWarning = false;
        this.rightGateStuckOpenWarning = false;
        this.sortingGateStuckOpenWarning = false;
        this.leftGateOpenSinceMs = 0;
        this.rightGateOpenSinceMs = 0;
        this.sortingGateOpenSinceMs = 0;
    }

    public int getCurrentBatchBalls() {
        return currentBatchBalls;
    }

    public boolean isMissingBallWarning() {
        return missingBallWarning;
    }

    public boolean isExtraBallWarning() {
        return extraBallWarning;
    }

    public boolean isBatchSeenDuringMonitoring() {
        return batchSeenDuringMonitoring;
    }

    private void updateBatchWarnings() {
        updateBatchWarnings(System.currentTimeMillis());
    }

    private void updateBatchWarnings(long nowMs) {
        if (this.monitoringActive && this.currentBatchBalls > EXPECTED_BATCH_BALLS) {
            this.extraBallWarning = true;
        }
        if (this.monitoringActive && this.currentBatchBalls > 0) {
            this.lastAppleTimeMs = nowMs;
            if (this.firstAppleTimeMs == 0) {
                this.firstAppleTimeMs = nowMs;
                this.firstAppleDelayMs = nowMs - getFirstAppleDelayStartTime();
            }
            if (this.currentBatchBalls >= EXPECTED_BATCH_BALLS && this.batchCompletedTimeMs == 0) {
                this.batchCompletedTimeMs = nowMs;
            }
        }
    }

    public void updateTimingWarnings(long nowMs, long warningThresholdMs) {
        if (!this.monitoringActive) {
            return;
        }

        if (this.currentBatchBalls == 0) {
            this.firstAppleDelayMs = nowMs - getFirstAppleDelayStartTime();
            this.timeSinceLastAppleMs = 0;
        } else {
            this.timeSinceLastAppleMs = nowMs - this.lastAppleTimeMs;
        }

        this.lineBlockedWarning = this.currentBatchBalls > 0
                && this.currentBatchBalls < EXPECTED_BATCH_BALLS
                && !this.leftGateOpen
                && !this.rightGateOpen
                && !this.sortingOpen
                && this.timeSinceLastAppleMs > warningThresholdMs;

        updateGateStuckWarnings(nowMs, warningThresholdMs);
    }

    private void updateGateStuckWarnings(long nowMs, long warningThresholdMs) {
        if (this.leftGateOpen) {
            if (this.leftGateOpenSinceMs == 0) {
                this.leftGateOpenSinceMs = nowMs;
            }
            this.leftGateStuckOpenWarning = nowMs - this.leftGateOpenSinceMs > warningThresholdMs;
        } else {
            this.leftGateOpenSinceMs = 0;
            this.leftGateStuckOpenWarning = false;
        }

        if (this.rightGateOpen) {
            if (this.rightGateOpenSinceMs == 0) {
                this.rightGateOpenSinceMs = nowMs;
            }
            this.rightGateStuckOpenWarning = nowMs - this.rightGateOpenSinceMs > warningThresholdMs;
        } else {
            this.rightGateOpenSinceMs = 0;
            this.rightGateStuckOpenWarning = false;
        }

        if (this.sortingOpen) {
            if (this.sortingGateOpenSinceMs == 0) {
                this.sortingGateOpenSinceMs = nowMs;
            }
            this.sortingGateStuckOpenWarning = nowMs - this.sortingGateOpenSinceMs > warningThresholdMs;
        } else {
            this.sortingGateOpenSinceMs = 0;
            this.sortingGateStuckOpenWarning = false;
        }
    }

    private long getFirstAppleDelayStartTime() {
        if (this.batchSeenTimeMs > 0) {
            return this.batchSeenTimeMs;
        }
        return this.monitoringStartTimeMs;
    }

    public long getBatchDurationMs() {
        return batchDurationMs;
    }

    public int getCompletedBatchCounter() {
        return completedBatchCounter;
    }

    public int getTotalProcessedCounter() {
        return totalProcessedCounter;
    }

    public int getTotalGoodCounter() {
        return totalGoodCounter;
    }

    public int getTotalDiscardedCounter() {
        return totalDiscardedCounter;
    }

    public double getAverageGoodPerBatch() {
        return averageGoodPerBatch;
    }

    public double getAverageDiscardedPerBatch() {
        return averageDiscardedPerBatch;
    }

    public void addSortingToGateTime(long timeMs) {
        this.sortingToGateTotalTimeMs += timeMs;
        this.sortingToGateSamples++;
    }

    public long getAverageSortingToGateTimeMs() {
        if (sortingToGateSamples == 0) {
            return 0;
        }
        return sortingToGateTotalTimeMs / sortingToGateSamples;
    }

    public long getTimeSinceLastAppleMs() {
        return timeSinceLastAppleMs;
    }

    public long getFirstAppleDelayMs() {
        return firstAppleDelayMs;
    }

    public boolean isLineBlockedWarning() {
        return lineBlockedWarning;
    }

    public boolean isLeftGateStuckOpenWarning() {
        return leftGateStuckOpenWarning;
    }

    public boolean isRightGateStuckOpenWarning() {
        return rightGateStuckOpenWarning;
    }

    public boolean isSortingGateStuckOpenWarning() {
        return sortingGateStuckOpenWarning;
    }
}
