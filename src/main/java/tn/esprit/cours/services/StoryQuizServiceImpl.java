package tn.esprit.cours.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.cours.entity.StoryBlank;
import tn.esprit.cours.entity.StoryQuiz;
import tn.esprit.cours.entity.StoryWordBank;
import tn.esprit.cours.repository.StoryQuizRepository;
import tn.esprit.cours.repository.StoryWordBankRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StoryQuizServiceImpl implements IStoryQuizService {

    private final StoryQuizRepository storyQuizRepository;
    private final StoryWordBankRepository storyWordBankRepository;

    @Override
    public StoryQuiz createStoryQuiz(StoryQuiz storyQuiz) {
        if (storyQuiz.getBlanks() != null) {
            for (StoryBlank blank : storyQuiz.getBlanks()) {
                blank.setStoryQuiz(storyQuiz);
            }
        }
        return storyQuizRepository.save(storyQuiz);
    }

    @Override
    public StoryQuiz getStoryQuizById(Long id) {
        return storyQuizRepository.findByIdWithBlanks(id)
                .orElseThrow(() -> new RuntimeException("StoryQuiz not found with id: " + id));
    }

    @Override
    public List<StoryQuiz> getAllStoryQuizzes() {
        return storyQuizRepository.findAllWithBlanks();
    }

    @Override
    public StoryQuiz updateStoryQuiz(Long id, StoryQuiz storyQuiz) {
        StoryQuiz existing = storyQuizRepository.findByIdWithBlanks(id)
                .orElseThrow(() -> new RuntimeException("StoryQuiz not found with id: " + id));
        existing.setTitle(storyQuiz.getTitle());
        existing.setStoryTemplate(storyQuiz.getStoryTemplate());
        existing.setIllustration(storyQuiz.getIllustration());
        existing.setXpReward(storyQuiz.getXpReward());
        existing.setDifficulty(storyQuiz.getDifficulty());

        if (storyQuiz.getBlanks() != null) {
            existing.getBlanks().clear();
            for (StoryBlank blank : storyQuiz.getBlanks()) {
                blank.setStoryQuiz(existing);
                existing.getBlanks().add(blank);
            }
        }

        return storyQuizRepository.save(existing);
    }

    @Override
    public void deleteStoryQuiz(Long id) {
        StoryQuiz existing = storyQuizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StoryQuiz not found with id: " + id));
        storyWordBankRepository.findByStoryQuizId(id).ifPresent(storyWordBankRepository::delete);
        storyQuizRepository.delete(existing);
    }

    @Override
    public StoryWordBank getWordBank(Long storyQuizId) {
        return storyWordBankRepository.findByStoryQuizId(storyQuizId)
                .orElseThrow(() -> new RuntimeException("WordBank not found for storyQuizId: " + storyQuizId));
    }

    @Override
    public StoryWordBank saveWordBank(StoryWordBank wordBank) {
        // Upsert: if one already exists for this storyQuizId, update it
        StoryWordBank existing = storyWordBankRepository.findByStoryQuizId(wordBank.getStoryQuizId())
                .orElse(null);
        if (existing != null) {
            existing.setWords(wordBank.getWords());
            return storyWordBankRepository.save(existing);
        }
        return storyWordBankRepository.save(wordBank);
    }

    @Override
    public Map<Integer, Boolean> validateAnswers(Long storyQuizId, Map<Integer, String> answers) {
        StoryQuiz quiz = getStoryQuizById(storyQuizId);
        Map<Integer, Boolean> results = new HashMap<>();

        for (StoryBlank blank : quiz.getBlanks()) {
            String submitted = answers.get(blank.getBlankIndex());
            boolean correct = blank.getCorrectWord() != null
                    && blank.getCorrectWord().equalsIgnoreCase(submitted != null ? submitted.trim() : "");
            results.put(blank.getBlankIndex(), correct);
        }

        return results;
    }
}
