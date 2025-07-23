package com.demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class StudentController {

	private List<Student> students = new ArrayList<>(
			List.of(new Student(1, "shubham", 60), new Student(2, "sharad", 70)));

	@GetMapping("/students")
	public List<Student> getAllStudents() {
		return students;
	}

	@PostMapping("/students")
	public Student addNewStudent(@RequestBody Student student) {
		students.add(student);
		return student;
	}

}
