import MusicService.MusicService;
import MusicService.SongResult;
import com.alibaba.fastjson.JSON;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by qtfreet00 on 2017/2/5.
 */
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        List<SongResult> wy = MusicService.GetMusic("wy").SongSearch("泡沫", 1, 10);
        System.out.println(JSON.toJSONString(wy));
        List<SongResult> tt = MusicService.GetMusic("tt").SongSearch("泡沫", 1, 10);
        System.out.println(JSON.toJSONString(tt));
        List<SongResult> kg = MusicService.GetMusic("kg").SongSearch("泡沫", 1, 10);
        System.out.println(JSON.toJSONString(kg));
        List<SongResult> qq = MusicService.GetMusic("qq").SongSearch("泡沫", 1, 10);
        System.out.println(JSON.toJSONString(qq));
    }
}
