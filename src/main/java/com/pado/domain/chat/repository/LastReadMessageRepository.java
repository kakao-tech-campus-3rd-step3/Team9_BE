package com.pado.domain.chat.repository;

import com.pado.domain.chat.entity.LastReadMessage;
import com.pado.domain.study.entity.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LastReadMessageRepository extends JpaRepository<LastReadMessage, Long>, LastReadMessageRepositoryCustom {
    
    Optional<LastReadMessage> findByStudyMember(StudyMember studyMember);
}
