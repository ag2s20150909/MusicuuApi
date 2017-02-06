/**
 * Created by qtfreet00 on 2017/2/4.
 */
public class MusicService {
    public static IMusic GetMusic(String type) {
        switch (type) {

            case "wy":
                return new WyMusic();
            case "kg":
                return new KgMusic();
        }
        return null;
    }
}
