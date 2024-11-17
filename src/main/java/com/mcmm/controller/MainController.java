package com.mcmm.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/main/v1")
@PreAuthorize("hasRole('ADMIN')")
public class MainController {


}
