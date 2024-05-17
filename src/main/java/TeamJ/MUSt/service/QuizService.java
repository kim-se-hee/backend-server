package TeamJ.MUSt.service;

import TeamJ.MUSt.domain.*;
import TeamJ.MUSt.repository.MeaningRepository;
import TeamJ.MUSt.repository.MemberRepository;
import TeamJ.MUSt.repository.QuizRepository;
import TeamJ.MUSt.repository.WordRepository;
import TeamJ.MUSt.repository.song.SongRepository;
import TeamJ.MUSt.repository.wordbook.MemberWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static TeamJ.MUSt.domain.QuizType.*;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final MemberRepository memberRepository;
    private final SongRepository songRepository;
    private final MemberWordRepository wordBookRepository;
    private final WordRepository wordRepository;
    private final MeaningRepository meaningRepository;

    public List<Quiz> findQuizzes(Long songId, QuizType type){
        return quizRepository.findBySongIdAndType(songId, type);
    }

    @Transactional
    public void updateWordBook(Long userId, Long songId){
        Optional<Member> findMember = memberRepository.findById(userId);
        List<Word> usedWords = songRepository.findUsedWords(songId);
        for (Word word : usedWords) {
            MemberWord wordBook = new MemberWord(findMember.get(), word);
            wordBookRepository.save(wordBook);
        }
    }
    @Transactional
    public List<Quiz> createMeaningQuiz(Long songId){
        if (hasQuizAlready(songId, MEANING))
            return null;

        Song targetSong = songRepository.findById(songId).get();
        List<SongWord> songWords = targetSong.getSongWords();
        List<Quiz> createdQuiz = new ArrayList<>();
        for (SongWord songWord : songWords) {
            Word targetWord = songWord.getWord();
            long count = wordRepository.count();

            long[] randomIds = new long[3];
            createRandomIds(count, randomIds);

            List<Choice> choiceList = new ArrayList<>();
            List<Answer> answerList = new ArrayList<>();


            Quiz newQuiz = new Quiz(targetSong, targetWord, MEANING, answerList, choiceList);

            createChoices(randomIds, choiceList, newQuiz, MEANING);
            createAnswer(answerList, targetWord, newQuiz, MEANING);
            quizRepository.save(newQuiz);
            createdQuiz.add(newQuiz);
        }

        return createdQuiz;
    }
    @Transactional
    public List<Quiz> createReadingQuiz(Long songId){
        if(hasQuizAlready(songId, READING))
            return null;

        Song targetSong = songRepository.findById(songId).get();
        List<SongWord> songWords = targetSong.getSongWords();
        List<Quiz> createdQuiz = new ArrayList<>();
        for (SongWord songWord : songWords) {
            Word targetWord = songWord.getWord();
            long count = wordRepository.count();

            long[] randomIds = new long[3];
            createRandomIds(count, randomIds);

            List<Choice> choiceList = new ArrayList<>();
            List<Answer> answerList = new ArrayList<>();

            Quiz newQuiz = new Quiz(targetSong, targetWord, READING, answerList, choiceList);

            createChoices(randomIds, choiceList, newQuiz, READING);
            createAnswer(answerList, targetWord, newQuiz, READING);

            quizRepository.save(newQuiz);
            createdQuiz.add(newQuiz);
        }

        return createdQuiz;
    }

    private boolean hasQuizAlready(Long songId, QuizType type) {
        //Quiz findQuiz= quizRepository.findFirst1BySongIdAndType(songId, type);
        /*if(findQuiz != null)
            return true;
        return false;*/
        return quizRepository.existsBySongIdAndType(songId, type);
    }



    private static void createAnswer(List<Answer> answerList, Word targetWord, Quiz quiz, QuizType type) {
        if(type == MEANING)
            answerList.add(new Answer(targetWord.getMeaning().get(0).getMeaning(), quiz));
        else if(type == READING)
            answerList.add(new Answer(targetWord.getJpPronunciation(), quiz));
    }

    private void createChoices(long[] randomIds, List<Choice> choiceList, Quiz quiz, QuizType type) {
        if(type == MEANING){
            for (long randomId : randomIds) {
                Meaning findMeaning = meaningRepository.findFirstByWordId(randomId);
                if(findMeaning == null){
                    System.out.println("찾으려 시도한 단어 id" + randomId);
                }
                Choice choice = new Choice(findMeaning.getMeaning().trim(), quiz);
                choiceList.add(choice);
            }
        }
        else if(type == READING){
            List<Long> idList = Arrays.stream(randomIds).boxed().toList();
            List<Word> randomWords = wordRepository.findInIds(idList);
            for (Word randomWord : randomWords) {
                Choice choice = new Choice(randomWord.getJpPronunciation(), quiz);
                choiceList.add(choice);
            }
        }
    }

    private static void createRandomIds(long count, long[] randomIds) {
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        for(int i = 0; i < 3; i++){
            long num = random.nextLong(count) + 1;
            for(int j = 0; j < i; j++){
                if(randomIds[j] == num){
                    i--;
                    break;
                }
            }
            randomIds[i] = num;
        }
    }
}
