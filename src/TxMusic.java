import com.alibaba.fastjson.JSON;
import tx.TencentDatas;
import tx.TencentGetKey;
import tx.TencentMvData;
import tx.TencentMvKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
        TencentDatas tencentDatas = JSON.parseObject(html, TencentDatas.class);
        TencentDatas.DataBean.SongBean songs = tencentDatas.getData().getSong();
        int totalsize = songs.getTotalnum();
        if (totalsize == 0) {
            return null;//没有找到符合的歌曲
        }
        List<TencentDatas.DataBean.SongBean.ListBean> list = songs.getList();

        return GetListByJson(list);
//        TiantianDatas kugouDatas = JSON.parseObject(s, TiantianDatas.class);
//        if (kugouDatas == null) {
//            return null;//搜索歌曲失败
//        }
//        if (kugouDatas.getTotalCount() == 0) {
//            return null;//没有搜到歌曲
//        }
//        int totalsize = kugouDatas.getTotalCount();
//        List<TiantianDatas.DataBean> data = kugouDatas.getData();
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
            String mvId = songsBean.getVid();
            if (!mvId.isEmpty()) {
                songResult.setMvId(mvId);
                String hdMvUrl = GetMvUrl(songsBean.getVid(), "hd");
                songResult.setMvHdUrl(hdMvUrl);
                String ldMvUrl = GetMvUrl(songsBean.getVid(), "ld");
                songResult.setMvLdUrl(ldMvUrl);
            }
            String mid = !songsBean.getStrMediaMid().isEmpty() ? songsBean.getStrMediaMid() : songsBean.getMedia_mid();
            if (songsBean.getSize128() != 0) {
                songResult.setBitRate("128K");

                double v = new Random(System.currentTimeMillis()).nextDouble();
                String key = GetKey(String.valueOf(v));
                songResult.setLqUrl("http://cc.stream.qqmusic.qq.com/M500" + mid + ".mp3?vkey=" + key + "&guid=" + v +
                        "&fromtag=0");
            }
//            if (songsBean.getSizeogg() != 0) {
//                songResult.setBitRate("192K");
//                songResult.setHqUrl("http://stream.qqmusic.tc.qq.com/M800" + mid + ".mp3");
//            }
            //暂时不清楚如何解析192K

            if (songsBean.getSize320() != 0) {

                songResult.setBitRate("320K");
                double v = new Random(System.currentTimeMillis()).nextDouble();
                String key = GetKey(String.valueOf(v));
                songResult.setSqUrl("http://cc.stream.qqmusic.qq.com/M800" + mid + ".mp3?vkey=" + key + "&guid=" + v +
                        "&fromtag=0");
                if (songResult.getHqUrl().equals("")) {
                    songResult.setHqUrl("http://cc.stream.qqmusic.qq.com/M800" + mid + ".mp3?vkey=" + key + "&guid=" + v +
                            "&fromtag=0");
                }
            }
//            if (songsBean.getSizeflac() != 0) {
//                songResult.setBitRate("无损");
//                double v = new Random(System.currentTimeMillis()).nextDouble();
//                String key = GetKey(String.valueOf(v));
//                songResult.setFlacUrl("http://116.55.235.12/streamoc.music.tc.qq.com/F000" + mid + ".flac?vkey=" + key + "&guid=" + v +
//                        "&fromtag=0");
//
//            }
            //目前无法测试解析flac
            String albummid = songsBean.getAlbummid();
            songResult.setPicUrl("http://i.gtimg.cn/music/photo/mid_album_500/" + albummid.substring(albummid.length() - 2, albummid.length() - 1) + "/" + albummid.substring(albummid.length() - 1) + "/" + albummid + ".jpg");
            songResult.setLength(Util.secTotime(songsBean.getInterval()));

            songResult.setType("qq");
//            songResult.setLrcUrl(GetLrcUrl(SongId, SongName, artistName)); //暂不去拿歌曲，直接解析浪费性能
            list.add(songResult);
        }
        return list;
    }

    private static String GetMvUrl(String id, String quality) {
        String html = NetUtil.GetHtmlContent("http://vv.video.qq.com/getinfo?vid=" + id + "&platform=11&charge=1&otype=json", false);

        html = html.substring(0, html.length() - 1).replace("QZOutputJson=", "");
        TencentMvData tencentMvData = JSON.parseObject(html, TencentMvData.class);
        if (tencentMvData.getFl() == null) {
            return "";

        }
        List<TencentMvData.FlBean.FiBean> fi = tencentMvData.getFl().getFi();

        HashMap<String, Integer> dic = new HashMap<>();
        int count = fi.size();
        for (int i = 0; i < count; i++) {
            TencentMvData.FlBean.FiBean fiBean = fi.get(i);
            dic.put(fiBean.getName(), fiBean.getId());
        }
        int mvID = 0;
        if (quality.equals("hd")) {
            switch (count) {
                case 4:
                    mvID = dic.get("fhd");
                    break;
                case 3:
                    mvID = dic.get("shd");

                    break;
                case 2:
                    mvID = dic.get("hd");
                    break;
                default:
                    mvID = dic.get("sd");
                    break;
            }
        } else {
            switch (count) {
                case 4:
                    mvID = dic.get("shd");
                    break;
                case 3:
                    mvID = dic.get("hd");
                    break;

                default:
                    mvID = dic.get("sd");
                    break;
            }
        }
        String vkey = GetVkey(mvID, id);
        String fn = id + ".p" + (mvID - 10000) + ".1.mp4";
        String s = tencentMvData.getVl().getVi().get(0).getUl().getUi().get(0).getUrl() + fn + "?vkey=" + vkey;
        return s;
    }

    private static String GetVkey(int id, String videoId) {
        String fn = videoId + ".p" + (id - 10000) + ".1.mp4";
        String url = "http://vv.video.qq.com/getkey?format=" + id + "&otype=json&vid=" + videoId +
                "&platform=11&charge=1&filename=" + fn;

        String html = NetUtil.GetHtmlContent(url, false);
        if (html.isEmpty()) {
            return "";
        }
        html = html.substring(0, html.length() - 1).replace("QZOutputJson=", "");
        TencentMvKey tencentMvKey = JSON.parseObject(html, TencentMvKey.class);
        return tencentMvKey.getKey();

//        return Regex.Match(html, @"(?<=key"":"")[^""]+(?="")").Value;
    }

    private static String GetKey(String time) {
//        try {
//
//        }catch (Exception e){
//
//        }
        String html =
                NetUtil.GetHtmlContent("http://base.music.qq.com/fcgi-bin/fcg_musicexpress.fcg?json=3&guid=" + time, false);
        html = html.replace("jsonCallback(", "").replace(");", "");
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
