package cn.guet.soft_manage.biz.teaching.service;

import cn.guet.soft_manage.biz.teaching.dto.TeachingStudentItemDTO;

import java.util.List;

public interface TeachingStudentService {

    List<TeachingStudentItemDTO> listTeacherStudents(String name, String studentNo, String courseCode);
}
