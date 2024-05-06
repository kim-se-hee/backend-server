package TeamJ.MUSt;

import TeamJ.MUSt.domain.*;
import TeamJ.MUSt.exception.NoSearchResultException;
import TeamJ.MUSt.repository.WordRepository;
import TeamJ.MUSt.service.song.SongInfo;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//@Component
@RequiredArgsConstructor
public class InitDb {
    static private String prefix = "C:\\Users\\saree98\\intellij-workspace\\MUSt\\src\\main\\resources\\thumbnail\\";
    static private String suffix = ".jpg";

    private final InitService initService;

    @PostConstruct
    public void init() throws IOException, NoSearchResultException {
        initService.initDb();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService{
        private final EntityManager em;
        private final WordExtractor wordExtractor;
        private final WordRepository wordRepository;
        public void initDb() throws IOException, NoSearchResultException {
            Member[] members = new Member[2];
            members[0] = new Member("member1");
            members[1] = new Member("member2");

            for (Member member : members)
                em.persist(member);

            Song[] songs = new Song[8];
            songs[0] = createSampleSong("BETELGEUSE", "Yuuri");
            songs[1] = createSampleSong("nandemonaiya", "RADWIMPS");
            songs[2] = createSampleSong("lemon", "Yonezu kenshi");
            songs[3] = createSampleSong("Flamingo", "Yonezu kenshi");
            songs[4] = createSampleSong("Madou Ito", "Masaki Suda");
            songs[5] = createSampleSong("Leo", "Yuuri");
            songs[6] = createSampleSong("BETELGEUSE", "KSUKE");
            songs[7] = createSampleSong("Bling-Bang-Bang-Born", "Creepy Nuts");

            for (Song song : songs){
                em.persist(song);
                List<WordInfo> wordInfos = wordExtractor.extractWords(song.getId());
                for (WordInfo wordInfo : wordInfos) {
                    String spelling = wordInfo.getLemma();
                    Word findWord = wordRepository.findBySpelling(spelling);

                    if(findWord == null){
                        List<String> before = wordInfo.getMeaning();
                        List<Meaning> after = new ArrayList<>();
                        if(before.size() == 1)
                            after.add(new Meaning(before.get(0)));
                        else{
                            after = before.stream().map(m -> new Meaning(m.substring(2))
                            ).toList();
                        }
                        Word newWord = new Word(wordInfo.getLemma(), wordInfo.getPronunciation(), after, wordInfo.getSpeechFields());
                        em.persist(newWord);
                        for (Meaning meaning : after) {
                            meaning.setWord(newWord);
                        }
                        SongWord songWord = new SongWord();
                        songWord.createSongWord(song, newWord);
                        song.getSongWords().add(songWord);
                    }
                }
            }

            for(int i = 0; i < songs.length; i++)
                new MemberSong().createMemberSong(members[0], songs[i]);

            for(int i = 2; i < songs.length; i++)
                new MemberSong().createMemberSong(members[1], songs[i]);

            List<SongInfo> songInfo = BugsCrawler.callBugsApi("sparkle", "radwimps");
            List<Song> searchedSongs = songInfo.stream().map(info -> {
                String lyrics = info.getLyrics();
                int length = lyrics.length();
                return new Song(
                        info.getTitle(),
                        info.getArtist().split("\\((.*?)\\)")[0],
                        length == 4 ? "" : lyrics.substring(2, length - 2),
                        imageToByte(info.getThumbnailUrl())
                );
            }).toList();
            for (Song song : searchedSongs)
                em.persist(song);

        }
    }

    private static Song createSampleSong(String title, String artist) throws IOException, NoSearchResultException {
        List<SongInfo> songInfos = BugsCrawler.callBugsApi(title, artist);
        SongInfo firstSong = songInfos.get(0);
        String lyrics = firstSong.getLyrics();
        if(lyrics.length() == 4)
            lyrics = "";
        else
            lyrics = lyrics.substring(2, lyrics.length() - 2);
        return new Song(title, artist, lyrics, imageToByte(firstSong.getThumbnailUrl()));
    }

    public static byte[] imageToByte(String imageURL){
        byte[] imageBytes = null;
        try {
            URL url = new URL(imageURL);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int bytesRead;
            InputStream stream = url.openStream();

            while ((bytesRead = stream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }

            imageBytes = outputStream.toByteArray();

            stream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageBytes;
    }
}
