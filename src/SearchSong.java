import MusicBean.SearchResult;
import MusicService.MusicService;
import MusicService.SongResult;
import com.alibaba.fastjson.JSON;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by qtfreet00 on 2017/2/5.
 */
public class SearchSong {
    public static void main(String[] args) throws UnsupportedEncodingException {

        List<SongResult> wy = MusicService.GetMusic("wy").SongSearch("泡沫", 1, 10);
        SearchResult searchResult = new SearchResult();
        if (wy == null) {
            searchResult.setStatus(404);
            searchResult.setSongs(null);
        } else {
            searchResult.setStatus(200);
            searchResult.setSongs(wy);
        }
        System.out.println(JSON.toJSONString(searchResult));
    }
}
