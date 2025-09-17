package com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai;


import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.QuestionRepository;
import com.jemyeonso.app.jemyeonsobe.api.user.service.ai.dto.ImproveRequestDto.QaItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterviewQaListProvider implements QaListProvider {

    private final QuestionRepository questionRepository;


    /**
     * 특정 인터뷰 ID에 속한 질문-답변 리스트를 가져옴
     * @param interviewId
     * @return
     */
    @Override
    public List<QaItem> getQaListForInterview(Long interviewId) {
        return questionRepository.findQaRowsByInterviewId(interviewId).stream()
            .filter(row -> row.getAnswer() != null && !row.getAnswer().isBlank())           // 답변 내용 없는 거 제거
            .map(row -> QaItem.builder()
                .question(row.getQuestion())
                .answer(row.getAnswer())
                .build())
            .toList();          // List로 반환
    }
}
