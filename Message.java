public class Message{
    public static int JUMP = 1;
    public static int SWITCH = 2;
    public int message;
    public double target; // target second
    Message(int message,int target){
        this.message = message;
        this.target = target;
    }
    Message(int message,double target){
        this.message = message;
        this.target = target;
    }
}