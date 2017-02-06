/**
 * Created by qtfreet on 2017/2/6.
 */
public class Util {
    //将秒数转为时间
    public static String secTotime(int seconds) {

        int temp = 0;
        StringBuffer sb = new StringBuffer();
        temp = seconds / 3600;
        sb.append((temp < 10) ? "0" + temp + ":" : "" + temp + ":");

        temp = seconds % 3600 / 60;
        sb.append((temp < 10) ? "0" + temp + ":" : "" + temp + ":");

        temp = seconds % 3600 % 60;
        sb.append((temp < 10) ? "0" + temp : "" + temp);
        return sb.toString();
    }
}
