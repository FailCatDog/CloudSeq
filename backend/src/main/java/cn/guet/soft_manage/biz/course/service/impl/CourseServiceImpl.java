package cn.guet.soft_manage.biz.course.service.impl;

import cn.guet.soft_manage.biz.course.dao.CourseDao;
import cn.guet.soft_manage.biz.course.dao.CourseEnrollmentDao;
import cn.guet.soft_manage.biz.course.dao.CourseStaffDao;
import cn.guet.soft_manage.biz.course.dto.request.CourseCreateRequestDTO;
import cn.guet.soft_manage.biz.course.dto.request.CourseEnrollmentCreateRequestDTO;
import cn.guet.soft_manage.biz.course.dto.request.CourseUpdateRequestDTO;
import cn.guet.soft_manage.biz.course.dto.response.CourseDetailDTO;
import cn.guet.soft_manage.biz.course.dto.response.CourseEnrollmentDTO;
import cn.guet.soft_manage.biz.course.dto.response.CourseStaffDTO;
import cn.guet.soft_manage.biz.course.dto.response.CourseSummaryDTO;
import cn.guet.soft_manage.biz.course.entity.Course;
import cn.guet.soft_manage.biz.course.entity.CourseEnrollment;
import cn.guet.soft_manage.biz.course.entity.CourseStaff;
import cn.guet.soft_manage.biz.course.service.CourseService;
import cn.guet.soft_manage.biz.user.dao.UserDao;
import cn.guet.soft_manage.biz.user.entity.User;
import cn.guet.soft_manage.frame.auth.UserContext;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.CacheCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

  private static final int DEFAULT_MIN_TEAM_SIZE = 2;
  private static final int DEFAULT_MAX_TEAM_SIZE = 5;
  private static final int DEFAULT_WEEKLY_REQUIRED = 1;

  @Resource
  private CourseDao courseDao;

  @Resource
  private CourseStaffDao courseStaffDao;

  @Resource
  private CourseEnrollmentDao courseEnrollmentDao;

  @Resource
  private UserDao userDao;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public CourseDetailDTO createCourse(CourseCreateRequestDTO request) {
    Long teacherId = UserContext.getUserId();
    if (teacherId == null) {
      throw new BusinessException(BizResponseCode.UNAUTHORIZED);
    }

    boolean codeExists = courseDao.exists(new LambdaQueryWrapper<Course>()
        .eq(Course::getCourseCode, request.getCourseCode().trim()));
    if (codeExists) {
      throw new BusinessException(BizResponseCode.COURSE_CODE_EXISTS);
    }

    int minTeamSize = request.getMinTeamSize() != null ? request.getMinTeamSize() : DEFAULT_MIN_TEAM_SIZE;
    int maxTeamSize = request.getMaxTeamSize() != null ? request.getMaxTeamSize() : DEFAULT_MAX_TEAM_SIZE;
    validateTeamSize(minTeamSize, maxTeamSize);

    String status = CacheCode.COURSE_STATUS_DRAFT.getCode();

    Course course = Course.builder()
        .courseCode(request.getCourseCode().trim())
        .courseName(request.getCourseName().trim())
        .termYear(request.getTermYear())
        .termSeason(request.getTermSeason())
        .description(request.getDescription())
        .primaryTeacherId(teacherId)
        .status(status)
        .topicDeadline(request.getTopicDeadline())
        .minTeamSize(minTeamSize)
        .maxTeamSize(maxTeamSize)
        .weeklyRequired(request.getWeeklyRequired() != null ? request.getWeeklyRequired() : DEFAULT_WEEKLY_REQUIRED)
        .build();
    courseDao.insert(course);

    CourseStaff teacherStaff = CourseStaff.builder()
        .courseId(course.getId())
        .userId(teacherId)
        .staffRole(CacheCode.COURSE_STAFF_ROLE_TEACHER.getCode())
        .staffStatus(CacheCode.COURSE_STAFF_STATUS_ACTIVE.getCode())
        .build();
    courseStaffDao.insert(teacherStaff);

    return getCourseById(course.getId());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public CourseDetailDTO updateCourse(Long courseId, CourseUpdateRequestDTO request) {
    Course course = requireCourse(courseId);

    if (request.getMinTeamSize() != null || request.getMaxTeamSize() != null) {
      int minTeamSize = request.getMinTeamSize() != null ? request.getMinTeamSize() : course.getMinTeamSize();
      int maxTeamSize = request.getMaxTeamSize() != null ? request.getMaxTeamSize() : course.getMaxTeamSize();
      validateTeamSize(minTeamSize, maxTeamSize);
    }

    boolean updated = new LambdaUpdateChainWrapper<>(courseDao)
        .eq(Course::getId, courseId)
        .set(StringUtils.hasText(request.getCourseName()), Course::getCourseName, request.getCourseName().trim())
        .set(request.getDescription() != null, Course::getDescription, request.getDescription())
        .set(StringUtils.hasText(request.getStatus()), Course::getStatus, request.getStatus())
        .set(request.getTopicDeadline() != null, Course::getTopicDeadline, request.getTopicDeadline())
        .set(request.getMinTeamSize() != null, Course::getMinTeamSize, request.getMinTeamSize())
        .set(request.getMaxTeamSize() != null, Course::getMaxTeamSize, request.getMaxTeamSize())
        .set(request.getWeeklyRequired() != null, Course::getWeeklyRequired, request.getWeeklyRequired())
        .set(Course::getUpdateUser, UserContext.getUserId())
        .set(Course::getUpdateDate, LocalDateTime.now())
        .update();
    if (!updated) {
      throw new BusinessException(BizResponseCode.COURSE_NOT_FOUND);
    }

    return getCourseById(courseId);
  }

  @Override
  public CourseDetailDTO getCourseById(Long courseId) {
    Course course = requireCourse(courseId);
    User teacher = userDao.selectById(course.getPrimaryTeacherId());

    List<CourseStaff> staffRows = courseStaffDao.selectList(new LambdaQueryWrapper<CourseStaff>()
        .eq(CourseStaff::getCourseId, courseId)
        .orderByAsc(CourseStaff::getId));

    Map<Long, User> userMap = loadUsers(staffRows.stream().map(CourseStaff::getUserId).collect(Collectors.toList()));

    List<CourseStaffDTO> staffList = staffRows.stream()
        .map(staff -> CourseStaffDTO.builder()
            .id(staff.getId())
            .courseId(staff.getCourseId())
            .userId(staff.getUserId())
            .userName(resolveDisplayName(userMap.get(staff.getUserId())))
            .staffRole(staff.getStaffRole())
            .staffStatus(staff.getStaffStatus())
            .build())
        .toList();

    return CourseDetailDTO.builder()
        .id(course.getId())
        .courseCode(course.getCourseCode())
        .courseName(course.getCourseName())
        .termYear(course.getTermYear())
        .termSeason(course.getTermSeason())
        .description(course.getDescription())
        .primaryTeacherId(course.getPrimaryTeacherId())
        .primaryTeacherName(resolveDisplayName(teacher))
        .status(course.getStatus())
        .topicDeadline(course.getTopicDeadline())
        .minTeamSize(course.getMinTeamSize())
        .maxTeamSize(course.getMaxTeamSize())
        .weeklyRequired(course.getWeeklyRequired())
        .createDate(course.getCreateDate())
        .updateDate(course.getUpdateDate())
        .staffList(staffList)
        .build();
  }

  @Override
  public List<CourseSummaryDTO> listCourses(Integer termYear, String termSeason, Long teacherId) {
    LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
        .eq(termYear != null, Course::getTermYear, termYear)
        .eq(StringUtils.hasText(termSeason), Course::getTermSeason, termSeason)
        .orderByDesc(Course::getTermYear)
        .orderByDesc(Course::getCreateDate);

    if (teacherId != null) {
      List<Long> courseIds = findCourseIdsByTeacher(teacherId);
      if (courseIds.isEmpty()) {
        return Collections.emptyList();
      }
      wrapper.in(Course::getId, courseIds);
    }

    List<Course> courses = courseDao.selectList(wrapper);
    return toSummaryList(courses);
  }

  @Override
  public List<CourseSummaryDTO> listMyCourses() {
    Long userId = UserContext.getUserId();
    if (userId == null) {
      return Collections.emptyList();
    }
    List<Long> courseIds = findCourseIdsByTeacher(userId);
    if (courseIds.isEmpty()) {
      return Collections.emptyList();
    }
    List<Course> courses = courseDao.selectList(new LambdaQueryWrapper<Course>()
        .in(Course::getId, courseIds)
        .orderByDesc(Course::getTermYear)
        .orderByDesc(Course::getCreateDate));
    return toSummaryList(courses);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public CourseEnrollmentDTO enrollStudent(Long courseId, CourseEnrollmentCreateRequestDTO request) {
    requireCourse(courseId);

    User student = userDao.selectById(request.getUserId());
    if (student == null) {
      throw new BusinessException(BizResponseCode.USER_NOT_FOUND);
    }

    CourseEnrollment existing = courseEnrollmentDao.selectOne(new LambdaQueryWrapper<CourseEnrollment>()
        .eq(CourseEnrollment::getCourseId, courseId)
        .eq(CourseEnrollment::getUserId, request.getUserId()));

    String studentNo = StringUtils.hasText(request.getStudentNo())
        ? request.getStudentNo().trim()
        : student.getStudentNo();

    if (existing != null) {
      if (Objects.equals(existing.getEnrollStatus(), CacheCode.ENROLL_STATUS_ENROLLED.getCode())) {
        throw new BusinessException(BizResponseCode.ENROLLMENT_ALREADY_EXISTS);
      }
      existing.setEnrollStatus(CacheCode.ENROLL_STATUS_ENROLLED.getCode());
      existing.setStudentNo(studentNo);
      existing.setEnrollDate(LocalDateTime.now());
      existing.setDropDate(null);
      courseEnrollmentDao.updateById(existing);
      return toEnrollmentDTO(existing, student);
    }

    CourseEnrollment enrollment = CourseEnrollment.builder()
        .courseId(courseId)
        .userId(request.getUserId())
        .studentNo(studentNo)
        .enrollStatus(CacheCode.ENROLL_STATUS_ENROLLED.getCode())
        .enrollDate(LocalDateTime.now())
        .build();
    courseEnrollmentDao.insert(enrollment);
    return toEnrollmentDTO(enrollment, student);
  }

  @Override
  public List<CourseEnrollmentDTO> listEnrollments(Long courseId) {
    requireCourse(courseId);

    List<CourseEnrollment> enrollments = courseEnrollmentDao.selectList(new LambdaQueryWrapper<CourseEnrollment>()
        .eq(CourseEnrollment::getCourseId, courseId)
        .eq(CourseEnrollment::getEnrollStatus, CacheCode.ENROLL_STATUS_ENROLLED.getCode())
        .orderByAsc(CourseEnrollment::getEnrollDate)
        .orderByAsc(CourseEnrollment::getId));

    Map<Long, User> userMap = loadUsers(enrollments.stream().map(CourseEnrollment::getUserId).toList());
    return enrollments.stream()
        .map(item -> toEnrollmentDTO(item, userMap.get(item.getUserId())))
        .toList();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void dropEnrollment(Long courseId, Long userId) {
    requireCourse(courseId);

    CourseEnrollment enrollment = courseEnrollmentDao.selectOne(new LambdaQueryWrapper<CourseEnrollment>()
        .eq(CourseEnrollment::getCourseId, courseId)
        .eq(CourseEnrollment::getUserId, userId)
        .eq(CourseEnrollment::getEnrollStatus, CacheCode.ENROLL_STATUS_ENROLLED.getCode()));
    if (enrollment == null) {
      throw new BusinessException(BizResponseCode.ENROLLMENT_NOT_FOUND);
    }

    enrollment.setEnrollStatus(CacheCode.ENROLL_STATUS_DROPPED.getCode());
    enrollment.setDropDate(LocalDateTime.now());
    courseEnrollmentDao.updateById(enrollment);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteCourse(Long courseId) {
    requireCourse(courseId);
    int rows = courseDao.deleteById(courseId);
    if (rows == 0) {
      throw new BusinessException(BizResponseCode.COURSE_NOT_FOUND);
    }
  }

  private Course requireCourse(Long courseId) {
    Course course = courseDao.selectById(courseId);
    if (course == null) {
      throw new BusinessException(BizResponseCode.COURSE_NOT_FOUND);
    }
    return course;
  }

  private void validateTeamSize(int minTeamSize, int maxTeamSize) {
    if (minTeamSize > maxTeamSize) {
      throw new BusinessException(BizResponseCode.COURSE_TEAM_SIZE_INVALID);
    }
  }

  private List<Long> findCourseIdsByTeacher(Long teacherId) {
    Set<Long> courseIds = new LinkedHashSet<>();

    courseDao.selectList(new LambdaQueryWrapper<Course>()
            .eq(Course::getPrimaryTeacherId, teacherId)
            .select(Course::getId))
        .forEach(course -> courseIds.add(course.getId()));

    courseStaffDao.selectList(new LambdaQueryWrapper<CourseStaff>()
            .eq(CourseStaff::getUserId, teacherId)
            .eq(CourseStaff::getStaffStatus, CacheCode.COURSE_STAFF_STATUS_ACTIVE.getCode())
            .select(CourseStaff::getCourseId))
        .forEach(staff -> courseIds.add(staff.getCourseId()));

    return new ArrayList<>(courseIds);
  }

  private List<CourseSummaryDTO> toSummaryList(List<Course> courses) {
    if (courses.isEmpty()) {
      return Collections.emptyList();
    }

    List<Long> teacherIds = courses.stream().map(Course::getPrimaryTeacherId).distinct().toList();
    Map<Long, User> userMap = loadUsers(teacherIds);

    return courses.stream()
        .map(course -> CourseSummaryDTO.builder()
            .id(course.getId())
            .courseCode(course.getCourseCode())
            .courseName(course.getCourseName())
            .termYear(course.getTermYear())
            .termSeason(course.getTermSeason())
            .primaryTeacherId(course.getPrimaryTeacherId())
            .primaryTeacherName(resolveDisplayName(userMap.get(course.getPrimaryTeacherId())))
            .status(course.getStatus())
            .topicDeadline(course.getTopicDeadline())
            .build())
        .toList();
  }

  private Map<Long, User> loadUsers(List<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return Collections.emptyMap();
    }
    List<Long> distinctIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
    if (distinctIds.isEmpty()) {
      return Collections.emptyMap();
    }
    return userDao.selectBatchIds(distinctIds).stream()
        .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
  }

  private CourseEnrollmentDTO toEnrollmentDTO(CourseEnrollment enrollment, User user) {
    return CourseEnrollmentDTO.builder()
        .id(enrollment.getId())
        .courseId(enrollment.getCourseId())
        .userId(enrollment.getUserId())
        .userName(resolveDisplayName(user))
        .studentNo(enrollment.getStudentNo())
        .enrollStatus(enrollment.getEnrollStatus())
        .enrollDate(enrollment.getEnrollDate())
        .dropDate(enrollment.getDropDate())
        .build();
  }

  private String resolveDisplayName(User user) {
    if (user == null) {
      return null;
    }
    if (StringUtils.hasText(user.getRealName())) {
      return user.getRealName();
    }
    if (StringUtils.hasText(user.getNickName())) {
      return user.getNickName();
    }
    return user.getUsername();
  }
}
