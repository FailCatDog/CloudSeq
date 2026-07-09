package cn.guet.soft_manage.biz.teaching.controller;

import cn.guet.soft_manage.biz.teaching.dto.TeachingStudentItemDTO;
import cn.guet.soft_manage.biz.teaching.service.TeachingStudentService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teaching")
public class TeachingStudentController {

    @Resource
    private TeachingStudentService teachingStudentService;

    @GetMapping("/students")
    public Response<List<TeachingStudentItemDTO>> listStudents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String courseCode) {
        return Response.success(teachingStudentService.listTeacherStudents(name, studentNo, courseCode));
    }
}
