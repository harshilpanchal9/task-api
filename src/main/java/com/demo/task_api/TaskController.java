package com.demo.task_api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
public class TaskController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "task-api");
    }

    @GetMapping("/tasks")
    public List<String> tasks() {
        return List.of("Build pipeline", "Write tests", "Deploy to K8s");
    }
}