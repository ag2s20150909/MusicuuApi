import com.alibaba.fastjson.JSON;
import sun.misc.BASE64Encoder;
import wy.*;

import java.io.IOException;
import java.security.MessageDigest;
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

    //获取封面
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

    //解析搜索时获取到的json，然后拼接成固定格式
    //具体每个返回标签的规范参考https://github.com/metowolf/NeteaseCloudMusicApi/wiki/%E7%BD%91%E6%98%93%E4%BA%91%E9%9F%B3%E4%B9%90API%E5%88%86%E6%9E%90---weapi
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
            songResult.setLength(secTotime(songsBean.getDt() / 1000));
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
        return list;
    }

    //如果常规请求不到播放地址，也就是下架地址，则采用专辑去搜索
    public static String GetLostAlbumId(String id) {
        String text = "[{\"id\":\"" + id + "\"}]";
        HashMap<String, String> map = new HashMap<>();
        map.put("c", text);
        try {
            String s = NetUtil.PostData("http://music.163.com/api/v3/song/detail", map);
            int albumId = JSON.parseObject(s, NeteaseLostSongAlbumId.class).getSongs().get(0).getAl().getId();
            return String.valueOf(albumId);
        } catch (Exception e) {
            return null;
        }
    }

    //将秒数转为时间
    private static String secTotime(int seconds) {

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

    //解析lrc歌词
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

    //获取lrc歌词
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

    //获取mv地址
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

    //获取音乐播放地址
    private static String GetPlayUrl(String id, String quality) {
        String text = "{\"ids\":[\"" + id + "\"],\"br\":" + quality + ",\"csrf_token\":\"\"}";
        String html = null;
        try {
            html = NetUtil.GetEncHtml("http://music.163.com/weapi/song/enhance/player/url?csrf_token=", text);
            NeteaseSongUrl neteaseSongUrl = JSON.parseObject(html, NeteaseSongUrl.class);
            if (neteaseSongUrl.getCode() == 200) {
                return neteaseSongUrl.getData().get(0).getUrl();
            } else {
                return GetLostPlayUrl(id, quality);
            }
        } catch (Exception e) {

        }
        return "";
    }

    //获取下架音乐地址
    public static String GetLostPlayUrl(String id, String quality) {
        String albumId = GetLostAlbumId(id);
        try {
            String s = NetUtil.GetHtmlContent("http://music.163.com/api/album/" + albumId);
            NeteaseLostSong neteaseLostSong = JSON.parseObject(s, NeteaseLostSong.class);
            List<NeteaseLostSong.AlbumBeanX.SongsBean> songs = neteaseLostSong.getAlbum().getSongs();
            int size = songs.size();
            for (int i = 0; i < size; i++) {
                NeteaseLostSong.AlbumBeanX.SongsBean songsBean = songs.get(i);
                if (songsBean.getId() == Integer.parseInt(id)) {
                    switch (quality) {
                        case "320000":
                            String dfsId;
                            if (songsBean.getHMusic() == null) {
                                if (songsBean.getMMusic() == null) {
                                    return songsBean.getMp3Url();
                                }
                                dfsId = String.valueOf(songsBean.getMMusic().getDfsId());
                            } else {
                                dfsId = String.valueOf(songsBean.getHMusic().getDfsId());
                            }
                            return GetUrlBySid(dfsId);
                        case "192000":
                            if (songsBean.getMMusic() == null) {
                                return songsBean.getMp3Url();
                            } else {
                                dfsId = String.valueOf(songsBean.getMMusic().getDfsId());
                                return GetUrlBySid(dfsId);
                            }
                        default:
                            return songsBean.getMp3Url();
                    }
                }
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    //通过sid去搜索真实播放地址
    private static String GetUrlBySid(String dfsId) {
        String encryptPath = EncryptId(dfsId);
        String url = "http://m2.music.126.net/" + encryptPath + "/" + dfsId + ".mp3";
        return url;
    }

    public static String EncryptId(String id) {
        byte[] byte1 = "3go8&$8*3*3h0k(2)2".getBytes();
        byte[] byte2 = id.getBytes();
        int byte1Length = byte1.length;
        for (int i = 0; i < byte2.length; i++) {
            byte tmp = byte1[(i % byte1Length)];
            byte2[i] = ((byte) (byte2[i] ^ tmp));
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        byte[] md5Bytes = md5.digest(byte2);
        String retval = new BASE64Encoder().encode(md5Bytes);
        retval = retval.replace('/', '_');
        retval = retval.replace('+', '-');
        return retval;
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
