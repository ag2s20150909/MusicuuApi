import com.alibaba.fastjson.JSON;
import wy.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by qtfreet00 on 2017/2/3.
 */
public class WyMusic implements IMusic {


    private static List<SongResult> search(String key, int page, int size) throws IOException {
        String text = "{\"s\":\"" + key + "\",\"type\":1,\"offset\":" + (page - 1) * size + ",\"limit\":" + size + ",\"total\":true}";
        String s = NetUtil.GetEncHtml("http://music.163.com/weapi/cloudsearch/get/web?csrf_token=", text);
        NeteaseDatas neteaseDatas = JSON.parseObject(s, NeteaseDatas.class);
        List<NeteaseDatas.ResultBean.SongsBean> songs = neteaseDatas.getResult().getSongs();
        List<SongResult> songResults = GetListByJson(songs);
        return songResults;
    }


    private static String GetPic(String id) {
        String html = null;
        try {
            html = NetUtil.GetHtmlContent("http://music.163.com/api/song/detail/?ids=%5B" + id + "%5D");
            NeteasePic neteasePic = JSON.parseObject(html, NeteasePic.class);
            return neteasePic.getSongs().get(0).getAlbum().getBlurPicUrl();
        } catch (Exception e) {
            return "";
        }
    }

    private static List<SongResult> GetListByJson(List<NeteaseDatas.ResultBean.SongsBean> songs) throws IOException {
        List<SongResult> list = new ArrayList<>();
        int len = songs.size();
        if (len <= 0) {
            return null;
        }
        for (int i = 0; i < len; i++) {
            SongResult songResult = new SongResult();
            NeteaseDatas.ResultBean.SongsBean songsBean = songs.get(i);
            List<NeteaseDatas.ResultBean.SongsBean.ArBean> ar = songsBean.getAr();
            int arLen = ar.size();
            String artistName = "";
            for (int j = 0; j < arLen; j++) {
                artistName = artistName + ar.get(j).getName() + "/";
            }
            artistName = artistName.substring(0, artistName.length() - 1);
            String SongId = String.valueOf(songsBean.getId());
            String SongName = songsBean.getName();

            String Songlink = "http://music.163.com/#/song?id=" + String.valueOf(songsBean.getId());
            String ArtistId = String.valueOf(ar.get(0).getId());

            String AlbumId = String.valueOf(songsBean.getAl().getId());
            String AlbumName = songsBean.getAl().getName();

            String AlbumArtist = ar.get(0).getName();
            songResult.setArtistName(artistName);
            songResult.setArtistId(ArtistId);
            songResult.setSongId(SongId);
            songResult.setSongName(SongName);
            songResult.setSongLink(Songlink);
            songResult.setAlbumId(AlbumId);
            songResult.setAlbumName(AlbumName);
            String MvId = String.valueOf(songsBean.getMv());
            songResult.setMvId(MvId);
            if (Integer.valueOf(MvId) != 0) {
                songResult.setMvHdUrl(GetMvUrl(MvId, "hd"));
                songResult.setMvLdUrl(GetMvUrl(MvId, "ld"));
            }
            songResult.setLength("");
            songResult.setType("wy");
            songResult.setPicUrl(GetPic(SongId));
            songResult.setLrcUrl(GetLrcUrl(SongId));
            int maxbr = songsBean.getPrivilege().getMaxbr();
            if (maxbr == 999000) {
                String flac = GetPlayUrl(SongId, "999000");
                if (!flac.contains(".mp3")) {
                    songResult.setBitRate("无损");
                    songResult.setFlacUrl(flac);
                } else {
                    songResult.setFlacUrl("");
                    songResult.setBitRate("320K");
                }

                songResult.setSqUrl(GetPlayUrl(SongId, "320000"));
                songResult.setHqUrl(GetPlayUrl(SongId, "192000"));
                songResult.setLqUrl(GetPlayUrl(SongId, "128000"));

            } else if (maxbr == 320000) {
                songResult.setBitRate("320K");
                songResult.setFlacUrl("");
                songResult.setSqUrl(GetPlayUrl(SongId, "320000"));
                songResult.setHqUrl(GetPlayUrl(SongId, "192000"));
                songResult.setLqUrl(GetPlayUrl(SongId, "128000"));

            } else if (maxbr == 192000) {
                songResult.setBitRate("192K");
                songResult.setFlacUrl("");
                songResult.setSqUrl("");
                songResult.setHqUrl(GetPlayUrl(SongId, "192000"));
                songResult.setLqUrl(GetPlayUrl(SongId, "128000"));

            } else {
                songResult.setBitRate("128K");
                songResult.setFlacUrl("");
                songResult.setSqUrl("");
                songResult.setHqUrl("");
                songResult.setLqUrl(GetPlayUrl(SongId, "128000"));

            }
            if (songsBean.getFee() == 4 || songsBean.getPrivilege().getSt() != 0) {
                if (songResult.getBitRate().equals("无损")) {
                    songResult.setBitRate("320K");
                    songResult.setFlacUrl("");
                }
            }
            list.add(songResult);
        }
        String s = JSON.toJSONString(list);
        return list;
    }

    private static String GetLrc(String sid) {
        String url = "http://music.163.com/api/song/lyric?os=pc&id=" + sid + "&lv=-1&kv=-1&tv=-1";
        String html = null;
        try {
            html = NetUtil.GetHtmlContent(url);
            if (html.contains("uncollected")) {
                return null;
            }
            return JSON.parseObject(html, NeteaseLrc.class).getLrc().getLyric();
        } catch (Exception e) {
            return "";
        }
    }

    private static String GetLrcUrl(String sid) {
        String url = "http://music.163.com/api/song/lyric?os=pc&id=" + sid + "&lv=-1&kv=-1&tv=-1";
        String html = null;
        try {
            html = NetUtil.GetHtmlContent(url);
            if (html.contains("uncollected")) {
                return "";
            }
            return url;
        } catch (Exception e) {
            return "";
        }
    }

    private static String GetMvUrl(String mid, String quality) {
        String url = "http://music.163.com/api/song/mv?id=" + mid + "&type=mp4";
        String html = null;
        try {
            html = NetUtil.GetHtmlContent(url);
            NeteaseMv neteaseMv = JSON.parseObject(html, NeteaseMv.class);
            int len = neteaseMv.getMvs().size();
            HashMap<Integer, String> map = new HashMap<>();
            int max = 0;
            for (int i = 0; i < len; i++) {
                NeteaseMv.MvsBean mvsBean = neteaseMv.getMvs().get(i);
                int br = mvsBean.getBr();
                if (br > max) {
                    max = br;
                }
                map.put(br, mvsBean.getMvurl());
            }
            if (!quality.equals("ld")) {
                return map.get(max);
            }
            switch (max) {
                case 1080:
                    return map.get(720);
                case 720:
                    return map.get(480);
                case 480:
                    return map.containsKey(320) ? map.get(320) : map.get(240);
                default:
                    return map.get(240);
            }
        } catch (Exception e) {
            return "";
        }

    }


    private static String GetPlayUrl(String id, String quality) {
        String text = "{\"ids\":[\"" + id + "\"],\"br\":" + quality + ",\"csrf_token\":\"\"}";
        String html = null;
        try {
            html = NetUtil.GetEncHtml("http://music.163.com/weapi/song/enhance/player/url?csrf_token=", text);
            NeteaseSongUrl neteaseSongUrl = JSON.parseObject(html, NeteaseSongUrl.class);
            if (neteaseSongUrl.getCode() == 200) {
                return neteaseSongUrl.getData().get(0).getUrl();
            }
        } catch (Exception e) {

        }
        return "";
    }


    @Override
    public List<SongResult> SongSearch(String key, int page, int size) {
        try {
            return search(key, page, size);
        } catch (Exception e) {
            return null;
        }
    }
}
