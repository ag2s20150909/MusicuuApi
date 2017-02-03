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

    public static void main(String[] args) throws IOException {
        search("why", 1, 20);

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
            artistName = artistName.substring(0,artistName.length()-1);
            String SongId = String.valueOf(songsBean.getId());
            String SongName = songsBean.getName();
            String SongSubName = String.valueOf(songsBean.getAlia());
            String Songlink = "http://music.163.com/#/song?id=" + String.valueOf(songsBean.getId());
            String ArtistId = String.valueOf(ar.get(0).getId());
            String ArtistSubName = "";
            String AlbumId = String.valueOf(songsBean.getAl().getId());
            String AlbumName = songsBean.getAl().getName();
            String AlbumSubName = "";
            String AlbumArtist = ar.get(0).getName();
            songResult.setArtistName(artistName);
            songResult.setArtistId(ArtistId);
            songResult.setSongId(SongId);
            songResult.setSongName(SongName);
            songResult.setSongSubName(SongSubName);
            songResult.setSongLink(Songlink);
            songResult.setArtistSubName(ArtistSubName);
            songResult.setAlbumId(AlbumId);
            songResult.setAlbumName(AlbumName);
            songResult.setAlbumSubName(AlbumSubName);
            String MvId = String.valueOf(songsBean.getMv());
            songResult.setMvId(MvId);

            songResult.setMvHdUrl("");
            songResult.setMvLdUrl("");
            songResult.setLanguage("");
            songResult.setLength("");
            songResult.setCompany("");
            songResult.setYear("");
            songResult.setDisc("");
            songResult.setTrackNum("");
            songResult.setType("wy");
            songResult.setPicUrl("");
            songResult.setLrcUrl("");
            songResult.setTrcUrl("");
            songResult.setKrcUrl("");


            System.out.println(AlbumArtist);
            int maxbr = songsBean.getPrivilege().getMaxbr();
            System.out.println(maxbr + "   ....");
            if (maxbr == 999000) {
                songResult.setBitRate("无损");
                songResult.setFlacUrl(GetPlayUrl(SongId, "999000"));
                songResult.setSqUrl(GetPlayUrl(SongId, "320000"));
                songResult.setHqUrl(GetPlayUrl(SongId, "192000"));
                songResult.setLqUrl(GetPlayUrl(SongId, "128000"));
                songResult.setCopyUrl(songResult.getSqUrl());
            } else if (maxbr == 320000) {
                songResult.setBitRate("320K");
                songResult.setFlacUrl("");
                songResult.setSqUrl(GetPlayUrl(SongId, "320000"));
                songResult.setHqUrl(GetPlayUrl(SongId, "192000"));
                songResult.setLqUrl(GetPlayUrl(SongId, "128000"));
                songResult.setCopyUrl(songResult.getSqUrl());
            } else if (maxbr == 192000) {
                songResult.setBitRate("192K");
                songResult.setFlacUrl("");
                songResult.setSqUrl("");
                songResult.setHqUrl(GetPlayUrl(SongId, "192000"));
                songResult.setLqUrl(GetPlayUrl(SongId, "128000"));
                songResult.setCopyUrl(songResult.getHqUrl());
            } else {
                songResult.setBitRate("128K");
                songResult.setFlacUrl("");
                songResult.setSqUrl("");
                songResult.setHqUrl("");
                songResult.setLqUrl(GetPlayUrl(SongId, "128000"));
                songResult.setCopyUrl(songResult.getLqUrl());
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

    private static String GetPlayUrl(String id, String quality) throws IOException {
        String text = "{\"ids\":[\"" + id + "\"],\"br\":" + quality + ",\"csrf_token\":\"\"}";
        String html = GetEncHtml("http://music.163.com/weapi/song/enhance/player/url?csrf_token=", text);
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
