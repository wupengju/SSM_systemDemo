package com.menglin.service;

import com.menglin.entity.TeacherClassTeam;

public interface TeacherClassTeamService {
    int addTeacherClassTeam(TeacherClassTeam teacherClassTeam);

    int updateTeacherClassTeam(TeacherClassTeam teacherClassTeam);

    void deleteTeacherClassTeamById(Long id);

    TeacherClassTeam getTeacherClassTeamById(Long id);
}
