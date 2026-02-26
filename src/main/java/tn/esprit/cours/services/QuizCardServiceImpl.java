package tn.esprit.cours.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.cours.entity.QuizCard;
import tn.esprit.cours.repository.QuizCardRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizCardServiceImpl implements IQuizCardService {

    private final QuizCardRepository quizCardRepository;

    @Override
    public QuizCard createQuizCard(QuizCard quizCard) {
        return quizCardRepository.save(quizCard);
    }

    @Override
    public QuizCard getQuizCardById(Long id) {
        return quizCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QuizCard not found with id: " + id));
    }

    @Override
    public List<QuizCard> getAllQuizCards() {
        return quizCardRepository.findAll();
    }

    @Override
    public QuizCard updateQuizCard(Long id, QuizCard quizCard) {
        QuizCard existing = quizCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QuizCard not found with id: " + id));
        existing.setTitle(quizCard.getTitle());
        existing.setTotalQuestions(quizCard.getTotalQuestions());
        existing.setLevel(quizCard.getLevel());
        existing.setProgress(quizCard.getProgress());
        existing.setStatus(quizCard.getStatus());
        existing.setIcon(quizCard.getIcon());
        existing.setXpRequired(quizCard.getXpRequired());
        return quizCardRepository.save(existing);
    }

    @Override
    public void deleteQuizCard(Long id) {
        QuizCard existing = quizCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QuizCard not found with id: " + id));
        quizCardRepository.delete(existing);
    }
}
