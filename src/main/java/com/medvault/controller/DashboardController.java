package com.medvault.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    @GetMapping("/admin/dashboard")
    public String admin()  {
        return "dashboards/admin"; }
    @GetMapping("/doctor/dashboard")
    public String doctor() {
        return "dashboards/doctor"; }
    @GetMapping("/patient/dashboard")
    public String patient(){
        return "dashboards/patient"; }
}
