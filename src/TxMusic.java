import com.alibaba.fastjson.JSON;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import kg.KugouDatas;
import tx.TencentDatas;
import tx.TencentGetKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by qtfreet on 2017/2/6.
 */
public class TxMusic implements IMusic {
    private static List<SongResult> search(String key, int page, int size) throws Exception {
        String url = "http://soso.music.qq.com/fcgi-bin/search_cp?aggr=0&catZhida=0&lossless=1&sem=1&w=" + key + "&n=" + size + "&t=0&p=" + page + "&remoteplace=sizer.yqqlist.song&g_tk=5381&loginUin=0&hostUin=0&format=jsonp&inCharset=GB2312&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0";
        String html = NetUtil.GetHtmlContent(url, false);
        if (html.isEmpty()) {
            return null;//获取信息失败
        }
        html = html.substring(0, html.length() - 1).replace("callback(", "");
        System.out.println(html);
        TencentDatas tencentDatas = JSON.parseObject(html, TencentDatas.class);
        TencentDatas.DataBean.SongBean songs = tencentDatas.getData().getSong();
        int totalsize = songs.getTotalnum();
        if (totalsize == 0) {
            return null;//没有找到符合的歌曲
        }
        List<TencentDatas.DataBean.SongBean.ListBean> list = songs.getList();

        return GetListByJson(list);
//        KugouDatas kugouDatas = JSON.parseObject(s, KugouDatas.class);
//        if (kugouDatas == null) {
//            return null;//搜索歌曲失败
//        }
//        if (kugouDatas.getTotalCount() == 0) {
//            return null;//没有搜到歌曲
//        }
//        int totalsize = kugouDatas.getTotalCount();
//        List<KugouDatas.DataBean> data = kugouDatas.getData();
//        List<SongResult> songResults = GetListByJson(data);
//
//        return songResults;
    }

    //解析搜索时获取到的json，然后拼接成固定格式
    //具体每个返回标签的规范参考https://github.com/metowolf/NeteaseCloudMusicApi/wiki/%E7%BD%91%E6%98%93%E4%BA%91%E9%9F%B3%E4%B9%90API%E5%88%86%E6%9E%90---weapi
    private static List<SongResult> GetListByJson(List<TencentDatas.DataBean.SongBean.ListBean> songs) throws Exception {
        List<SongResult> list = new ArrayList<>();
        int len = songs.size();
        if (len <= 0) {
            return null;
        }
        for (int i = 0; i < len; i++) {
            SongResult songResult = new SongResult();
            NetUtil.init(songResult);
            TencentDatas.DataBean.SongBean.ListBean songsBean = songs.get(i);
            String SongId = String.valueOf(songsBean.getSongid());
            String SongName = songsBean.getSongname();
            String Songlink = "http://y.qq.com/#type=song&mid=" + String.valueOf(songsBean.getSongmid());
            String ArtistId = String.valueOf(songsBean.getSinger().get(0).getId());
            String AlbumId = String.valueOf(songsBean.getAlbumid());
            String AlbumName = songsBean.getAlbumname();
            String artistName = songsBean.getSinger().get(0).getName();
            songResult.setArtistName(artistName);
            songResult.setArtistId(ArtistId);
            songResult.setSongId(SongId);
            songResult.setSongName(SongName);
            songResult.setSongLink(Songlink);
            songResult.setAlbumId(AlbumId);
            songResult.setAlbumName(AlbumName);
            String mid = !songsBean.getStrMediaMid().isEmpty() ? songsBean.getStrMediaMid() : songsBean.getMedia_mid();
            if (songsBean.getSize128() != 0) {
                songResult.setBitRate("128K");
                double v = new Random(System.currentTimeMillis()).nextDouble();
                String key = GetKey(String.valueOf(v));
                songResult.setLqUrl("http://cc.stream.qqmusic.qq.com/M500" + mid + ".mp3?vkey=" + key + "&guid=" + v +
                        "&fromtag=0");
            }
            if (songsBean.getSizeogg() != 0) {
                songResult.setBitRate("192K");
                songResult.setHqUrl("http://stream.qqmusic.tc.qq.com/" + (Integer.parseInt(SongId) + 40000000) + ".ogg");
            }
            if (songsBean.getSize320() != 0) {

                songResult.setBitRate("320K");
                double v = new Random(System.currentTimeMillis()).nextDouble();
                String key = GetKey(String.valueOf(v));
                songResult.setLqUrl("http://cc.stream.qqmusic.qq.com/M800" + mid + ".mp3?vkey=" + key + "&guid=" + v +
                        "&fromtag=0");
            }
            if (songsBean.getSizeflac() != 0) {
                songResult.setBitRate("无损");
                songResult.setFlacUrl("http://stream.qqmusic.tc.qq.com/" + (Integer.parseInt(SongId) + 70000000) + ".flac");
            }
            songResult.setPicUrl("");
            songResult.setLength(Util.secTotime(songsBean.getInterval()));

            songResult.setType("qq");
//            songResult.setLrcUrl(GetLrcUrl(SongId, SongName, artistName)); //暂不去拿歌曲，直接解析浪费性能
            list.add(songResult);
        }
        return list;
    }

    private static String GetKey(String time) {
        try {

        }catch (Exception e){

        }
        String html =
                NetUtil.GetHtmlContent("http://base.music.qq.com/fcgi-bin/fcg_musicexpress.fcg?json=3&guid=" + time, false);
        html = html.substring(0, html.length() - 1).replace("jsonCallback(","");
        TencentGetKey tencentGetKey = JSON.parseObject(html, TencentGetKey.class);
        return tencentGetKey.getKey();
        //return Regex.Match(html, @ "(?<=key" ":\s*" ")[^" "]+").Value;
    }

    @Override
    public List<SongResult> SongSearch(String key, int page, int size) {
        try {
            return search(key, page, size);
        } catch (Exception e) {
            return null;//解析歌曲时失败
        }
    }
}
