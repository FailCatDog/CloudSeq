package cn.guet.soft_manage.biz.course.service;

import cn.guet.soft_manage.biz.course.dto.request.CourseCreateRequestDTO;
import cn.guet.soft_manage.biz.course.dto.request.CourseEnrollmentCreateRequestDTO;
import cn.guet.soft_manage.biz.course.dto.request.CourseUpdateRequestDTO;
import cn.guet.soft_manage.biz.course.dto.response.CourseDetailDTO;
import cn.guet.soft_manage.biz.course.dto.response.CourseEnrollmentDTO;
import cn.guet.soft_manage.biz.course.dto.response.CourseSummaryDTO;

import java.util.List;

public interface CourseService {

    CourseDetailDTO createCourse(CourseCreateRequestDTO request);

    CourseDetailDTO updateCourse(Long courseId, CourseUpdateRequestDTO request);

    CourseDetailDTO getCourseById(Long courseId);

    List<CourseSummaryDTO> listCourses(Integer termYear, String termSeason, Long teacherId);

    List<CourseSummaryDTO> listMyCourses();

    CourseEnrollmentDTO enrollStudent(Long courseId, CourseEnrollmentCreateRequestDTO request);

    List<CourseEnrollmentDTO> listEnrollments(Long courseId);

    void dropEnrollment(Long courseId, Long userId);

    void deleteCourse(Long courseId);
}
