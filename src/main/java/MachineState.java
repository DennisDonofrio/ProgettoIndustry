public class MachineState {
    private int leftGateCounter;
    private int rightGateCounter;
    private int leftDepoCounter;
    private int rightDepoCounter;
    private boolean leftGateOpen = false;
    private boolean rightGateOpen = false;
    private boolean sortingOpen = false;
    private boolean leftBatchPresent = false;
    private boolean rightBatchPresent = false;

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

    public void setLeftDepoCounter(int leftDepoCounter) {
        this.leftDepoCounter = leftDepoCounter;
    }

    public void incrementLeftDepoCounter(int incr){
        this.leftDepoCounter += incr;
    }

    public int getRightDepoCounter() {
        return rightDepoCounter;
    }

    public void incrementRightDepoCounter(int incr){
        this.rightDepoCounter += incr;
    }

    public void setRightDepoCounter(int rightDepoCounter) {
        this.rightDepoCounter = rightDepoCounter;
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
        this.leftBatchPresent = leftBatchPresent;
    }

    public boolean isRightBatchPresent() {
        return rightBatchPresent;
    }

    public void setRightBatchPresent(boolean rightBatchPresent) {
        this.rightBatchPresent = rightBatchPresent;
    }
}
