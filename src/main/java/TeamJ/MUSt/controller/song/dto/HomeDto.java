package TeamJ.MUSt.controller.song.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class HomeDto {
    List<MainSongDto> songs = new ArrayList<>();
    Integer finishedSongCount;

    Integer finishedWordCount;

    public HomeDto(List<MainSongDto> songs, Integer finishedSongCount, Integer finishedWordCount) {
        this.songs = songs;
        this.finishedSongCount = finishedSongCount;
        this.finishedWordCount = finishedWordCount;
    }
}
