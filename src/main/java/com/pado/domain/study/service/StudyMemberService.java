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
        // 현재는 임시로 항상 true를 반환하며, 향후 스터디 멤버 기능 구현 시 반드시 수정해야 함.
        return true;
    }
}