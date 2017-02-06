import com.alibaba.fastjson.JSON;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by qtfreet00 on 2017/2/5.
 */
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        List<SongResult> wy = MusicService.GetMusic("wy").SongSearch("青衣谣", 1, 10);
        System.out.println(JSON.toJSONString(wy));
//        String s = WyMusic.GetLostPlayUrl("422094369", "320000");
//        System.out.println(s);


    }
}
