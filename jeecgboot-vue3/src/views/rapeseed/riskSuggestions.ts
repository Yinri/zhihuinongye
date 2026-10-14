// 兼容新旧 Python/Java 字段命名；旧综合建议为空时保留当前风险里已经生成的措施。
export function normalizeRiskSuggestions(response: any) {
  const risk = response.current_risk || response.currentRisk || {};
  const comprehensive = response.comprehensive_suggestions || response.comprehensiveSuggestions || {};
  const generated = risk.suggestions || {};
  const pick = (...values: any[]) => values.find(value => Array.isArray(value) && value.length > 0) || [];
  return {
    riskAlert: pick(comprehensive.riskAlert),
    immediate: pick(comprehensive.immediate, generated.urgent),
    shortTerm: pick(comprehensive.shortTerm, comprehensive.short_term),
    mediumTerm: pick(comprehensive.mediumTerm, comprehensive.medium_term, generated.mediumTerm, generated.medium_term),
    longTerm: pick(comprehensive.longTerm, comprehensive.long_term, generated.longTerm, generated.long_term),
  };
}
