package tn.esprit.cours.services;

import tn.esprit.cours.entity.WritingPrompt;
import tn.esprit.cours.entity.WritingSubmission;

import java.util.List;

public interface IWritingService {

    WritingPrompt createPrompt(WritingPrompt prompt);

    WritingPrompt getPromptById(Long id);

    List<WritingPrompt> getAllPrompts();

    WritingPrompt updatePrompt(Long id, WritingPrompt prompt);

    void deletePrompt(Long id);

    WritingPrompt archivePrompt(Long id, boolean archived);

    WritingSubmission startOrResumeSubmission(Long userId, Long promptId);

    WritingSubmission saveProgress(Long submissionId, String text);

    WritingSubmission submitAndEvaluate(Long submissionId, String text);

    List<WritingSubmission> getUserSubmissions(Long userId);

    WritingSubmission getSubmissionById(Long submissionId);
}
