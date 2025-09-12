package com.pado.domain.study.service;

import com.pado.domain.study.entity.Study;
import com.pado.domain.user.entity.User;
import org.springframework.stereotype.Service;

@Service
public class StudyMemberService {

    public boolean isStudyLeader(User user, Study study) {
        return study.getLeader().getId().equals(user.getId());
    }

    public boolean isStudyMember(User user, Long studyId) {
        // TODO: StudyMember 테이블 조회를 통해 실제 스터디 멤버인지 확인하는 로직 구현 필요.
        return true;
    }
}