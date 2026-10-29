# Skill Benchmark: spring-boot-best-practices

**Model**: <model-name>
**Date**: 2026-06-23T17:05:37Z
**Evals**: 1, 2 (3 runs each per configuration)

## Summary

| Metric | With Skill | Without Skill | Delta |
|--------|------------|---------------|-------|
| Pass Rate | 100% ± 0% | 86% ± 21% | +0.15 |
| Time | 131.0s ± 7.1s | 153.0s ± 7.1s | -22.0s |
| Tokens | 30000 ± 7071 | 24000 ± 5657 | +6000 |

## Notes

- Using the skill resulted in a 100% pass rate across both evaluations, verifying all JpaRepository and Thymeleaf shared fragments requirements.
- The baseline run (without skill) for Eval-1 scored 100% because the user prompt was highly detailed, specifying record DTOs, mappers, and application.properties. However, it defaulted to the 'com.example' package base instead of the requested customization.
- The baseline run for Eval-2 failed to use JpaRepository (writing a mock ConcurrentHashMap database instead) and failed to generate layouts/fragments for header/footer.
- Execution time was reduced by 22 seconds on average (131.0s vs 153.0s) when using the skill.
- Token usage was slightly higher (+6000 output characters) when using the skill due to the generation of the H2 development database configurations and additional layout fragments.