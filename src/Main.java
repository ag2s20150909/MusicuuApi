import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * Created by qtfreet00 on 2017/2/5.
 */
public class Main {
    public static void main(String[] args) {
//        List<SongResult> wy = MusicService.GetMusic("wy").SongSearch("金泰妍", 1, 10);
//        System.out.println(JSON.toJSONString(wy));
        String s = WyMusic.GetLostPlayUrl("400875361", "");
        System.out.println(s);


    }
}
