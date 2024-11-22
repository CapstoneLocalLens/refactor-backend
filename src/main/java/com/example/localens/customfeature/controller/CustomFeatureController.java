package com.example.localens.customfeature.controller;

import com.example.localens.customfeature.domain.CustomFeature;
import com.example.localens.customfeature.service.CustomFeatureService;
import com.example.localens.influx.InfluxDBService;
import com.example.localens.member.service.MemberService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/api/customFeatures")
public class CustomFeatureController {

    private final CustomFeatureService customFeatureService;
    private final InfluxDBService influxDBService;
    private final MemberService memberService;

    @Value("${influxdb.measurement}")
    private String measurement;

    @Autowired
    public CustomFeatureController(CustomFeatureService customFeatureService, InfluxDBService influxDBService,MemberService memberService) {
        this.customFeatureService = customFeatureService;
        this.influxDBService = influxDBService;
        this.memberService = memberService;
    }

    //현재 사용자 커스텀피처 조회 메소드
    /*@GetMapping
    public ResponseEntity<List<CustomFeature>> listCustomFeatures() {
        Long userId = memberService.getCurrentUserId();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<CustomFeature> customFeatures = customFeatureService.getCustomFeaturesByUserId(userId);
        return new ResponseEntity<>(customFeatures, HttpStatus.OK);
    }*/

    // 피처 생성 폼
    @GetMapping("/new")
    public String showCustomFeatureForm(Model model) {
        model.addAttribute("custom_feature", new CustomFeature());
        model.addAttribute("dataColumns", influxDBService.getFieldKeys(measurement));
        return "custom_feature_form";
    }

    // 피처 생성 처리
    @PostMapping
    public String createCustomFeature(@ModelAttribute CustomFeature customFeature,
                                      RedirectAttributes redirectAttributes) {
        // Long userId = getCurrentUserId(); 현재 사용자ID 설정
        // customFeature.setUserId(userId);

        if (!isValidFormula(customFeature.getFormula())) {
            redirectAttributes.addFlashAttribute("message", "유효하지 않은 수식입니다.");
            return "redirect:/customFeatures/new";
        }

        customFeatureService.saveCustomFeature(customFeature);
        redirectAttributes.addFlashAttribute("message", "피처가 성공적으로 생성되었습니다.");
        return "redirect:/customFeatures";
    }

    //피처 삭제
    @PostMapping("/delete/{customFeatureId}")
    public String deleteCustomFeature(@PathVariable Long customFeatureId, RedirectAttributes redirectAttributes) {
        customFeatureService.deleteFeature(customFeatureId);
        redirectAttributes.addFlashAttribute("message", "피처가 성공적으로 삭제되었습니다.");
        return "redirect:/customFeatures";
    }

    // 수식 검증 메서드
    private boolean isValidFormula(String formula) {
        try {
            // 변수 목록을 InfluxDBService에서 가져옴
            Set<String> variables = new HashSet<>(influxDBService.getFieldKeys(measurement));

            // 수식 파싱
            Expression e = new ExpressionBuilder(formula)
                    .variables(variables)
                    .build();

            for (String var : variables) {
                e.setVariable(var, 1);
            }

            e.evaluate();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}
