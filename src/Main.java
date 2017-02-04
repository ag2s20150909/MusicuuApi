import com.alibaba.fastjson.JSON;
import okhttp3.*;
import org.apache.commons.lang3.RandomStringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.print.DocFlavor;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

/**
 * Created by qtfreet00 on 2017/2/3.
 */
public class Main {
    final static private String modulus = "00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7" +
            "b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280" +
            "104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932" +
            "575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b" +
            "3ece0462db0a22b8e7";
    final static private String nonce = "0CoJUm6Qyw8W8jud";
    final static private String pubKey = "010001";
    private static OkHttpClient client = new OkHttpClient();
    private static MediaType Content_Type = MediaType.parse("application/x-www-form-urlencoded");
    private static Headers headers = new Headers.Builder()
            .add("Cookie", "__remember_me=true; MUSIC_U=5f9d910d66cb2440037d1c68e6972ebb9f15308b56bfeaa4545d34fbabf71e0f36b9357ab7f474595690d369e01fbb9741049cea1c6bb9b6; __csrf=8ea789fbbf78b50e6b64b5ebbb786176; os=uwp; osver=10.0.10586.318; appver=1.2.1; deviceId=0e4f13d2d2ccbbf31806327bd4724043")
            .add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36")
            .build();
    private static Headers headers2 = new Headers.Builder()
            .add("Cookie", "__remember_me=true; MUSIC_U=5f9d910d66cb2440037d1c68e6972ebb9f15308b56bfeaa4545d34fbabf71e0f36b9357ab7f474595690d369e01fbb9741049cea1c6bb9b6; __csrf=8ea789fbbf78b50e6b64b5ebbb786176; os=uwp; osver=10.0.10586.318; appver=1.2.1; deviceId=0e4f13d2d2ccbbf31806327bd4724043")
            .add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .add("User-Agent", "NativeHost")
            .build();

    public static void main(String[] args) throws IOException {
        search("sugar free", 1, 5);

    }


    private static void search(String key, int page, int size) throws IOException {
        String text = "{\"s\":\"" + key + "\",\"type\":1,\"offset\":" + (page - 1) * size + ",\"limit\":" + size + ",\"total\":true}";
        String s = GetEncHtml("http://music.163.com/weapi/cloudsearch/get/web?csrf_token=", text);
        System.out.println(s);
        NeteaseDatas neteaseDatas = JSON.parseObject(s, NeteaseDatas.class);
        List<NeteaseDatas.ResultBean.SongsBean> songs = neteaseDatas.getResult().getSongs();
        GetListByJson(songs);

    }

    private static String GetEncHtml(String url, String text) throws IOException {
//        String secKey = createSecretKey(16);
//        int pad = 16 - text.length() % 16;
//        for(int i=0;i<pad;i++){
//            text = text + (char)i;
//        }
        String param = encryptedRequest(text);
        RequestBody requestBody = RequestBody.create(Content_Type, param);
        Request request = new Request.Builder().url(url).post(requestBody).headers(headers).build();
        Response execute = client.newCall(request).execute();
        if (execute.isSuccessful()) {
            return execute.body().string();
        }
        return "";
    }

    private static String GetHtmlContent(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().headers(headers).build();
        Response execute = client.newCall(request).execute();
        if (execute.isSuccessful()) {
            return execute.body().string();
        }
        return "";
    }

    private static String GetHtmlContentNative(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().headers(headers2).build();
        Response execute = client.newCall(request).execute();
        if (execute.isSuccessful()) {
            return execute.body().string();
        }
        return "";
    }

    private static String GetPic(String id) {
        String html = null;
        try {
            html = GetHtmlContent("http://music.163.com/api/song/detail/?ids=%5B" + id + "%5D");
            NeteasePic neteasePic = JSON.parseObject(html, NeteasePic.class);
            return neteasePic.getSongs().get(0).getAlbum().getBlurPicUrl();
        } catch (IOException e) {
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
            System.out.println(AlbumArtist);
            int maxbr = songsBean.getPrivilege().getMaxbr();
            System.out.println(maxbr + "   ....");
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
//        int listLen = list.size();
//        for (int i = 0; i < listLen; i++) {
//            System.out.println(list.get(i).getFlacUrl());
//            System.out.println(list.get(i).getSqUrl());
//            System.out.println(list.get(i).getHqUrl());
//            System.out.println(list.get(i).getLqUrl());
//            System.out.println("--------------------------");
//        }
        String s = JSON.toJSONString(list);
        System.out.println(s);

        return list;
    }

    private static String GetLrc(String sid) {
        String url = "http://music.163.com/api/song/lyric?os=pc&id=" + sid + "&lv=-1&kv=-1&tv=-1";
        String html = null;
        try {
            html = GetHtmlContent(url);
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
            html = GetHtmlContent(url);
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
            html = GetHtmlContentNative(url);
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

//    private static String GetUrl(String id, String quality, String format)
//    {
//        String text = "";
//        switch (format.toLowerCase())
//        {
//            case "mp3":
//                if (quality == "320")
//                {
//                    text = GetPlayUrl(id, "320000");
//                }
//                else if (quality == "160")
//                {
//                    text = GetPlayUrl(id, "192000");
//                }
//                else
//                {
//                    text = GetPlayUrl(id, "128000");
//                }
//                break;
//            case "flac":
//                text = GetPlayUrl(id, "999000");
//                break;
//            case "mp4":
//                text = GetMvUrl(id, quality.ToLower());
//                break;
//            case "lrc":
//                text = GetLrc(id);
//                break;
//            case "krc":
//                text = GetLrc(id, format.ToLower());
//                break;
//            case "trc":
//                text = GetLrc(id, format.ToLower());
//                break;
//            case "jpg":
//                text = GetPic(id);
//                break;
//        }
//        return text;
//    }

    private static String GetPlayUrl(String id, String quality) {
        String text = "{\"ids\":[\"" + id + "\"],\"br\":" + quality + ",\"csrf_token\":\"\"}";
        String html = null;
        try {
            html = GetEncHtml("http://music.163.com/weapi/song/enhance/player/url?csrf_token=", text);
            System.out.println(html);
        } catch (IOException e) {

        }
        if (html.isEmpty()) {
            return null;
        }
//        System.out.println(html);
        NeteaseSongUrl neteaseSongUrl = JSON.parseObject(html, NeteaseSongUrl.class);
        if (neteaseSongUrl.getCode() == 200) {
            return neteaseSongUrl.getData().get(0).getUrl();
        }
        return "";
//        var json = JObject.Parse(html);
//        if (json["data"].First["code"].ToString() != "200") {
//            return GetLostUrl(id, quality);
//        }
//        var link = json["data"].First["url"].ToString();
//        return string.IsNullOrEmpty(link) || link == "null" ? "" : link;
    }

    //based on [darknessomi/musicbox](https://github.com/darknessomi/musicbox)
    static String encryptedRequest(String text) {
        String secKey = createSecretKey(16);
        String encText = aesEncrypt(aesEncrypt(text, nonce), secKey);
        String encSecKey = rsaEncrypt(secKey, pubKey, modulus);
        try {
            return "params=" + URLEncoder.encode(encText, "UTF-8") + "&encSecKey=" + URLEncoder.encode(encSecKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //ignore
            return null;
        }
    }

    //based on [darknessomi/musicbox](https://github.com/darknessomi/musicbox)
    private static String aesEncrypt(String text, String key) {
        try {
            IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(text.getBytes());

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            //ignore
            return null;
        }
    }

    //based on [darknessomi/musicbox](https://github.com/darknessomi/musicbox)
    private static String rsaEncrypt(String text, String pubKey, String modulus) {
        text = new StringBuilder(text).reverse().toString();
        BigInteger rs = new BigInteger(String.format("%x", new BigInteger(1, text.getBytes())), 16)
                .modPow(new BigInteger(pubKey, 16), new BigInteger(modulus, 16));
        String r = rs.toString(16);
        if (r.length() >= 256) {
            return r.substring(r.length() - 256, r.length());
        } else {
            while (r.length() < 256) {
                r = 0 + r;
            }
            return r;
        }
    }

    //based on [darknessomi/musicbox](https://github.com/darknessomi/musicbox)
    private static String createSecretKey(int i) {
        return RandomStringUtils.random(i, "0123456789abcde");
    }
}
