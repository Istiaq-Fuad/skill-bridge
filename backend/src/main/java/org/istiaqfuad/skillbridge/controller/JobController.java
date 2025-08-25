package org.istiaqfuad.skillbridge.controller;

import org.istiaqfuad.skillbridge.model.JobPost;
import org.istiaqfuad.skillbridge.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JobController {
    @Autowired
    public JobService service;

    @GetMapping("/")
    public String home() {
        return "Welcome to the Job Portal";
    }

    @GetMapping("jobs")
    public List<JobPost> getAllJobs() {
        return service.getJobs();
    }

    @GetMapping("jobs/keyword/{keyword}")
    public List<JobPost> getJobsByKeyword(@PathVariable("keyword") String keyword) {
        return service.searchJobsByKeyword(keyword);
    }
}
