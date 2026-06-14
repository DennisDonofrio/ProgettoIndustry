import java.util.Objects;

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

    public MachineState() {
    }

    public MachineState(MachineState other) {
        this.leftGateCounter = other.leftGateCounter;
        this.rightGateCounter = other.rightGateCounter;
        this.leftDepoCounter = other.leftDepoCounter;
        this.rightDepoCounter = other.rightDepoCounter;
        this.leftGateOpen = other.leftGateOpen;
        this.rightGateOpen = other.rightGateOpen;
        this.sortingOpen = other.sortingOpen;
        this.leftBatchPresent = other.leftBatchPresent;
        this.rightBatchPresent = other.rightBatchPresent;
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
        System.out.println("LeftBatchPresent: " + leftBatchPresent);
        this.leftBatchPresent = leftBatchPresent;
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
                && isLeftGateOpen() == that.isLeftGateOpen()
                && isRightGateOpen() == that.isRightGateOpen()
                && isSortingOpen() == that.isSortingOpen()
                && isLeftBatchPresent() == that.isLeftBatchPresent()
                && isRightBatchPresent() == that.isRightBatchPresent();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeftGateCounter(), getRightGateCounter(), getLeftDepoCounter(), getRightDepoCounter(), isLeftGateOpen(), isRightGateOpen(), isSortingOpen(), isLeftBatchPresent(), isRightBatchPresent());
    }

    public void setRightBatchPresent(boolean rightBatchPresent) {
        System.out.println("RightBatchPresent: " + rightBatchPresent);
        this.rightBatchPresent = rightBatchPresent;
    }
}
