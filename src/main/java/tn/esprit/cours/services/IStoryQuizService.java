package tn.esprit.cours.services;

import tn.esprit.cours.entity.StoryQuiz;
import tn.esprit.cours.entity.StoryWordBank;

import java.util.List;
import java.util.Map;

public interface IStoryQuizService {
    StoryQuiz createStoryQuiz(StoryQuiz storyQuiz);
    StoryQuiz getStoryQuizById(Long id);
    List<StoryQuiz> getAllStoryQuizzes();
    StoryQuiz updateStoryQuiz(Long id, StoryQuiz storyQuiz);
    void deleteStoryQuiz(Long id);
    StoryQuiz archiveStoryQuiz(Long id, boolean archived);
    StoryWordBank getWordBank(Long storyQuizId);
    StoryWordBank saveWordBank(StoryWordBank wordBank);
    Map<Integer, Boolean> validateAnswers(Long storyQuizId, Map<Integer, String> answers);
}
