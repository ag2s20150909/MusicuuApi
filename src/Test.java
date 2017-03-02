import MusicService.NetUtil;

/**
 * Created by qtfreet on 2017/3/2.
 */
public class Test {
    private static final String HOST = "http://music.163.com/weapi/cloudsearch/get/web?csrf_token=";

    public static void main(String[] args) {

        //通过歌手名去搜索专辑
        String key = "金泰妍";
        int page = 1;
        int size = 30;
        String text = "{\"s\":\"" + key + "\",\"type\":10,\"offset\":" + (page - 1) * size + ",\"limit\":" + size + ",\"total\":true}";
        System.out.println(text);
        String s = NetUtil.GetEncHtml(HOST, text, true);
        System.out.println(s);

        //通过歌手名去搜索mv
        String text1 = "{\"s\":\"" + key + "\",\"type\":1004,\"offset\":" + (page - 1) * size + ",\"limit\":" + size + ",\"total\":true}";
        String t = NetUtil.GetEncHtml(HOST, text1, true);
        System.out.println(t);

        //由上可以发现变化的仅仅是type值，
        // 10===通过歌手搜索专辑，
        // 1004===mv，
        // 1000===该歌手歌单（用户创建）
        // 100 === 搜索歌手
        // 1009 === 主播电台

    }
}
