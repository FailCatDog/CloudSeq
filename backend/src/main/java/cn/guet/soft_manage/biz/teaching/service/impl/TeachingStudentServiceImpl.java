package cn.guet.soft_manage.biz.teaching.service.impl;

import cn.guet.soft_manage.biz.course.dao.CourseDao;
import cn.guet.soft_manage.biz.course.dao.CourseEnrollmentDao;
import cn.guet.soft_manage.biz.course.entity.Course;
import cn.guet.soft_manage.biz.course.entity.CourseEnrollment;
import cn.guet.soft_manage.biz.rbac.service.IDataScopeService;
import cn.guet.soft_manage.biz.teaching.dto.TeachingStudentItemDTO;
import cn.guet.soft_manage.biz.teaching.service.TeachingStudentService;
import cn.guet.soft_manage.biz.user.dao.UserDao;
import cn.guet.soft_manage.biz.user.entity.User;
import cn.guet.soft_manage.frame.auth.UserContext;
import cn.guet.soft_manage.frame.enums.CacheCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TeachingStudentServiceImpl implements TeachingStudentService {

    @Resource
    private CourseDao courseDao;

    @Resource
    private CourseEnrollmentDao courseEnrollmentDao;

    @Resource
    private UserDao userDao;

    @Resource
    private IDataScopeService dataScopeService;

    @Override
    public List<TeachingStudentItemDTO> listTeacherStudents(String name, String studentNo, String courseCode) {
        Long teacherId = UserContext.getUserId();
        if (teacherId == null) {
            return Collections.emptyList();
        }

        List<Long> courseIds = dataScopeService.resolveCourseIds(teacherId);
        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<Course>()
                .in(Course::getId, courseIds)
                .orderByDesc(Course::getTermYear)
                .orderByDesc(Course::getCreateDate);
        if (StringUtils.hasText(courseCode)) {
            courseWrapper.like(Course::getCourseCode, courseCode.trim());
        }

        List<Course> courses = courseDao.selectList(courseWrapper);
        if (courses.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, Function.identity(), (left, right) -> left));
        List<Long> filteredCourseIds = courses.stream().map(Course::getId).toList();

        List<CourseEnrollment> enrollments = courseEnrollmentDao.selectList(new LambdaQueryWrapper<CourseEnrollment>()
                .in(CourseEnrollment::getCourseId, filteredCourseIds)
                .eq(CourseEnrollment::getEnrollStatus, CacheCode.ENROLL_STATUS_ENROLLED.getCode())
                .orderByDesc(CourseEnrollment::getEnrollDate)
                .orderByAsc(CourseEnrollment::getId));

        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, User> userMap = loadUsers(enrollments.stream().map(CourseEnrollment::getUserId).toList());

        String nameKeyword = normalizeKeyword(name);
        String studentNoKeyword = normalizeKeyword(studentNo);

        return enrollments.stream()
                .map(enrollment -> toStudentItem(
                        enrollment,
                        userMap.get(enrollment.getUserId()),
                        courseMap.get(enrollment.getCourseId())))
                .filter(item -> matchesName(item, nameKeyword))
                .filter(item -> matchesStudentNo(item, studentNoKeyword))
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
                .collect(Collectors.toMap(User::getId, Function.identity(), (left, right) -> left));
    }

    private TeachingStudentItemDTO toStudentItem(CourseEnrollment enrollment, User user, Course course) {
        return TeachingStudentItemDTO.builder()
                .enrollmentId(enrollment.getId())
                .userId(enrollment.getUserId())
                .userName(resolveDisplayName(user))
                .studentNo(resolveStudentNo(enrollment, user))
                .courseId(enrollment.getCourseId())
                .courseCode(course != null ? course.getCourseCode() : null)
                .courseName(course != null ? course.getCourseName() : null)
                .enrollDate(enrollment.getEnrollDate())
                .build();
    }

    private String resolveDisplayName(User user) {
        if (user == null) {
            return null;
        }
        if (StringUtils.hasText(user.getRealName())) {
            return user.getRealName().trim();
        }
        if (StringUtils.hasText(user.getNickName())) {
            return user.getNickName().trim();
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername().trim();
        }
        return null;
    }

    private String resolveStudentNo(CourseEnrollment enrollment, User user) {
        if (StringUtils.hasText(enrollment.getStudentNo())) {
            return enrollment.getStudentNo().trim();
        }
        if (user != null && StringUtils.hasText(user.getStudentNo())) {
            return user.getStudentNo().trim();
        }
        return null;
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim().toLowerCase() : null;
    }

    private boolean matchesName(TeachingStudentItemDTO item, String keyword) {
        if (keyword == null) {
            return true;
        }
        String userName = item.getUserName();
        return userName != null && userName.toLowerCase().contains(keyword);
    }

    private boolean matchesStudentNo(TeachingStudentItemDTO item, String keyword) {
        if (keyword == null) {
            return true;
        }
        String no = item.getStudentNo();
        return no != null && no.toLowerCase().contains(keyword);
    }
}
