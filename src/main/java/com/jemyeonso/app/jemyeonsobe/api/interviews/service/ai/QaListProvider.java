package com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai;

import com.jemyeonso.app.jemyeonsobe.api.user.service.ai.dto.ImproveRequestDto.QaItem;
import java.util.List;

public interface QaListProvider {
    List<QaItem> getQaListForInterview(Long interviewId);
}
