package cn.guet.soft_manage.biz.course.controller;

import cn.guet.soft_manage.biz.course.dto.request.CourseCreateRequestDTO;
import cn.guet.soft_manage.biz.course.dto.request.CourseEnrollmentCreateRequestDTO;
import cn.guet.soft_manage.biz.course.dto.request.CourseJoinByCodeRequestDTO;
import cn.guet.soft_manage.biz.course.dto.request.CourseUpdateRequestDTO;
import cn.guet.soft_manage.biz.course.dto.response.CourseDetailDTO;
import cn.guet.soft_manage.biz.course.dto.response.CourseEnrollmentDTO;
import cn.guet.soft_manage.biz.course.dto.response.CourseSummaryDTO;
import cn.guet.soft_manage.biz.course.service.CourseService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {

  @Resource
  private CourseService courseService;

  @PostMapping
  public Response<CourseDetailDTO> create(@Valid @RequestBody CourseCreateRequestDTO request) {
    return Response.success(courseService.createCourse(request));
  }

  @PutMapping("/{id}")
  public Response<CourseDetailDTO> update(@PathVariable Long id,
      @Valid @RequestBody CourseUpdateRequestDTO request) {
    return Response.success(courseService.updateCourse(id, request));
  }

  @DeleteMapping("/{id}")
  public Response<Void> delete(@PathVariable Long id) {
    courseService.deleteCourse(id);
    return Response.success();
  }

  @GetMapping("/mine")
  public Response<List<CourseSummaryDTO>> listMine() {
    return Response.success(courseService.listMyCourses());
  }

  @PostMapping("/join")
  public Response<CourseEnrollmentDTO> joinByCode(@Valid @RequestBody CourseJoinByCodeRequestDTO request) {
    return Response.success(courseService.joinByCourseCode(request));
  }

  @GetMapping("/{id}")
  public Response<CourseDetailDTO> getById(@PathVariable Long id) {
    return Response.success(courseService.getCourseById(id));
  }

  @GetMapping
  public Response<List<CourseSummaryDTO>> list(
      @RequestParam(required = false) Integer termYear,
      @RequestParam(required = false) String termSeason,
      @RequestParam(required = false) Long teacherId) {
    return Response.success(courseService.listCourses(termYear, termSeason, teacherId));
  }

  @PostMapping("/{id}/enrollments")
  public Response<CourseEnrollmentDTO> enroll(@PathVariable Long id,
      @Valid @RequestBody CourseEnrollmentCreateRequestDTO request) {
    return Response.success(courseService.enrollStudent(id, request));
  }

  @GetMapping("/{id}/enrollments")
  public Response<List<CourseEnrollmentDTO>> listEnrollments(@PathVariable Long id) {
    return Response.success(courseService.listEnrollments(id));
  }

  @PostMapping("/{id}/enrollments/{userId}/drop")
  public Response<Void> dropEnrollment(@PathVariable Long id, @PathVariable Long userId) {
    courseService.dropEnrollment(id, userId);
    return Response.success();
  }
}
