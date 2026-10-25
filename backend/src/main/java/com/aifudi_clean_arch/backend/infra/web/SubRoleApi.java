package com.aifudi_clean_arch.backend.infra.web;

import com.aifudi_clean_arch.backend.core.controller.SubRoleController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/subroles")
@RequiredArgsConstructor
public class SubRoleApi {
    private final SubRoleController subRoleController;



}
